package com.jenix.cap.push

import android.content.Context
import com.jenix.cap.core.CoreHttp
import com.jenix.cap.core.CoreStore
import com.jenix.cap.core.DeviceIdentity
import org.json.JSONObject

class PushTokenUploader(context: Context) {
    private val http = CoreHttp(CoreStore(context))

    fun upload(context: Context, token: String): Boolean {
        http.patchJson(
            "/api/v1/devices/${DeviceIdentity.getDeviceId(context)}/fcm-token",
            JSONObject().put("fcmToken", token),
            true,
        )
        return true
    }
}
