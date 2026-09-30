package com.mkrinfinity.autooptimiser.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mkrinfinity.autooptimiser.data.DeviceRepository
import com.mkrinfinity.autooptimiser.data.DeviceHistory
import com.mkrinfinity.autooptimiser.data.PreferencesRepository
import kotlinx.coroutines.CancellationException

/** Background work is intentionally limited to a cheap device snapshot. File scans stay user initiated. */
class AutoAnalysisWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        return try {
        if (!PreferencesRepository(applicationContext).automaticOptimisation) return Result.success()
        val status = DeviceRepository(applicationContext).read()
        DeviceHistory(applicationContext).record(status)
        applicationContext.getSharedPreferences("auto_optimiser_preferences", Context.MODE_PRIVATE)
            .edit()
            .putLong("background_last_check", status.measuredAtMillis)
            .apply()
        Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
        } catch (_: Exception) {
            if (runAttemptCount < 2) Result.retry() else Result.failure()
        }
    }
}
