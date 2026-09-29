package com.mkrinfinity.autooptimiser.data

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.documentfile.provider.DocumentFile
import com.mkrinfinity.autooptimiser.storage.DuplicateDetector
import com.mkrinfinity.autooptimiser.storage.ScannedFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.File
import java.security.MessageDigest
import kotlin.coroutines.coroutineContext

enum class StorageCategory(val label: String) {
    LARGE("Large files"), DOWNLOAD("Downloads"), DUPLICATE("Duplicates"), IMAGE("Images"), VIDEO("Videos"), AUDIO("Audio"), APK("APK files"), ARCHIVE("Archives"), OTHER("Other")
}

data class StorageEntry(
    val uri: Uri,
    val name: String,
    val path: String,
    val sizeBytes: Long,
    val modifiedAtMillis: Long,
    val category: StorageCategory,
    val contentHash: String? = null,
    val mimeType: String? = null
) {
    val id: String get() = uri.toString()
}

data class StorageReport(
    val rootName: String,
    val entries: List<StorageEntry>,
    val skippedCount: Int,
    val scannedAtMillis: Long
) {
    val totalBytes: Long get() = entries.sumOf { it.sizeBytes }
    fun entries(category: StorageCategory): List<StorageEntry> = entries.filter { it.category == category }
}

data class DeleteResult(val entry: StorageEntry, val deleted: Boolean, val message: String? = null)

class StorageRepository(private val context: Context) {
    suspend fun scanTree(treeUri: Uri, largeThresholdBytes: Long): StorageReport = withContext(Dispatchers.IO) {
        val root = DocumentFile.fromTreeUri(context, treeUri)
            ?: return@withContext StorageReport("Selected folder", emptyList(), 1, System.currentTimeMillis())
        val entries = ArrayList<StorageEntry>()
        var skipped = 0
        val candidateSizes = HashMap<Long, MutableList<Int>>()
        val queue = ArrayDeque<DocumentFile>()
        queue.add(root)
        while (queue.isNotEmpty()) {
            coroutineContext.ensureActive()
            val directory = queue.removeFirst()
            val children = runCatching { directory.listFiles() }.getOrElse { skipped++; emptyArray() }
            children.forEach { child ->
                coroutineContext.ensureActive()
                if (child.isDirectory) queue.addLast(child)
                else if (child.isFile) {
                    val size = child.length().coerceAtLeast(0)
                    val item = StorageEntry(
                        uri = child.uri,
                        name = child.name ?: "Unnamed file",
                        path = child.uri.toString(),
                        sizeBytes = size,
                        modifiedAtMillis = child.lastModified().coerceAtLeast(0),
                        category = categoryFor(child.name.orEmpty(), child.type, child.uri.toString(), size, largeThresholdBytes),
                        mimeType = child.type
                    )
                    val index = entries.size
                    entries += item
                    if (size > 0) candidateSizes.getOrPut(size) { ArrayList() }.add(index)
                }
            }
        }
        // Hash only same-size candidates. Small files get a full hash; large candidates get a
        // bounded head/tail digest so a deep scan does not become a battery or memory problem.
        candidateSizes.values.filter { it.size > 1 }.forEach { indices ->
            indices.forEach { index ->
                coroutineContext.ensureActive()
                val item = entries[index]
                entries[index] = item.copy(contentHash = hashDocument(item, full = item.sizeBytes <= 64L * 1024 * 1024))
            }
            // A partial hash is only a candidate filter. Verify matching large files with a
            // full streaming SHA-256 before ever describing them as duplicates.
            indices.filter { entries[it].sizeBytes > 64L * 1024 * 1024 }
                .groupBy { entries[it].contentHash }
                .filterKeys { it != null }
                .values.filter { it.size > 1 }
                .forEach { matching ->
                    matching.forEach { index ->
                        coroutineContext.ensureActive()
                        entries[index] = entries[index].copy(contentHash = hashDocument(entries[index], full = true))
                    }
                }
            // Unmatched partial hashes cannot be used for duplicate claims.
            indices.filter { entries[it].sizeBytes > 64L * 1024 * 1024 }
                .forEach { index ->
                    if (entries[index].contentHash?.let { hash ->
                        indices.count { entries[it].contentHash == hash } == 1
                    } == true) entries[index] = entries[index].copy(contentHash = null)
                }
        }
        val duplicateGroups = DuplicateDetector.findGroups(entries.map { it.toScannedFile() })
            .flatMap { it.files.map { file -> file.path to it.contentHash }.toMap().entries }
            .associate { it.key to it.value }
        val withDuplicateCategory = entries.map { item ->
            if (item.contentHash != null && duplicateGroups[item.path] != null) item.copy(category = StorageCategory.DUPLICATE) else item
        }
        StorageReport(root.name ?: "Selected folder", withDuplicateCategory, skipped, System.currentTimeMillis())
    }

    suspend fun delete(entries: Collection<StorageEntry>): List<DeleteResult> = withContext(Dispatchers.IO) {
        entries.map { entry ->
            val deleted = runCatching {
                DocumentFile.fromSingleUri(context, entry.uri)?.delete() == true
            }.getOrDefault(false)
            DeleteResult(entry, deleted, if (!deleted) "Android did not permit deleting this item from the selected folder." else null)
        }
    }

    private fun hashDocument(item: StorageEntry, full: Boolean): String? = runCatching {
        val digest = MessageDigest.getInstance("SHA-256")
        context.contentResolver.openInputStream(item.uri)?.use { raw ->
            BufferedInputStream(raw).use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                if (full) {
                    while (true) {
                        val count = input.read(buffer)
                        if (count <= 0) break
                        digest.update(buffer, 0, count)
                    }
                } else {
                    val head = ByteArray(PARTIAL_HASH_BYTES)
                    val headCount = input.read(head)
                    if (headCount > 0) digest.update(head, 0, headCount)
                    if (item.sizeBytes > PARTIAL_HASH_BYTES) {
                        input.skip((item.sizeBytes - PARTIAL_HASH_BYTES).coerceAtLeast(0))
                        val tail = ByteArray(PARTIAL_HASH_BYTES)
                        val tailCount = input.read(tail)
                        if (tailCount > 0) digest.update(tail, 0, tailCount)
                    }
                }
            }
        } ?: return null
        digest.digest().joinToString("") { byte -> "%02x".format(byte) }
    }.getOrNull()

    private fun categoryFor(name: String, mime: String?, uri: String, sizeBytes: Long, largeThresholdBytes: Long): StorageCategory {
        val lower = name.lowercase()
        return when {
            lower.endsWith(".apk") || lower.endsWith(".xapk") -> StorageCategory.APK
            lower.endsWith(".zip") || lower.endsWith(".rar") || lower.endsWith(".7z") || lower.endsWith(".tar") || lower.endsWith(".gz") -> StorageCategory.ARCHIVE
            mime?.startsWith("image/") == true || lower.matches(Regex(".*\\.(jpg|jpeg|png|gif|webp|heic)$")) -> StorageCategory.IMAGE
            mime?.startsWith("video/") == true || lower.matches(Regex(".*\\.(mp4|mkv|mov|avi|webm)$")) -> StorageCategory.VIDEO
            mime?.startsWith("audio/") == true || lower.matches(Regex(".*\\.(mp3|m4a|wav|flac|ogg)$")) -> StorageCategory.AUDIO
            lower.contains("download") || uri.contains("download", ignoreCase = true) -> StorageCategory.DOWNLOAD
            sizeBytes >= largeThresholdBytes -> StorageCategory.LARGE
            else -> StorageCategory.OTHER
        }
    }

    private fun StorageEntry.toScannedFile() = ScannedFile(path, sizeBytes, modifiedAtMillis, contentHash)

    private companion object {
        const val PARTIAL_HASH_BYTES = 64 * 1024
    }
}
