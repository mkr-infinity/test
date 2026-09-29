package com.mkrinfinity.autooptimiser.accessibility

/**
 * Small deterministic guard around the system-facing service. The service owns node
 * inspection; this engine owns legal state changes and per-state deadlines so a stale
 * accessibility event cannot advance a queue after cancellation or timeout.
 */
class AppStopAutomationEngine(private val timeoutMillis: Long = 10_000L) {
    var state: StopStep = StopStep.IDLE
        private set
    var enteredAtMillis: Long = 0L
        private set
    var deadlineMillis: Long = 0L
        private set

    fun enter(next: StopStep, nowMillis: Long): Boolean {
        if (next == StopStep.CANCELLED && state != StopStep.COMPLETED) {
            state = next
            enteredAtMillis = nowMillis
            deadlineMillis = 0L
            return true
        }
        if (!isAllowed(state, next)) return false
        state = next
        enteredAtMillis = nowMillis
        deadlineMillis = if (next == StopStep.IDLE || next == StopStep.COMPLETED || next == StopStep.CANCELLED) 0L else nowMillis + timeoutMillis
        return true
    }

    fun timedOut(nowMillis: Long): Boolean = deadlineMillis > 0L && nowMillis >= deadlineMillis

    fun reset(nowMillis: Long = 0L) {
        state = StopStep.IDLE
        enteredAtMillis = nowMillis
        deadlineMillis = 0L
    }

    private fun isAllowed(from: StopStep, to: StopStep): Boolean = when (from) {
        StopStep.IDLE -> to == StopStep.OPENING_APP_INFO
        StopStep.OPENING_APP_INFO -> to == StopStep.WAITING_FOR_APP_INFO || to == StopStep.FAILED || to == StopStep.RETURNING
        StopStep.WAITING_FOR_APP_INFO -> to == StopStep.FINDING_STOP_BUTTON || to == StopStep.FAILED || to == StopStep.RETURNING || to == StopStep.TIMED_OUT
        StopStep.FINDING_STOP_BUTTON -> to == StopStep.WAITING_FOR_CONFIRMATION || to == StopStep.FAILED || to == StopStep.RETURNING || to == StopStep.TIMED_OUT
        StopStep.WAITING_FOR_CONFIRMATION -> to == StopStep.VERIFYING || to == StopStep.FAILED || to == StopStep.RETURNING || to == StopStep.TIMED_OUT
        StopStep.VERIFYING -> to == StopStep.RETURNING || to == StopStep.COMPLETED || to == StopStep.FAILED || to == StopStep.TIMED_OUT
        StopStep.RETURNING -> to == StopStep.OPENING_APP_INFO || to == StopStep.COMPLETED || to == StopStep.CANCELLED
        StopStep.FAILED, StopStep.TIMED_OUT -> to == StopStep.RETURNING || to == StopStep.OPENING_APP_INFO || to == StopStep.COMPLETED
        StopStep.COMPLETED, StopStep.CANCELLED -> to == StopStep.IDLE
    }
}
