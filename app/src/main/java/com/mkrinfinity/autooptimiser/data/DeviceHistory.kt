package com.mkrinfinity.autooptimiser.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Bounded local observations, not a reconstructed continuous battery history. */
data class DeviceObservation(
    val atMillis: Long,
    val batteryPercent: Int?,
    val charging: Boolean,
    val freeStorageBytes: Long,
    val availableMemoryBytes: Long
)

class DeviceHistory(context: Context) {
    private val preferences = context.getSharedPreferences("device_observations", Context.MODE_PRIVATE)

    fun read(): List<DeviceObservation> = synchronized(lock) { readUnlocked() }

    fun record(status: DeviceStatus) = synchronized(lock) {
        val recent = readUnlocked().filter { status.measuredAtMillis - it.atMillis > 60_000 }
        val observations = (listOf(DeviceObservation(status.measuredAtMillis, status.batteryPercent,
            status.isCharging, status.storageAvailableBytes, status.memoryAvailableBytes)) + recent).take(48)
        val json = JSONArray()
        observations.forEach { item ->
            json.put(JSONObject().put("at", item.atMillis).put("battery", item.batteryPercent ?: -1)
                .put("charging", item.charging).put("storage", item.freeStorageBytes)
                .put("memory", item.availableMemoryBytes))
        }
        preferences.edit().putString("snapshots", json.toString()).apply()
    }

    fun clear() = synchronized(lock) { preferences.edit().clear().apply() }

    private fun readUnlocked(): List<DeviceObservation> = runCatching {
        val json = JSONArray(preferences.getString("snapshots", "[]"))
        (0 until minOf(json.length(), 48)).map { index ->
            val item = json.getJSONObject(index)
            DeviceObservation(item.getLong("at"), item.optInt("battery", -1).takeIf { it in 0..100 },
                item.optBoolean("charging"), item.getLong("storage"), item.getLong("memory"))
        }
    }.getOrDefault(emptyList())

    private companion object {
        val lock = Any()
    }
}
