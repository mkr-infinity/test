package com.mkrinfinity.autooptimiser.accessibility

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class StopDeadlineTest {
    @Test fun repeatedContentCannotRenewTheDeadline() {
        val engine = AppStopAutomationEngine(10)
        assertTrue(engine.enter(StopStep.OPENING_APP_INFO, 0))
        assertTrue(engine.enter(StopStep.WAITING_FOR_APP_INFO, 1))
        assertTrue(engine.enter(StopStep.FINDING_STOP_BUTTON, 2))
        repeat(10) { assertFalse(engine.enter(StopStep.FINDING_STOP_BUTTON, 3L + it)) }
        assertEquals(2L, engine.enteredAtMillis)
        assertEquals(12L, engine.deadlineMillis)
        assertTrue(engine.timedOut(12))
        assertFalse(engine.enter(StopStep.WAITING_FOR_CONFIRMATION, 12))
        assertTrue(engine.enter(StopStep.RETURNING, 12))
    }

    @Test fun cancelledEngineRejectsDelayedActionsAndQueueAdvancement() {
        val engine = AppStopAutomationEngine(10)
        engine.enter(StopStep.OPENING_APP_INFO, 0)
        engine.enter(StopStep.WAITING_FOR_APP_INFO, 1)
        engine.enter(StopStep.FINDING_STOP_BUTTON, 2)
        engine.enter(StopStep.WAITING_FOR_CONFIRMATION, 3)
        assertTrue(engine.enter(StopStep.CANCELLED, 4))
        assertFalse(engine.enter(StopStep.VERIFYING, 5))
        assertFalse(engine.enter(StopStep.OPENING_APP_INFO, 6))
        assertFalse(engine.enter(StopStep.RETURNING, 7))
        assertFalse(engine.timedOut(100))
    }

    @Test fun finishedItemCannotFinishTwiceAndNextItemMustStartAtOpening() {
        val engine = AppStopAutomationEngine(10)
        engine.enter(StopStep.OPENING_APP_INFO, 0)
        engine.enter(StopStep.WAITING_FOR_APP_INFO, 1)
        assertTrue(engine.enter(StopStep.RETURNING, 2))
        assertFalse(engine.enter(StopStep.RETURNING, 3))
        assertFalse(engine.enter(StopStep.FINDING_STOP_BUTTON, 3))
        assertTrue(engine.enter(StopStep.OPENING_APP_INFO, 3))
    }

    @Test fun timeoutDoesNotAllowLateConfirmationButAlwaysAllowsCancellation() {
        val engine = AppStopAutomationEngine(10)
        engine.enter(StopStep.OPENING_APP_INFO, 0)
        engine.enter(StopStep.WAITING_FOR_APP_INFO, 1)
        engine.enter(StopStep.FINDING_STOP_BUTTON, 2)
        engine.enter(StopStep.WAITING_FOR_CONFIRMATION, 3)
        assertFalse(engine.enter(StopStep.VERIFYING, 13))
        assertTrue(engine.enter(StopStep.CANCELLED, 13))
        assertEquals(0L, engine.deadlineMillis)
    }
}
