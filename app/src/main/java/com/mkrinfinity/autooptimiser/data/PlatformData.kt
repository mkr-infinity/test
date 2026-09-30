package com.mkrinfinity.autooptimiser.data

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.core.content.pm.PackageInfoCompat
import com.mkrinfinity.autooptimiser.model.AppInventoryItem
import com.mkrinfinity.autooptimiser.model.ProtectedApp
import com.mkrinfinity.autooptimiser.model.ProtectionReason
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.ensureActive
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/** A UI-ready app record. Icon loading is done once during a repository refresh. */
data class AppRecord(
    val inventory: AppInventoryItem,
    val icon: android.graphics.drawable.Drawable,
    val isRunning: Boolean,
    val isProtected: Boolean,
    val protectionReason: ProtectionReason?
) {
    val packageName get() = inventory.packageName
    val label get() = inventory.label
}

enum class AppFilter { ALL, USER, SYSTEM, RUNNING, PROTECTED }
enum class AppSort { NAME, SIZE, UPDATED }

data class DeviceStatus(
    val memoryTotalBytes: Long,
    val memoryAvailableBytes: Long,
    val storageTotalBytes: Long,
    val storageAvailableBytes: Long,
    val batteryPercent: Int?,
    val isCharging: Boolean,
    val batteryTemperatureCelsius: Float?,
    val batteryHealth: String?,
    val batteryOptimisationIgnored: Boolean,
    val measuredAtMillis: Long,
    val batteryCurrentMicroamps: Int? = null,
    val remainingChargeMicroampHours: Int? = null,
    val batterySaverEnabled: Boolean = false
) {
    val memoryUsedBytes: Long get() = (memoryTotalBytes - memoryAvailableBytes).coerceAtLeast(0)
    val memoryUsedPercent: Int get() = if (memoryTotalBytes == 0L) 0 else (memoryUsedBytes * 100 / memoryTotalBytes).toInt()
    val storageUsedBytes: Long get() = (storageTotalBytes - storageAvailableBytes).coerceAtLeast(0)
    val storageUsedPercent: Int get() = if (storageTotalBytes == 0L) 0 else (storageUsedBytes * 100 / storageTotalBytes).toInt()
}

class AppRepository(private val app: Application) {
    private val packageManager = app.packageManager
    private val inventoryCache = linkedMapOf<String, AppRecord>()

    suspend fun refresh(protectedPackages: Map<String, ProtectedApp>): List<AppRecord> = withContext(Dispatchers.IO) {
        val scanContext = coroutineContext
        val runningPackages = runningPackages()
        val enabledAccessibilityPackages = enabledAccessibilityPackages()
        val launcherPackage = launcherPackage()
        val ownPackage = app.packageName
        val defaultKeyboardPackage = Settings.Secure.getString(app.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            ?.substringBefore('/')
        val records = packageManager.getInstalledApplications(PackageManager.MATCH_ALL)
            .asSequence()
            .filter { it.packageName != "android" }
            .mapNotNull { info ->
                scanContext.ensureActive()
                runCatching {
                    val packageInfo = packageManager.getPackageInfo(info.packageName, 0)
                    val isSystem = info.flags and ApplicationInfo.FLAG_SYSTEM != 0 ||
                        info.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP != 0
                    val userProtection = protectedPackages[info.packageName]
                    val critical = when {
                        info.packageName == ownPackage -> ProtectionReason.SYSTEM_CRITICAL
                        info.packageName == launcherPackage -> ProtectionReason.SYSTEM_CRITICAL
                        info.packageName == "com.android.systemui" -> ProtectionReason.SYSTEM_CRITICAL
                        defaultKeyboardPackage == info.packageName -> ProtectionReason.SYSTEM_CRITICAL
                        enabledAccessibilityPackages.contains(info.packageName) -> ProtectionReason.SYSTEM_CRITICAL
                        isSystem -> ProtectionReason.SYSTEM_CRITICAL
                        else -> userProtection?.reason
                    }
                    AppRecord(
                        inventory = AppInventoryItem(
                            packageName = info.packageName,
                            label = info.loadLabel(packageManager).toString().ifBlank { info.packageName },
                            versionName = packageInfo.versionName,
                            versionCode = PackageInfoCompat.getLongVersionCode(packageInfo),
                            firstInstallTimeMillis = packageInfo.firstInstallTime.coerceAtLeast(0),
                            lastUpdateTimeMillis = packageInfo.lastUpdateTime.coerceAtLeast(packageInfo.firstInstallTime),
                            isSystemApp = isSystem,
                            isEnabled = info.enabled,
                            isLaunchable = packageManager.getLaunchIntentForPackage(info.packageName) != null,
                            sizeBytes = (listOfNotNull(info.sourceDir) + info.splitSourceDirs.orEmpty()).sumOf { java.io.File(it).length() }.takeIf { it > 0 }
                        ),
                        icon = cached(info.packageName)?.takeIf { it.inventory.lastUpdateTimeMillis == packageInfo.lastUpdateTime }?.icon ?: info.loadIcon(packageManager),
                        isRunning = runningPackages.contains(info.packageName),
                        isProtected = critical != null,
                        protectionReason = critical
                    )
                }.getOrNull()
            }
            .sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.label })
            .toList()
        synchronized(inventoryCache) {
            inventoryCache.clear()
            records.forEach { inventoryCache[it.packageName] = it }
        }
        records
    }

    fun cached(packageName: String): AppRecord? = synchronized(inventoryCache) { inventoryCache[packageName] }

    private fun runningPackages(): Set<String> {
        val manager = app.getSystemService(ActivityManager::class.java) ?: return emptySet()
        return manager.runningAppProcesses.orEmpty()
            .flatMap { it.pkgList?.asList().orEmpty() }
            .toSet()
    }

    private fun enabledAccessibilityPackages(): Set<String> = runCatching {
        val manager = app.getSystemService(android.view.accessibility.AccessibilityManager::class.java)
        manager?.getEnabledAccessibilityServiceList(android.accessibilityservice.AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
            .orEmpty()
            .mapNotNull { it.resolveInfo?.serviceInfo?.packageName }
            .toSet()
    }.getOrDefault(emptySet())

    private fun launcherPackage(): String? = runCatching {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo?.packageName
    }.getOrNull()
}

class DeviceRepository(private val app: Context) {
    suspend fun read(): DeviceStatus = withContext(Dispatchers.IO) {
        val memory = ActivityManager.MemoryInfo()
        app.getSystemService(ActivityManager::class.java)?.getMemoryInfo(memory)
        val stat = StatFs(Environment.getDataDirectory().absolutePath)
        val battery = app.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val percent = battery?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            if (level >= 0 && scale > 0) (level * 100 / scale.toFloat()).roundToInt() else null
        }
        val health = battery?.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)?.let {
            when (it) {
                BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheating"
                BatteryManager.BATTERY_HEALTH_DEAD -> "Unavailable"
                BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over-voltage"
                BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Unavailable"
                else -> null
            }
        }
        val powerManager = app.getSystemService(android.os.PowerManager::class.java)
        val batteryManager = app.getSystemService(BatteryManager::class.java)
        DeviceStatus(
            memoryTotalBytes = memory.totalMem,
            memoryAvailableBytes = memory.availMem,
            storageTotalBytes = stat.blockCountLong * stat.blockSizeLong,
            storageAvailableBytes = stat.availableBlocksLong * stat.blockSizeLong,
            batteryPercent = percent,
            isCharging = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) in setOf(
                BatteryManager.BATTERY_STATUS_CHARGING,
                BatteryManager.BATTERY_STATUS_FULL
            ),
            batteryTemperatureCelsius = battery?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
                ?.takeIf { it != Int.MIN_VALUE }
                ?.div(10f),
            batteryHealth = health,
            batteryOptimisationIgnored = powerManager?.isIgnoringBatteryOptimizations(app.packageName) == true,
            measuredAtMillis = System.currentTimeMillis(),
            batteryCurrentMicroamps = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                ?.takeUnless { it == Int.MIN_VALUE },
            remainingChargeMicroampHours = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                ?.takeIf { it > 0 },
            batterySaverEnabled = powerManager?.isPowerSaveMode == true
        )
    }

    fun formatBytes(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val units = arrayOf("KB", "MB", "GB", "TB")
        var value = bytes.toDouble()
        var unit = -1
        while (value >= 1024 && unit < units.lastIndex) {
            value /= 1024
            unit++
        }
        return String.format(Locale.getDefault(), "%.1f %s", value, units[unit])
    }
}

fun formatTimestamp(millis: Long): String = DateFormat.getDateTimeInstance(
    DateFormat.MEDIUM,
    DateFormat.SHORT,
    Locale.getDefault()
).format(Date(millis))
