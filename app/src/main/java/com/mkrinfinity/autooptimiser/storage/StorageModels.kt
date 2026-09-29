package com.mkrinfinity.autooptimiser.storage

/** Metadata returned by a scanner; file contents are not read by this model. */
data class ScannedFile(
    val path: String,
    val sizeBytes: Long,
    val modifiedAtEpochMillis: Long,
    val contentHash: String? = null
) {
    init {
        require(path.isNotBlank()) { "path must not be blank" }
        require(sizeBytes >= 0) { "sizeBytes must not be negative" }
        require(modifiedAtEpochMillis >= 0) { "modifiedAtEpochMillis must not be negative" }
        require(contentHash == null || contentHash.isNotBlank()) {
            "contentHash must be null or non-blank"
        }
    }
}

data class StorageScan(
    val root: String,
    val scannedAtEpochMillis: Long,
    val files: List<ScannedFile>,
    val unreadablePaths: List<String> = emptyList()
) {
    init {
        require(root.isNotBlank()) { "root must not be blank" }
        require(scannedAtEpochMillis >= 0) { "scannedAtEpochMillis must not be negative" }
        require(files.map(ScannedFile::path).toSet().size == files.size) {
            "A scan cannot contain the same path twice"
        }
        require(unreadablePaths.all(String::isNotBlank)) {
            "unreadable paths must not be blank"
        }
    }

    val totalBytes: Long
        get() = files.sumOf(ScannedFile::sizeBytes)
}

/** Platform-specific implementations can use java.io or MediaStore without changing domain logic. */
fun interface StorageScanner {
    fun scan(root: String, scannedAtEpochMillis: Long): StorageScan
}

data class DuplicateGroup(
    val contentHash: String,
    val files: List<ScannedFile>
) {
    init {
        require(contentHash.isNotBlank()) { "contentHash must not be blank" }
        require(files.size >= 2) { "A duplicate group must contain at least two files" }
        require(files.all { it.contentHash == contentHash }) {
            "Every file must have the group's content hash"
        }
    }

    val canonicalFile: ScannedFile
        get() = files.minWith(compareBy<ScannedFile> { it.path }.thenBy { it.modifiedAtEpochMillis })

    val duplicateFiles: List<ScannedFile>
        get() = files.filterNot { it.path == canonicalFile.path }

    val reclaimableBytes: Long
        get() = duplicateFiles.sumOf(ScannedFile::sizeBytes)
}
