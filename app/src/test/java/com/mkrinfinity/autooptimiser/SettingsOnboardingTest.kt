package com.mkrinfinity.autooptimiser

import com.mkrinfinity.autooptimiser.settings.InMemoryOnboardingRepository
import com.mkrinfinity.autooptimiser.settings.InMemorySettingsRepository
import com.mkrinfinity.autooptimiser.settings.OnboardingStateMachine
import com.mkrinfinity.autooptimiser.settings.OnboardingStep
import com.mkrinfinity.autooptimiser.settings.OptimisationSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SettingsOnboardingTest {
    @Test
    fun onboardingAdvancesInOrderAndCanBePersisted() {
        val repository = InMemoryOnboardingRepository()
        repeat(4) { repository.write(OnboardingStateMachine.completeCurrent(repository.read())) }

        assertEquals(OnboardingStep.COMPLETE, repository.read().currentStep)
        assertTrue(repository.read().isComplete)
    }

    @Test
    fun settingsRepositoryStoresValidatedDomainSettings() {
        val repository = InMemorySettingsRepository()
        val settings = OptimisationSettings(
            automaticOptimisationEnabled = true,
            optimiseWhenScreenTurnsOff = true,
            memoryThresholdPercent = 15,
            intervalMinutes = 60,
            clearCache = true
        )

        repository.write(settings)

        assertEquals(settings, repository.read())
    }
}
