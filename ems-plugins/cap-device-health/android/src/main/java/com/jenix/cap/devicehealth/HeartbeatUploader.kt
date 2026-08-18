package com.jenix.cap.devicehealth

import android.content.Context
import com.jenix.cap.core.CoreHttp
import com.jenix.cap.core.CoreStore
import com.jenix.cap.core.DeviceIdentity

class HeartbeatUploader(context: Context) {
    private val http = CoreHttp(CoreStore(context))

    fun upload(context: Context, snapshot: HealthSnapshot) {
        http.postJson(
            "/api/v1/device-health/heartbeat",
            snapshot.toJson(DeviceIdentity.getDeviceId(context)),
            true,
        )
    }
}
