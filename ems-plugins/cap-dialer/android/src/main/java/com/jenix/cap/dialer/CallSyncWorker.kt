package com.jenix.cap.dialer

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jenix.cap.core.CoreHttp
import com.jenix.cap.core.CoreNetwork
import com.jenix.cap.core.CoreStore
import com.jenix.cap.core.DeviceIdentity
import org.json.JSONObject

class CallSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        if (!CoreNetwork.isAvailable(applicationContext)) return Result.retry()
        val prefs = DialerPrefs(applicationContext)
        val calls = CallLogBridge.read(applicationContext, 50, prefs.lastSync())
        if (calls.isEmpty()) return Result.success()
        val http = CoreHttp(CoreStore(applicationContext))
        return try {
            calls.forEach { item ->
                val startedAt = java.time.Instant.parse(item.getString("startedAt")).toEpochMilli()
                http.postJson("/api/v1/calls/ingest", JSONObject().apply {
                    put("deviceId", DeviceIdentity.getDeviceId(applicationContext))
                    put("externalCallId", CallIdFactory.build(DeviceIdentity.getDeviceId(applicationContext), item.getString("phoneNumber"), startedAt, item.getString("direction")))
                    put("phoneNumber", item.getString("phoneNumber"))
                    put("direction", item.getString("direction"))
                    put("status", item.getString("status"))
                    put("startedAt", item.getString("startedAt"))
                    put("answeredAt", item.optString("answeredAt", null))
                    put("endedAt", item.optString("endedAt", null))
                    put("durationSeconds", item.getLong("durationSeconds"))
                    put("contactName", item.optString("contactName", null))
                }, true)
                prefs.setLastSync(startedAt)
            }
            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }
}
