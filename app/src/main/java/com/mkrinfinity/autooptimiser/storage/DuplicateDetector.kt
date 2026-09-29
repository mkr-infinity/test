package com.mkrinfinity.autooptimiser.storage

/**
 * Detects duplicates only from a content hash supplied by a real scanner.
 * Size, filename, and modification time alone are intentionally insufficient.
 */
object DuplicateDetector {
    fun findGroups(files: Iterable<ScannedFile>): List<DuplicateGroup> = files
        .filter { !it.contentHash.isNullOrBlank() }
        .groupBy { it.contentHash!! }
        .asSequence()
        .filter { (_, matchingFiles) -> matchingFiles.size >= 2 }
        .map { (hash, matchingFiles) -> DuplicateGroup(hash, matchingFiles.sortedBy { it.path }) }
        .sortedBy { it.canonicalFile.path }
        .toList()

    fun reclaimableBytes(files: Iterable<ScannedFile>): Long =
        findGroups(files).sumOf(DuplicateGroup::reclaimableBytes)
}
