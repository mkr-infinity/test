package com.mkrinfinity.autooptimiser

import com.mkrinfinity.autooptimiser.optimization.OptimizationJob
import com.mkrinfinity.autooptimiser.optimization.OptimizationJobState
import com.mkrinfinity.autooptimiser.optimization.OptimizationQueue
import com.mkrinfinity.autooptimiser.optimization.OptimizationTaskType
import com.mkrinfinity.autooptimiser.optimization.OptimizationTransition
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class OptimizationQueueTest {
    private fun job(id: String, priority: Int) = OptimizationJob(
        id = id,
        packageName = "com.example.$id",
        task = OptimizationTaskType.CLEAR_CACHE,
        createdAtEpochMillis = 10,
        priority = priority
    )

    @Test
    fun startsHighestPriorityAndRequiresRunningStateForCompletion() {
        val queue = OptimizationQueue(listOf(job("low", 1), job("high", 2)))

        val running = queue.startNext(11)
        assertNotNull(running)
        assertEquals("high", running.id)
        assertEquals(OptimizationJobState.RUNNING, running.state)

        val completed = queue.transition("high", OptimizationTransition.Succeed(12))
        assertEquals(OptimizationJobState.SUCCEEDED, completed.state)
        assertNull(queue.snapshot().runningJob)
    }

    @Test
    fun onlyOneJobRunsAndInvalidTransitionsAreRejected() {
        val queue = OptimizationQueue(listOf(job("one", 1), job("two", 1)))
        queue.startNext(11)

        assertNull(queue.startNext(12))
        assertFailsWith<IllegalArgumentException> {
            queue.transition("two", OptimizationTransition.Succeed(12))
        }
    }

    @Test
    fun cancellationPreservesQueuedWorkAsCancelledWithoutReportingSuccess() {
        val queue = OptimizationQueue(listOf(job("one", 1), job("two", 1)))
        queue.startNext(11)
        val cancelled = queue.transition("one", OptimizationTransition.Cancel(12))

        assertEquals(OptimizationJobState.CANCELLED, cancelled.state)
        assertEquals(1, queue.snapshot().pendingCount)
    }
}
