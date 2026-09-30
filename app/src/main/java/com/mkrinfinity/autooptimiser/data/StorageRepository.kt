package com.mkrinfinity.autooptimiser.data

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Process
import android.provider.DocumentsContract
import com.mkrinfinity.autooptimiser.storage.DuplicateDetector
import com.mkrinfinity.autooptimiser.storage.DuplicateVerifier
import com.mkrinfinity.autooptimiser.storage.HashBudget
import com.mkrinfinity.autooptimiser.storage.HashSource
import com.mkrinfinity.autooptimiser.storage.ScannedFile
import com.mkrinfinity.autooptimiser.storage.StorageClassification
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.util.concurrent.CancellationException
import kotlin.coroutines.coroutineContext

enum class StorageCategory(val label: String) {
    LARGE("Large files"), DOWNLOAD("Downloads"), DUPLICATE("Duplicates"), IMAGE("Images"), VIDEO("Videos"), AUDIO("Audio"), APK("APK files"), ARCHIVE("Archives"), OTHER("Other")
}

data class StorageEntry(
    val uri: Uri,
    val name: String,
    /** Human-readable selected-folder-relative path, not a URI or filesystem path. */
    val path: String,
    val sizeBytes: Long,
    val modifiedAtMillis: Long,
    val category: StorageCategory,
    val isDuplicateCopy: Boolean = false,
    /** SHA-256 of a complete, metadata-stable read only. */
    val contentHash: String? = null,
    val mimeType: String? = null,
    val typeCategory: StorageCategory = category,
    val isLargeFile: Boolean = false,
    val isInDownloads: Boolean = false,
    val duplicateCanonicalId: String? = null,
    val isDuplicateCanonical: Boolean = false
) {
    val id: String get() = uri.toString()

    /** Large/download/duplicate are independent facets, not mutually exclusive file types. */
    fun matchesCategory(filter: StorageCategory): Boolean = StorageClassification.matchesCategory(
        filter, typeCategory, isLargeFile || category == StorageCategory.LARGE,
        isInDownloads || category == StorageCategory.DOWNLOAD, isDuplicateCopy
    )
}

data class StorageReport(
    val rootName: String,
    val entries: List<StorageEntry>,
    /** Files or directories skipped due to metadata, read or enumeration errors (not just folders). */
    val skippedCount: Int,
    val scannedAtMillis: Long,
    val limitations: List<String> = emptyList(),
    val hashFailureCount: Int = 0,
    val hashBytesProcessed: Long = 0
) {
    val totalBytes: Long get() = entries.sumOf { it.sizeBytes }
    fun entries(category: StorageCategory): List<StorageEntry> = entries.filter { it.matchesCategory(category) }
}

data class DeleteResult(val entry: StorageEntry, val deleted: Boolean, val message: String? = null) {
    /** Bytes of confirmed successful deletions only, not requested/estimated reclaimable bytes. */
    val deletedBytes: Long get() = if (deleted) entry.sizeBytes else 0
}

data class StorageScanLimits(
    val maxFiles: Int = 10_000,
    val maxDirectories: Int = 2_000,
    val maxDocuments: Int = 20_000,
    val maxDepth: Int = 32,
    val maxHashBytes: Long = 512L * 1024 * 1024
) {
    init {
        require(maxFiles > 0 && maxDirectories > 0 && maxDocuments > 0 && maxDepth >= 0 && maxHashBytes >= 0)
    }
}

/** Only SAF document URIs from the most recent successful scan can be deleted. */
class StorageRepository(private val context: Context) {
    private val operation = Mutex()
    private var latestTree: Uri? = null
    private val latestEntries = linkedMapOf<String, StorageEntry>()
    private val latestMetadata = hashMapOf<String, Metadata>()

    suspend fun scanTree(treeUri: Uri, largeThresholdBytes: Long): StorageReport =
        scanTree(treeUri, largeThresholdBytes, StorageScanLimits())

    suspend fun scanTree(treeUri: Uri, largeThresholdBytes: Long, limits: StorageScanLimits): StorageReport = withContext(Dispatchers.IO) {
        operation.withLock {
            require(largeThresholdBytes >= 0)
            latestTree = null
            latestEntries.clear()
            latestMetadata.clear()
            require(treeUri.scheme == "content" && DocumentsContract.isTreeUri(treeUri)) { "Choose a folder using Android's folder picker." }
            val rootUri = DocumentsContract.buildDocumentUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri))
            // Query directly: DocumentFile.listFiles() hides provider exceptions as empty folders.
            val root = metadata(rootUri) ?: throw IOException("The selected folder is unavailable. Choose it again.")
            if (!root.isDirectory) throw IOException("The selected document is not a folder.")
            val rootName = displayName(root.name, "Selected folder")
            val warnings = linkedSetOf<String>()
            warnings += "Only the selected SAF folder is indexed; inaccessible folders and other device storage are not included."
            warnings += "Provider metadata is not a filesystem snapshot; files can change after verification."
            val entries = arrayListOf<StorageEntry>()
            val snapshots = hashMapOf<String, Metadata>()
            val queue = ArrayDeque<Directory>()
            queue.add(Directory(rootUri, rootName, 0, StorageClassification.isDownloadFolder(rootName)))
            val seen = hashSetOf(root.id)
            var skipped = 0
            var directories = 1
            var documents = 0
            var stopped = false
            while (queue.isNotEmpty() && !stopped) {
                coroutineContext.ensureActive()
                val directory = queue.removeFirst()
                try {
                    val childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getDocumentId(directory.uri))
                    val cursor = context.contentResolver.query(childrenUri, PROJECTION, null, null, null)
                        ?: throw IOException("Provider did not return a folder listing")
                    cursor.use {
                        while (true) {
                            coroutineContext.ensureActive()
                            if (documents >= limits.maxDocuments || entries.size >= limits.maxFiles) {
                                warnings += "File/document limit reached (${limits.maxFiles} files, ${limits.maxDocuments} documents); scan is incomplete."
                                stopped = true
                                break
                            }
                            if (!it.moveToNext()) break
                            documents++
                            val child = row(it)
                            if (child == null) { skipped++; continue }
                            if (!seen.add(child.id)) continue
                            val uri = DocumentsContract.buildDocumentUriUsingTree(treeUri, child.id)
                            val name = displayName(child.name, if (child.isDirectory) "Unnamed folder" else "Unnamed file")
                            val path = "${directory.path}/$name"
                            if (child.isDirectory) {
                                if (directory.depth >= limits.maxDepth || directories >= limits.maxDirectories) {
                                    skipped++
                                    warnings += "Folder depth/count limit reached (${limits.maxDepth} levels, ${limits.maxDirectories} folders); some folders were not scanned."
                                } else {
                                    directories++
                                    queue.add(Directory(uri, path, directory.depth + 1, directory.inDownloads || StorageClassification.isDownloadFolder(name)))
                                }
                            } else {
                                val size = child.size
                                if (size == null || size < 0 || child.mime == null) { skipped++; continue }
                                val type = StorageClassification.typeFor(name, child.mime)
                                val entry = StorageEntry(
                                    uri, name, path, size, child.modified?.coerceAtLeast(0) ?: 0, type,
                                    mimeType = child.mime, typeCategory = type,
                                    isLargeFile = size >= largeThresholdBytes,
                                    isInDownloads = directory.inDownloads
                                )
                                entries += entry
                                snapshots[entry.id] = child
                            }
                        }
                    }
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (denied: SecurityException) {
                    // A revoked grant must not masquerade as an empty successful scan.
                    throw SecurityException("Folder access was revoked. Choose the folder again.", denied)
                } catch (failure: Exception) {
                    if (directory.uri == rootUri) throw IOException("Could not read the selected folder. Choose it again.", failure)
                    skipped++
                    warnings += "Some folders could not be read; the index is incomplete."
                }
            }
            val scanContext = coroutineContext
            val checkCancelled = { scanContext.ensureActive() }
            val budget = HashBudget(limits.maxHashBytes)
            val verification = DuplicateVerifier.verify(entries.map { entry ->
                checkCancelled()
                source(entry, snapshots.getValue(entry.id))
            }, budget, checkCancelled = checkCancelled)
            if (verification.failures.isNotEmpty()) warnings += "${verification.failures.size} files could not be fully checked for duplicates (changed/unreadable files or hash budget exhausted)."
            if (budget.remainingBytes == 0L) warnings += "Hash budget of ${limits.maxHashBytes} bytes exhausted; unchecked files are not claimed as duplicates."
            if (skipped > 0) warnings += "$skipped files/folders were skipped; the index is incomplete."
            val hashed = entries.map { checkCancelled(); it.copy(contentHash = verification.fullHashes[it.id]) }
            // Use unique SAF identities, never display paths (providers can return duplicate names).
            val groups = DuplicateDetector.findGroups(hashed.map {
                checkCancelled()
                ScannedFile(it.id, it.sizeBytes, it.modifiedAtMillis, it.contentHash)
            }, checkCancelled)
            val canonicalIds = hashSetOf<String>()
            val duplicateToCanonical = hashMapOf<String, String>()
            groups.forEach { group ->
                checkCancelled()
                val canonical = group.canonicalFile.path
                canonicalIds += canonical
                group.duplicateFiles.forEach { checkCancelled(); duplicateToCanonical[it.path] = canonical }
            }
            val result = hashed.map { entry ->
                checkCancelled()
                val canonical = duplicateToCanonical[entry.id]
                entry.copy(
                    category = if (canonical != null) StorageCategory.DUPLICATE else entry.category,
                    isDuplicateCopy = canonical != null,
                    duplicateCanonicalId = canonical,
                    isDuplicateCanonical = entry.id in canonicalIds
                )
            }
            checkCancelled()
            if (metadata(rootUri)?.isDirectory != true) throw IOException("The selected folder is no longer available. Choose it again.")
            checkCancelled()
            latestTree = treeUri
            result.forEach { latestEntries[it.id] = it }
            latestMetadata.putAll(snapshots)
            StorageReport(rootName, result, skipped, System.currentTimeMillis(), warnings.toList(), verification.failures.size, budget.consumedBytes)
        }
    }

    suspend fun delete(entries: Collection<StorageEntry>): List<DeleteResult> = withContext(Dispatchers.IO) {
        operation.withLock {
            val deleteContext = coroutineContext
            val checkCancelled = { deleteContext.ensureActive() }
            val results = arrayListOf<DeleteResult>()
            val seen = hashSetOf<String>()
            // Re-verification is bounded too; never skip verification just to satisfy deletion.
            val budget = HashBudget(StorageScanLimits().maxHashBytes)
            for (entry in entries) {
                checkCancelled()
                if (!seen.add(entry.id)) continue
                try {
                    val tree = latestTree ?: throw IOException("Scan this folder again before deleting.")
                    if (latestEntries[entry.id] != entry) throw IOException("Item is not from the current scan. Scan again.")
                    if (entry.isDuplicateCanonical) throw IOException("The retained original of a duplicate group cannot be deleted. Keep it and delete only extra copies.")
                    val snapshot = latestMetadata[entry.id] ?: throw IOException("Missing scan metadata. Scan again.")
                    if (snapshot.modified == null || snapshot.modified <= 0) throw IOException("The provider does not expose a reliable modification time; deletion was refused.")
                    if (context.checkUriPermission(tree, Process.myPid(), Process.myUid(), Intent.FLAG_GRANT_WRITE_URI_PERMISSION) != PackageManager.PERMISSION_GRANTED) {
                        throw SecurityException("Write access to the selected folder is unavailable.")
                    }
                    val current = metadata(entry.uri)
                    if (current == null || current != snapshot || current.isDirectory) throw IOException("Item changed or disappeared since scanning. Scan again.")
                    if (current.flags and DocumentsContract.Document.FLAG_SUPPORTS_DELETE == 0) throw IOException("The provider does not permit deleting this item.")
                    if (entry.isDuplicateCopy) {
                        val canonicalId = entry.duplicateCanonicalId ?: throw IOException("Missing retained original.")
                        val canonical = latestEntries[canonicalId] ?: throw IOException("The retained original is no longer available.")
                        val originalMetadata = latestMetadata[canonicalId] ?: throw IOException("Missing retained original metadata.")
                        if (originalMetadata.modified == null || originalMetadata.modified <= 0) throw IOException("Cannot verify the retained original's metadata.")
                        val verified = DuplicateVerifier.verify(
                            listOf(source(entry, snapshot), source(canonical, originalMetadata)), budget,
                            fullHashThreshold = Long.MAX_VALUE, checkCancelled = checkCancelled
                        ).fullHashes
                        if (entry.contentHash == null || verified[entry.id] != entry.contentHash || verified[canonicalId] != entry.contentHash) {
                            throw IOException("The duplicate and retained original could not be fully re-verified. Nothing was deleted.")
                        }
                        if (metadata(canonical.uri) != originalMetadata) throw IOException("The retained original changed during verification.")
                    }
                    checkCancelled()
                    if (metadata(entry.uri) != snapshot) throw IOException("Item changed during verification. Scan again.")
                    checkCancelled()
                    val deleted = DocumentsContract.deleteDocument(context.contentResolver, entry.uri)
                    // Record a successful side effect before the next cancellation point.
                    if (deleted) {
                        latestEntries.remove(entry.id)
                        latestMetadata.remove(entry.id)
                    }
                    results += DeleteResult(entry, deleted, if (deleted) null else "Android did not confirm deletion.")
                } catch (cancelled: CancellationException) {
                    throw cancelled
                } catch (failure: Exception) {
                    results += DeleteResult(entry, false, failure.message ?: "Android could not delete this item.")
                }
            }
            results
        }
    }

    private fun source(entry: StorageEntry, snapshot: Metadata): HashSource = object : HashSource {
        override val id = entry.id
        override val sizeBytes = entry.sizeBytes
        override fun open(): InputStream = context.contentResolver.openInputStream(entry.uri)
            ?: throw IOException("Provider did not open the file")
        override fun isUnchanged(): Boolean = metadata(entry.uri) == snapshot
    }

    private fun metadata(uri: Uri): Metadata? {
        val cursor = context.contentResolver.query(uri, PROJECTION, null, null, null)
            ?: throw IOException("Provider did not return document metadata")
        return cursor.use { if (it.moveToFirst()) row(it) else null }
    }

    private fun row(cursor: Cursor): Metadata? {
        fun text(column: String): String? = cursor.getColumnIndex(column).takeIf { it >= 0 && !cursor.isNull(it) }?.let(cursor::getString)
        fun number(column: String): Long? = cursor.getColumnIndex(column).takeIf { it >= 0 && !cursor.isNull(it) }?.let(cursor::getLong)
        val id = text(DocumentsContract.Document.COLUMN_DOCUMENT_ID)?.takeIf { it.isNotBlank() } ?: return null
        return Metadata(id, text(DocumentsContract.Document.COLUMN_DISPLAY_NAME), text(DocumentsContract.Document.COLUMN_MIME_TYPE),
            number(DocumentsContract.Document.COLUMN_SIZE), number(DocumentsContract.Document.COLUMN_LAST_MODIFIED),
            number(DocumentsContract.Document.COLUMN_FLAGS)?.toInt() ?: 0)
    }

    private data class Metadata(val id: String, val name: String?, val mime: String?, val size: Long?, val modified: Long?, val flags: Int) {
        val isDirectory: Boolean get() = mime == DocumentsContract.Document.MIME_TYPE_DIR
    }
    private data class Directory(val uri: Uri, val path: String, val depth: Int, val inDownloads: Boolean)

    private fun displayName(name: String?, fallback: String): String = name?.takeIf { it.isNotBlank() }?.replace('/', '∕') ?: fallback
    private companion object {
        val PROJECTION = arrayOf(DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME,
            DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.COLUMN_SIZE,
            DocumentsContract.Document.COLUMN_LAST_MODIFIED, DocumentsContract.Document.COLUMN_FLAGS)
    }
}
