package com.mkrinfinity.autooptimiser.accessibility

import android.app.admin.DevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.telecom.TelecomManager
import android.view.accessibility.AccessibilityManager
import android.accessibilityservice.AccessibilityServiceInfo
import com.mkrinfinity.autooptimiser.data.PreferencesRepository

/** Read current platform/protection state, not the potentially stale inventory shown in the UI. */
internal class StopTargetGuard(private val context: Context) {
    private val pm get() = context.packageManager
    private val preferences = PreferencesRepository(context)

    fun rejection(packageName: String): String? = try {
        when {
            !PACKAGE.matches(packageName) -> "Invalid package identity"
            packageName == context.packageName || packageName == "android" ||
                packageName == "com.android.systemui" -> "Core application is protected"
            packageName in preferences.protectedPackages -> "Application is protected by the user"
            else -> platformRejection(packageName)
        }
    } catch (_: Exception) {
        // Includes removed packages, unreadable protection state and platform security exceptions.
        "Could not safely revalidate application protection"
    }

    @Suppress("DEPRECATION")
    private fun platformRejection(packageName: String): String? {
        val info = pm.getApplicationInfo(packageName, 0)
        if (info.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0) {
            return "System applications are protected"
        }
        if (!info.enabled) return "Application is disabled"
        val home = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
        // Protect all launcher candidates, including when there is no selected default.
        if (pm.queryIntentActivities(home, PackageManager.MATCH_DEFAULT_ONLY).any {
                it.activityInfo.packageName == packageName
            }) return "Home applications are protected"
        val inputMethods = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_INPUT_METHODS)
            .orEmpty().split(':').map { it.substringBefore(';').substringBefore('/') }.toSet()
        val defaultInput = Settings.Secure.getString(context.contentResolver, Settings.Secure.DEFAULT_INPUT_METHOD)
            ?.substringBefore('/')
        if (packageName in inputMethods || packageName == defaultInput) return "Input methods are protected"
        val accessibility = requireNotNull(context.getSystemService(AccessibilityManager::class.java))
        if (accessibility.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK).any {
                it.resolveInfo.serviceInfo.packageName == packageName
            }) return "Enabled accessibility applications are protected"
        val admins = requireNotNull(context.getSystemService(DevicePolicyManager::class.java))
        if (admins.activeAdmins.orEmpty().any { it.packageName == packageName }) {
            return "Device administration applications are protected"
        }
        if (packageName == context.getSystemService(TelecomManager::class.java)?.defaultDialerPackage ||
            packageName == android.provider.Telephony.Sms.getDefaultSmsPackage(context)) {
            return "Default communication applications are protected"
        }
        val assistants = pm.queryIntentActivities(Intent(Intent.ACTION_ASSIST), PackageManager.MATCH_DEFAULT_ONLY)
        if (assistants.any { it.activityInfo.packageName == packageName }) {
            return "Assistant applications are protected"
        }
        return null
    }

    @Suppress("DEPRECATION")
    fun item(packageName: String): StopItem? = runCatching {
        val info = pm.getApplicationInfo(packageName, 0)
        StopItem(packageName, info.loadLabel(pm).toString())
    }.getOrNull()

    @Suppress("DEPRECATION")
    fun isStopped(packageName: String): Boolean? = runCatching {
        pm.getApplicationInfo(packageName, 0).flags and ApplicationInfo.FLAG_STOPPED != 0
    }.getOrNull()

    /** Exact known owner AND system provenance AND actual resolution of the App Info intent. */
    @Suppress("DEPRECATION")
    fun settingsComponent(intent: Intent): ComponentName? = runCatching {
        val resolved = pm.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)?.activityInfo
            ?: return@runCatching null
        if (resolved.packageName !in SETTINGS_PACKAGES || !resolved.exported || !resolved.enabled ||
            resolved.applicationInfo.flags and
            (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) == 0) return@runCatching null
        ComponentName(resolved.packageName, resolved.name)
    }.getOrNull()

    companion object {
        private val PACKAGE = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
        // Other OEM controllers need their own audited semantic layouts before being enabled.
        private val SETTINGS_PACKAGES = setOf("com.android.settings", "com.samsung.android.settings")
    }
}
