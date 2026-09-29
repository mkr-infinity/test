package com.mkrinfinity.autooptimiser.model

/** A package snapshot supplied by an Android-specific inventory reader. */
data class AppInventoryItem(
    val packageName: String,
    val label: String,
    val versionName: String?,
    val versionCode: Long,
    val firstInstallTimeMillis: Long,
    val lastUpdateTimeMillis: Long,
    val isSystemApp: Boolean,
    val isEnabled: Boolean,
    val isLaunchable: Boolean,
    val sizeBytes: Long?
) {
    init {
        require(packageName.isNotBlank()) { "packageName must not be blank" }
        require(label.isNotBlank()) { "label must not be blank" }
        require(versionCode >= 0) { "versionCode must not be negative" }
        require(firstInstallTimeMillis >= 0) { "firstInstallTimeMillis must not be negative" }
        require(lastUpdateTimeMillis >= firstInstallTimeMillis) {
            "lastUpdateTimeMillis must not precede firstInstallTimeMillis"
        }
        require(sizeBytes == null || sizeBytes >= 0) { "sizeBytes must not be negative" }
    }
}

data class AppInventorySnapshot(
    val apps: List<AppInventoryItem>,
    val scannedAtEpochMillis: Long
) {
    init {
        require(scannedAtEpochMillis >= 0) { "scannedAtEpochMillis must not be negative" }
        require(apps.map(AppInventoryItem::packageName).toSet().size == apps.size) {
            "An inventory snapshot cannot contain duplicate package names"
        }
    }

    fun find(packageName: String): AppInventoryItem? =
        apps.firstOrNull { it.packageName == packageName }

    companion object {
        fun empty(scannedAtEpochMillis: Long = 0L): AppInventorySnapshot =
            AppInventorySnapshot(emptyList(), scannedAtEpochMillis)
    }
}

/** In-memory repository useful to the data layer and deterministic tests. */
class AppInventoryStore(initial: Iterable<AppInventoryItem> = emptyList()) {
    private val items = linkedMapOf<String, AppInventoryItem>()

    init {
        initial.forEach(::upsert)
    }

    fun upsert(item: AppInventoryItem) {
        items[item.packageName] = item
    }

    fun remove(packageName: String): Boolean = items.remove(packageName) != null

    fun get(packageName: String): AppInventoryItem? = items[packageName]

    fun snapshot(scannedAtEpochMillis: Long): AppInventorySnapshot =
        AppInventorySnapshot(
            apps = items.values.sortedBy { it.packageName },
            scannedAtEpochMillis = scannedAtEpochMillis
        )

    fun replaceAll(newItems: Iterable<AppInventoryItem>) {
        val replacement = linkedMapOf<String, AppInventoryItem>()
        newItems.forEach { replacement[it.packageName] = it }
        items.clear()
        items.putAll(replacement)
    }
}
