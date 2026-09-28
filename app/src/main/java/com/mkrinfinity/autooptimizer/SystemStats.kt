package com.mkrinfinity.autooptimizer

import android.app.ActivityManager
import android.content.Context
import android.os.BatteryManager
import android.os.StatFs
import kotlin.math.roundToInt

data class DeviceSnapshot(
    val ramUsed: Long,
    val ramTotal: Long,
    val storageUsed: Long,
    val storageTotal: Long,
    val batteryPct: Int,
    val temperatureC: Float
) {
    val ramPct: Int get() = if (ramTotal == 0L) 0 else ((ramUsed * 100.0) / ramTotal).roundToInt()
    val storagePct: Int get() = if (storageTotal == 0L) 0 else ((storageUsed * 100.0) / storageTotal).roundToInt()
}

object SystemStats {
    fun snapshot(context: Context): DeviceSnapshot {
        val am = context.getSystemService(ActivityManager::class.java)
        val mem = ActivityManager.MemoryInfo()
        am.getMemoryInfo(mem)
        val total = mem.totalMem
        val used = (total - mem.availMem).coerceAtLeast(0)

        val stat = StatFs(context.filesDir.absolutePath)
        val storageTotal = stat.totalBytes
        val storageFree = stat.availableBytes

        val bm = context.getSystemService(BatteryManager::class.java)
        val pct = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY).coerceIn(0, 100)
        val intent = context.registerReceiver(null, android.content.IntentFilter(android.content.Intent.ACTION_BATTERY_CHANGED))
        val temp = (intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f

        return DeviceSnapshot(used, total, storageTotal - storageFree, storageTotal, pct, temp)
    }

    fun formatBytes(bytes: Long): String {
        val gb = bytes / 1_000_000_000.0
        val mb = bytes / 1_000_000.0
        return if (gb >= 1.0) String.format("%.1f GB", gb) else String.format("%.0f MB", mb)
    }
}
