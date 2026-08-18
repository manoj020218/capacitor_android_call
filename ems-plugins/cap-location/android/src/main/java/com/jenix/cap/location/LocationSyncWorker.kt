package com.jenix.cap.location

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jenix.cap.core.CoreHttp
import com.jenix.cap.core.CoreNetwork
import com.jenix.cap.core.CoreStore
import com.jenix.cap.core.DeviceIdentity
import com.jenix.cap.core.QueueStore
import org.json.JSONObject

class LocationSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!CoreNetwork.isAvailable(applicationContext)) return Result.retry()
        val queue = QueueStore(applicationContext)
        val rows = queue.readBatch("location", LocationPrefs(applicationContext).batchSize())
        if (rows.isEmpty()) return Result.success()
        return try {
            val prefs = LocationPrefs(applicationContext)
            val body = JSONObject().apply {
                put("deviceId", DeviceIdentity.getDeviceId(applicationContext))
                if (prefs.employeeId() != null) put("employeeId", prefs.employeeId())
                put("points", LocationPointJson.toBatch(rows))
            }
            CoreHttp(CoreStore(applicationContext)).postJson("/api/v1/locations/batch", body, true)
            queue.delete(rows.map { it.id })
            Result.success()
        } catch (error: Exception) {
            rows.forEach { queue.incrementAttempts(it.id, error.message ?: "sync failed") }
            Result.retry()
        }
    }
}
