package com.mkrinfinity.autooptimiser.storage

/**
 * Detects duplicates only from a FULL content hash supplied by a real scanner.
 * Size, filename, modification time and sample hashes are never sufficient.
 */
object DuplicateDetector {
    fun findGroups(files: Iterable<ScannedFile>, checkCancelled: () -> Unit = {}): List<DuplicateGroup> {
        val groups = linkedMapOf<Pair<Long, String>, MutableList<ScannedFile>>()
        val seen = hashSetOf<String>()
        for (file in files) {
            checkCancelled()
            if (!seen.add(file.path) || file.contentHash.isNullOrBlank()) continue
            groups.getOrPut(file.sizeBytes to file.contentHash) { arrayListOf() }.add(file)
        }
        val result = arrayListOf<DuplicateGroup>()
        for ((key, matching) in groups) {
            checkCancelled()
            if (matching.size >= 2) result += DuplicateGroup(key.second, matching.sortedBy { it.path })
        }
        checkCancelled()
        return result.sortedBy { it.canonicalFile.path }
    }

    fun reclaimableBytes(files: Iterable<ScannedFile>): Long =
        findGroups(files).sumOf(DuplicateGroup::reclaimableBytes)
}
