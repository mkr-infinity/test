package com.mkrinfinity.autooptimiser.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.mkrinfinity.autooptimiser.data.DeviceRepository
import kotlinx.coroutines.CancellationException

/** Background work is intentionally limited to a cheap device snapshot. File scans stay user initiated. */
class AutoAnalysisWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result = try {
        val status = DeviceRepository(applicationContext as android.app.Application).read()
        applicationContext.getSharedPreferences("auto_optimiser_preferences", Context.MODE_PRIVATE)
            .edit()
            .putLong("background_last_check", status.measuredAtMillis)
            .apply()
        Result.success()
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (_: Exception) {
        Result.retry()
    }
}
