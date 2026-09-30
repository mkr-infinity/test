package com.mkrinfinity.autooptimiser.storage

import com.mkrinfinity.autooptimiser.data.StorageCategory
import java.util.Locale

/** File type is separate from size, folder ancestry, and verified duplicate status. */
object StorageClassification {
    fun typeFor(name: String, mime: String?): StorageCategory {
        val extension = name.substringAfterLast('.', "").lowercase(Locale.ROOT)
        return when {
            extension in setOf("apk", "xapk") || mime == "application/vnd.android.package-archive" -> StorageCategory.APK
            extension in setOf("zip", "rar", "7z", "tar", "gz", "bz2", "xz") -> StorageCategory.ARCHIVE
            mime?.startsWith("image/", true) == true || extension in setOf("jpg", "jpeg", "png", "gif", "webp", "heic", "heif") -> StorageCategory.IMAGE
            mime?.startsWith("video/", true) == true || extension in setOf("mp4", "mkv", "mov", "avi", "webm") -> StorageCategory.VIDEO
            mime?.startsWith("audio/", true) == true || extension in setOf("mp3", "m4a", "wav", "flac", "ogg") -> StorageCategory.AUDIO
            else -> StorageCategory.OTHER
        }
    }

    /** Only known directory ancestry counts; a filename containing "download" does not. */
    fun isDownloadFolder(name: String): Boolean = name.equals("download", true) || name.equals("downloads", true)

    fun matchesCategory(
        filter: StorageCategory,
        type: StorageCategory,
        isLarge: Boolean,
        inDownloads: Boolean,
        isDuplicateCopy: Boolean
    ): Boolean = when (filter) {
        StorageCategory.LARGE -> isLarge
        StorageCategory.DOWNLOAD -> inDownloads
        StorageCategory.DUPLICATE -> isDuplicateCopy
        else -> type == filter
    }
}
