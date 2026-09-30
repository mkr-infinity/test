package com.mkrinfinity.autooptimiser

import com.mkrinfinity.autooptimiser.data.StorageCategory
import com.mkrinfinity.autooptimiser.storage.StorageClassification
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StorageClassificationTest {
    @Test
    fun largeDownloadedDuplicateImageMatchesAllFourIndependentFacets() {
        val type = StorageClassification.typeFor("holiday.JPG", "image/jpeg")
        assertEquals(StorageCategory.IMAGE, type)
        listOf(StorageCategory.IMAGE, StorageCategory.LARGE, StorageCategory.DOWNLOAD, StorageCategory.DUPLICATE).forEach {
            assertTrue(StorageClassification.matchesCategory(it, type, isLarge = true, inDownloads = true, isDuplicateCopy = true))
        }
        assertFalse(StorageClassification.matchesCategory(StorageCategory.VIDEO, type, true, true, true))
    }

    @Test
    fun canonicalCopyIsNotADuplicateFilterMatch() {
        assertFalse(StorageClassification.matchesCategory(StorageCategory.DUPLICATE, StorageCategory.IMAGE, true, true, false))
    }

    @Test
    fun downloadsAreDirectoryAncestryNotSubstringMatches() {
        assertTrue(StorageClassification.isDownloadFolder("Downloads"))
        assertTrue(StorageClassification.isDownloadFolder("DOWNLOAD"))
        assertFalse(StorageClassification.isDownloadFolder("download-report.pdf"))
        assertFalse(StorageClassification.isDownloadFolder("my-downloads-backup"))
        assertEquals(StorageCategory.OTHER, StorageClassification.typeFor("download-report.pdf", "application/pdf"))
    }

    @Test
    fun extensionAndMimeTypesRemainAvailableIndependentlyOfSize() {
        assertEquals(StorageCategory.APK, StorageClassification.typeFor("installer", "application/vnd.android.package-archive"))
        assertEquals(StorageCategory.ARCHIVE, StorageClassification.typeFor("backup.ZIP", null))
        assertEquals(StorageCategory.AUDIO, StorageClassification.typeFor("recording", "audio/wav"))
        assertEquals(StorageCategory.VIDEO, StorageClassification.typeFor("movie.MKV", null))
    }
}
