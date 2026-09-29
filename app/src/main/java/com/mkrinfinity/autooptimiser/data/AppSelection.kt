package com.mkrinfinity.autooptimiser.data

/** Pure, deterministic filtering used by the app list and unit tests. */
object AppSelectionLogic {
    fun filter(
        records: Iterable<AppRecord>,
        query: String,
        filter: AppFilter,
        sort: AppSort
    ): List<AppRecord> {
        val searched = records.filter { it.label.contains(query, true) || it.packageName.contains(query, true) }
        val filtered = when (filter) {
            AppFilter.ALL -> searched
            AppFilter.USER -> searched.filterNot { it.inventory.isSystemApp }
            AppFilter.SYSTEM -> searched.filter { it.inventory.isSystemApp }
            AppFilter.RUNNING -> searched.filter { it.isRunning }
            AppFilter.PROTECTED -> searched.filter { it.isProtected }
        }
        return when (sort) {
            AppSort.NAME -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            AppSort.SIZE -> filtered.sortedByDescending { it.inventory.sizeBytes ?: 0L }
            AppSort.UPDATED -> filtered.sortedByDescending { it.inventory.lastUpdateTimeMillis }
        }
    }
}
