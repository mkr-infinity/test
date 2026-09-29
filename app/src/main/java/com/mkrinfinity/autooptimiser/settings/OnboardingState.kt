package com.mkrinfinity.autooptimiser.settings

enum class OnboardingStep {
    WELCOME,
    APP_INVENTORY,
    PROTECTED_APPS,
    ACCESSIBILITY,
    COMPLETE
}

data class OnboardingState(
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val completedSteps: Set<OnboardingStep> = emptySet()
) {
    init {
        require(OnboardingStep.COMPLETE !in completedSteps) {
            "COMPLETE is represented by currentStep, not completedSteps"
        }
        require(currentStep == OnboardingStep.WELCOME ||
            OnboardingStep.values().take(currentStep.ordinal).all { it in completedSteps }
        ) {
            "Steps before currentStep must be completed"
        }
    }

    val isComplete: Boolean
        get() = currentStep == OnboardingStep.COMPLETE
}

object OnboardingStateMachine {
    fun completeCurrent(state: OnboardingState): OnboardingState {
        if (state.isComplete) return state
        val completed = state.completedSteps + state.currentStep
        val next = OnboardingStep.values().getOrNull(state.currentStep.ordinal + 1)
            ?: OnboardingStep.COMPLETE
        return OnboardingState(next, completed.filter { it != OnboardingStep.COMPLETE }.toSet())
    }

    fun reset(): OnboardingState = OnboardingState()
}

interface OnboardingRepository {
    fun read(): OnboardingState
    fun write(state: OnboardingState)
}

class InMemoryOnboardingRepository(
    initial: OnboardingState = OnboardingState()
) : OnboardingRepository {
    private var value = initial

    override fun read(): OnboardingState = value

    override fun write(state: OnboardingState) {
        value = state
    }
}
