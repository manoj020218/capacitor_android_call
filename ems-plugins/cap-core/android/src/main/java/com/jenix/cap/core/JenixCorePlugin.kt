package com.jenix.cap.core

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import org.json.JSONObject

@CapacitorPlugin(name = "JenixCore")
class JenixCorePlugin : Plugin() {
    private lateinit var store: CoreStore
    private lateinit var http: CoreHttp

    override fun load() {
        store = CoreStore(context)
        http = CoreHttp(store)
    }

    @PluginMethod
    fun configure(call: PluginCall) {
        val baseUrl = call.getString("baseUrl")?.trim()?.trimEnd('/')
        if (baseUrl.isNullOrEmpty()) {
            call.reject("baseUrl is required", "NOT_CONFIGURED")
            return
        }
        val headers = JSONObject(call.getObject("defaultHeaders")?.toString() ?: "{}")
        store.saveConfig(baseUrl, call.getString("companyId"), headers)
        call.resolve(JSObject().apply {
            put("configured", true)
            put("baseUrl", baseUrl)
        })
    }

    @PluginMethod
    fun setUserSession(call: PluginCall) {
        val accessToken = call.getString("accessToken")
        if (accessToken.isNullOrEmpty()) {
            call.reject("accessToken is required", "AUTH_REQUIRED")
            return
        }
        store.saveUserSession(
            accessToken,
            call.getString("refreshToken"),
            call.getString("userId"),
            call.getString("employeeId"),
            call.getString("companyId"),
        )
        call.resolve()
    }

    @PluginMethod
    fun registerDevice(call: PluginCall) {
        try {
            val deviceId = DeviceIdentity.getDeviceId(context)
            val payload = JSONObject().apply {
                put("deviceId", deviceId)
                put("employeeId", call.getString("employeeId") ?: store.employeeId())
                put("platform", call.getString("platform") ?: "android")
                put("manufacturer", call.getString("manufacturer") ?: android.os.Build.MANUFACTURER)
                put("model", call.getString("model") ?: android.os.Build.MODEL)
                put("androidVersion", call.getString("androidVersion") ?: android.os.Build.VERSION.RELEASE)
                put("appVersion", call.getString("appVersion") ?: "unknown")
                put("deviceName", call.getString("deviceName") ?: android.os.Build.DEVICE)
                put("fcmToken", call.getString("fcmToken"))
                put("managed", call.getBoolean("managed", true))
                put("companyId", store.companyId())
            }
            val response = http.postJson("/api/v1/devices/register", payload, false)
            val data = response.getJSONObject("data")
            if (data.has("deviceToken")) store.saveDeviceToken(data.getString("deviceToken"))
            call.resolve(JSObject().apply {
                put("deviceId", deviceId)
                put("deviceTokenStored", data.has("deviceToken"))
            })
        } catch (error: Exception) {
            call.reject(error.message ?: "Device registration failed", "BACKEND_REJECTED")
        }
    }

    @PluginMethod
    fun getDeviceId(call: PluginCall) {
        call.resolve(JSObject().apply { put("deviceId", DeviceIdentity.getDeviceId(context)) })
    }

    @PluginMethod
    fun getNativeStatus(call: PluginCall) {
        call.resolve(JSObject().apply {
            put("configured", store.baseUrl() != null)
            put("hasUserAccessToken", store.userAccessToken() != null)
            put("hasDeviceToken", store.deviceToken() != null)
            put("networkAvailable", CoreNetwork.isAvailable(context))
            put("baseUrl", store.baseUrl())
            put("deviceId", DeviceIdentity.getDeviceId(context))
        })
    }

    @PluginMethod
    fun clearSession(call: PluginCall) {
        store.clearSession()
        call.resolve()
    }
}
