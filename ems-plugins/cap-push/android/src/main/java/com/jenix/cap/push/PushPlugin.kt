package com.jenix.cap.push

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.jenix.cap.core.CoreStore
import org.json.JSONObject

@CapacitorPlugin(name = "JenixPush")
class PushPlugin : Plugin() {
    private lateinit var pushState: PushStateStore
    private lateinit var coreStore: CoreStore

    override fun load() {
        pushState = PushStateStore(context)
        coreStore = CoreStore(context)
    }

    @PluginMethod
    fun getToken(call: PluginCall) {
        call.resolve(JSObject().apply { put("token", pushState.token()) })
    }

    @PluginMethod
    fun refreshRegistration(call: PluginCall) {
        val token = call.getString("token") ?: pushState.token()
        if (token.isNullOrEmpty()) {
            call.resolve(JSObject().apply { put("uploaded", false) })
            return
        }
        try {
            pushState.saveToken(token)
            call.resolve(JSObject().apply { put("uploaded", PushTokenUploader(context).upload(context, token)) })
        } catch (error: Exception) {
            call.reject(error.message ?: "Upload failed", "BACKEND_REJECTED")
        }
    }

    @PluginMethod
    fun getPushStatus(call: PluginCall) {
        call.resolve(JSObject().apply {
            put("token", pushState.token())
            put("configured", coreStore.baseUrl() != null)
            put("nativeCallBridgeAvailable", VideoCallBridge.isAvailable())
        })
    }

    @PluginMethod
    fun dispatchPayload(call: PluginCall) {
        val json = JSONObject(call.data.toString())
        val payload = buildMap {
            json.keys().forEach { key -> put(key, json.optString(key)) }
        }
        val type = payload["commandType"]
        val handled = JenixPushMessageHandler.handleData(context, payload)
        if (type != null) pushState.saveLastCommand(type)
        call.resolve(JSObject().apply {
            put("handled", handled)
            put("commandType", type)
        })
    }
}
