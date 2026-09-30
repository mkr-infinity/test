package com.mkrinfinity.autooptimiser.data

import android.content.Context
import android.net.Uri
import com.mkrinfinity.autooptimiser.settings.OptimisationSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

enum class ThemeMode { SYSTEM, LIGHT, DARK }

data class LastOptimisation(
    val atMillis: Long,
    val attempted: Int,
    val successful: Int,
    val skipped: Int,
    val failed: Int
)

class PreferencesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("auto_optimiser_preferences", Context.MODE_PRIVATE)
    private val mutableTheme = MutableStateFlow(readTheme())
    val theme: StateFlow<ThemeMode> = mutableTheme

    val onboardingComplete: Boolean get() = prefs.getBoolean(KEY_ONBOARDING, false)
    val selectedTreeUri: Uri? get() = prefs.getString(KEY_TREE_URI, null)?.let(Uri::parse)
    val protectedPackages: Set<String> get() = prefs.getStringSet(KEY_PROTECTED, emptySet()).orEmpty()
    val largeThresholdBytes: Long get() = prefs.getLong(KEY_THRESHOLD, 500L * 1024 * 1024)
    val automaticOptimisation: Boolean get() = prefs.getBoolean(KEY_AUTOMATIC, false)
    val automaticFrequency: String get() = prefs.getString(KEY_FREQUENCY, "weekly") ?: "weekly"
    val confirmationRequired: Boolean get() = prefs.getBoolean(KEY_CONFIRMATION, true)
    val settings: OptimisationSettings
        get() = OptimisationSettings(automaticOptimisationEnabled = automaticOptimisation)

    fun setOnboardingComplete(value: Boolean) = prefs.edit().putBoolean(KEY_ONBOARDING, value).apply()
    fun setTreeUri(uri: Uri?) {
        if (uri == null) prefs.edit().remove(KEY_TREE_URI).apply()
        else {
            prefs.edit().putString(KEY_TREE_URI, uri.toString()).apply()
        }
    }
    fun setTheme(value: ThemeMode) {
        prefs.edit().putString(KEY_THEME, value.name).apply()
        mutableTheme.value = value
    }
    fun toggleProtected(packageName: String, protect: Boolean) {
        val next = protectedPackages.toMutableSet().apply { if (protect) add(packageName) else remove(packageName) }
        prefs.edit().putStringSet(KEY_PROTECTED, next).apply()
    }
    fun setLargeThresholdBytes(value: Long) = prefs.edit().putLong(KEY_THRESHOLD, value).apply()
    fun setAutomatic(value: Boolean) = prefs.edit().putBoolean(KEY_AUTOMATIC, value).apply()
    fun setAutomaticFrequency(value: String) = prefs.edit().putString(KEY_FREQUENCY, value).apply()
    fun setConfirmationRequired(value: Boolean) = prefs.edit().putBoolean(KEY_CONFIRMATION, value).apply()
    fun setLastOptimisation(value: LastOptimisation?) {
        val edit = prefs.edit()
        if (value == null) edit.remove(KEY_LAST_AT)
        else edit.putLong(KEY_LAST_AT, value.atMillis)
            .putInt(KEY_LAST_ATTEMPTED, value.attempted)
            .putInt(KEY_LAST_SUCCESSFUL, value.successful)
            .putInt(KEY_LAST_SKIPPED, value.skipped)
            .putInt(KEY_LAST_FAILED, value.failed)
        edit.apply()
    }
    fun getLastOptimisation(): LastOptimisation? {
        val at = prefs.getLong(KEY_LAST_AT, 0)
        if (at <= 0) return null
        return LastOptimisation(at, prefs.getInt(KEY_LAST_ATTEMPTED, 0), prefs.getInt(KEY_LAST_SUCCESSFUL, 0), prefs.getInt(KEY_LAST_SKIPPED, 0), prefs.getInt(KEY_LAST_FAILED, 0))
    }

    private fun readTheme(): ThemeMode = runCatching {
        ThemeMode.valueOf(prefs.getString(KEY_THEME, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name)
    }.getOrDefault(ThemeMode.SYSTEM)

    private companion object {
        const val KEY_ONBOARDING = "onboarding_complete"
        const val KEY_TREE_URI = "storage_tree_uri"
        const val KEY_PROTECTED = "protected_packages"
        const val KEY_THEME = "theme_mode"
        const val KEY_THRESHOLD = "large_threshold"
        const val KEY_AUTOMATIC = "automatic_optimisation"
        const val KEY_FREQUENCY = "automatic_frequency"
        const val KEY_CONFIRMATION = "confirmation_required"
        const val KEY_LAST_AT = "last_optimisation_at"
        const val KEY_LAST_ATTEMPTED = "last_optimisation_attempted"
        const val KEY_LAST_SUCCESSFUL = "last_optimisation_successful"
        const val KEY_LAST_SKIPPED = "last_optimisation_skipped"
        const val KEY_LAST_FAILED = "last_optimisation_failed"
    }
}
