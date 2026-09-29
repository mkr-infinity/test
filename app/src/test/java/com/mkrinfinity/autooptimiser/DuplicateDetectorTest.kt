package com.mkrinfinity.autooptimiser

import com.mkrinfinity.autooptimiser.storage.DuplicateDetector
import com.mkrinfinity.autooptimiser.storage.ScannedFile
import kotlin.test.Test
import kotlin.test.assertEquals

class DuplicateDetectorTest {
    private fun file(path: String, size: Long, hash: String? = null) = ScannedFile(
        path = path,
        sizeBytes = size,
        modifiedAtEpochMillis = 1,
        contentHash = hash
    )

    @Test
    fun groupsOnlyFilesWithAnExactSuppliedContentHash() {
        val files = listOf(
            file("/music/a.mp3", 20, "abc"),
            file("/music/b.mp3", 20, "abc"),
            file("/music/c.mp3", 20, null),
            file("/music/d.mp3", 20, "different")
        )

        val groups = DuplicateDetector.findGroups(files)

        assertEquals(1, groups.size)
        assertEquals("/music/a.mp3", groups.single().canonicalFile.path)
        assertEquals(20L, groups.single().reclaimableBytes)
        assertEquals(20L, DuplicateDetector.reclaimableBytes(files))
    }

    @Test
    fun sameSizeFilesWithoutHashesAreNotReportedAsDuplicates() {
        assertEquals(
            emptyList(),
            DuplicateDetector.findGroups(listOf(file("/a", 4), file("/b", 4)))
        )
    }
}
