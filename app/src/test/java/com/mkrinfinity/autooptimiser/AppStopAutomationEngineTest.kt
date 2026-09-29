package com.mkrinfinity.autooptimiser

import com.mkrinfinity.autooptimiser.accessibility.AppStopAutomationEngine
import com.mkrinfinity.autooptimiser.accessibility.StopStep
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AppStopAutomationEngineTest {
    @Test
    fun rejectsSkippingTheConfirmationState() {
        val engine = AppStopAutomationEngine(timeoutMillis = 10)
        assertTrue(engine.enter(StopStep.OPENING_APP_INFO, 0))
        assertTrue(engine.enter(StopStep.WAITING_FOR_APP_INFO, 1))
        assertFalse(engine.enter(StopStep.VERIFYING, 2))
    }

    @Test
    fun cancellationEndsTheDeadlineAndTimeoutIsDeterministic() {
        val engine = AppStopAutomationEngine(timeoutMillis = 10)
        engine.enter(StopStep.OPENING_APP_INFO, 100)
        assertTrue(engine.timedOut(110))
        assertTrue(engine.enter(StopStep.CANCELLED, 110))
        assertFalse(engine.timedOut(1000))
    }
}
