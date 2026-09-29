package com.mkrinfinity.autooptimiser.settings

data class OptimisationSettings(
    val automaticOptimisationEnabled: Boolean = false,
    val optimiseWhenScreenTurnsOff: Boolean = false,
    val memoryThresholdPercent: Int = 20,
    val intervalMinutes: Long? = null,
    val clearCache: Boolean = false,
    val clearHistory: Boolean = false,
    val batterySaverEnabled: Boolean = false
) {
    init {
        require(memoryThresholdPercent in 1..99) { "memoryThresholdPercent must be between 1 and 99" }
        require(intervalMinutes == null || intervalMinutes > 0) {
            "intervalMinutes must be positive when set"
        }
        require(!optimiseWhenScreenTurnsOff || automaticOptimisationEnabled) {
            "screen-off optimisation requires automatic optimisation"
        }
    }
}

/** US spelling alias for callers that use Android's conventional terminology. */
typealias OptimizationSettings = OptimisationSettings

interface SettingsRepository {
    fun read(): OptimisationSettings
    fun write(settings: OptimisationSettings)
}

class InMemorySettingsRepository(
    initial: OptimisationSettings = OptimisationSettings()
) : SettingsRepository {
    private var value = initial

    override fun read(): OptimisationSettings = value

    override fun write(settings: OptimisationSettings) {
        value = settings
    }
}
