package com.jenix.cap.devicehealth

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jenix.cap.core.CoreNetwork

class HeartbeatWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!CoreNetwork.isAvailable(applicationContext)) return Result.retry()
        return try {
            val snapshot = HealthCollector.collect(applicationContext)
            HeartbeatUploader(applicationContext).upload(applicationContext, snapshot)
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
