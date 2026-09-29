package com.mkrinfinity.autooptimiser.optimization

import java.util.UUID

enum class OptimizationTaskType {
    RELEASE_MEMORY,
    CLEAR_CACHE,
    CLEAR_HISTORY,
    RESTART_APP
}

enum class OptimizationJobState {
    QUEUED,
    RUNNING,
    SUCCEEDED,
    FAILED,
    CANCELLED
}

data class OptimizationJob(
    val id: String = UUID.randomUUID().toString(),
    val packageName: String?,
    val task: OptimizationTaskType,
    val createdAtEpochMillis: Long,
    val priority: Int = 0,
    val state: OptimizationJobState = OptimizationJobState.QUEUED,
    val completedAtEpochMillis: Long? = null,
    val errorMessage: String? = null
) {
    init {
        require(id.isNotBlank()) { "id must not be blank" }
        require(packageName == null || packageName.isNotBlank()) {
            "packageName must be blank or null"
        }
        require(createdAtEpochMillis >= 0) { "createdAtEpochMillis must not be negative" }
        require(completedAtEpochMillis == null || completedAtEpochMillis >= createdAtEpochMillis) {
            "completedAtEpochMillis must not precede createdAtEpochMillis"
        }
        require(state != OptimizationJobState.FAILED || !errorMessage.isNullOrBlank()) {
            "failed jobs must include an error message"
        }
    }
}

data class OptimizationQueueSnapshot(val jobs: List<OptimizationJob>) {
    init {
        require(jobs.map(OptimizationJob::id).toSet().size == jobs.size) {
            "Queue jobs must have unique ids"
        }
    }

    val pendingCount: Int
        get() = jobs.count { it.state == OptimizationJobState.QUEUED }

    val runningJob: OptimizationJob?
        get() = jobs.singleOrNull { it.state == OptimizationJobState.RUNNING }
}

sealed class OptimizationTransition {
    data class Start(val atEpochMillis: Long) : OptimizationTransition()
    data class Succeed(val atEpochMillis: Long) : OptimizationTransition()
    data class Fail(val atEpochMillis: Long, val message: String) : OptimizationTransition()
    data class Cancel(val atEpochMillis: Long) : OptimizationTransition()
}

/** Pure state transition rules for one optimization job. */
object OptimizationStateMachine {
    fun transition(job: OptimizationJob, transition: OptimizationTransition): OptimizationJob {
        return when (transition) {
            is OptimizationTransition.Start -> {
                require(job.state == OptimizationJobState.QUEUED) { "Only queued jobs can start" }
                require(transition.atEpochMillis >= job.createdAtEpochMillis) {
                    "start time must not precede creation time"
                }
                job.copy(state = OptimizationJobState.RUNNING)
            }

            is OptimizationTransition.Succeed -> {
                require(job.state == OptimizationJobState.RUNNING) { "Only running jobs can succeed" }
                require(transition.atEpochMillis >= job.createdAtEpochMillis) {
                    "completion time must not precede creation time"
                }
                job.copy(
                    state = OptimizationJobState.SUCCEEDED,
                    completedAtEpochMillis = transition.atEpochMillis,
                    errorMessage = null
                )
            }

            is OptimizationTransition.Fail -> {
                require(job.state == OptimizationJobState.RUNNING) { "Only running jobs can fail" }
                require(transition.atEpochMillis >= job.createdAtEpochMillis) {
                    "completion time must not precede creation time"
                }
                require(transition.message.isNotBlank()) { "failure message must not be blank" }
                job.copy(
                    state = OptimizationJobState.FAILED,
                    completedAtEpochMillis = transition.atEpochMillis,
                    errorMessage = transition.message
                )
            }

            is OptimizationTransition.Cancel -> {
                require(job.state == OptimizationJobState.QUEUED || job.state == OptimizationJobState.RUNNING) {
                    "Only queued or running jobs can be cancelled"
                }
                require(transition.atEpochMillis >= job.createdAtEpochMillis) {
                    "cancellation time must not precede creation time"
                }
                job.copy(
                    state = OptimizationJobState.CANCELLED,
                    completedAtEpochMillis = transition.atEpochMillis,
                    errorMessage = null
                )
            }
        }
    }
}

/** Small FIFO-by-priority queue. Execution is deliberately delegated to a platform worker. */
class OptimizationQueue(initial: Iterable<OptimizationJob> = emptyList()) {
    private val jobs = linkedMapOf<String, OptimizationJob>()

    init {
        initial.forEach { job ->
            require(jobs.put(job.id, job) == null) { "Duplicate job id: ${job.id}" }
        }
        require(jobs.values.count { it.state == OptimizationJobState.RUNNING } <= 1) {
            "Only one job may be running at a time"
        }
    }

    fun enqueue(job: OptimizationJob): Boolean {
        if (jobs.containsKey(job.id)) return false
        jobs[job.id] = job
        return true
    }

    fun nextQueued(): OptimizationJob? = jobs.values
        .asSequence()
        .filter { it.state == OptimizationJobState.QUEUED }
        .sortedWith(compareByDescending<OptimizationJob> { it.priority }.thenBy { it.createdAtEpochMillis })
        .firstOrNull()

    fun startNext(atEpochMillis: Long): OptimizationJob? {
        if (jobs.values.any { it.state == OptimizationJobState.RUNNING }) return null
        val next = nextQueued() ?: return null
        return transition(next.id, OptimizationTransition.Start(atEpochMillis))
    }

    fun transition(id: String, transition: OptimizationTransition): OptimizationJob {
        val current = jobs[id] ?: error("Unknown optimization job: $id")
        val updated = OptimizationStateMachine.transition(current, transition)
        if (updated.state == OptimizationJobState.RUNNING && jobs.values.any {
                it.state == OptimizationJobState.RUNNING && it.id != id
            }
        ) {
            error("Only one job may be running at a time")
        }
        jobs[id] = updated
        return updated
    }

    fun snapshot(): OptimizationQueueSnapshot = OptimizationQueueSnapshot(jobs.values.toList())
}
