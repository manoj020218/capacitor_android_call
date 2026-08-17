package com.nativecall.plugin

import android.app.NotificationManager
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "NativeCall")
class NativeCallPlugin : Plugin() {

    override fun load() {
        super.load()
        NativeCallEvents.setListener(NativeCallEvents.Listener { type, callId -> notifyEvent(type, callId) })
        // Deliver anything that fired before this instance existed (app was
        // fully killed and got relaunched by the user tapping Accept/Decline).
        NativeCallEvents.consumePending(context)?.let { (type, callId) -> notifyEvent(type, callId) }
    }

    @PluginMethod
    fun initialize(call: PluginCall) {
        val ringtonesArray = call.getArray("ringtones")
        if (ringtonesArray == null || ringtonesArray.length() == 0) {
            call.reject("`ringtones` must be a non-empty array")
            return
        }

        val ringtones = mutableListOf<RingtoneOption>()
        for (i in 0 until ringtonesArray.length()) {
            val obj = ringtonesArray.getJSONObject(i)
            val key = obj.optString("key", "")
            val resourceName = obj.optString("resourceName", "")
            if (key.isEmpty() || resourceName.isEmpty()) {
                call.reject("Each entry in `ringtones` needs a non-empty `key` and `resourceName`")
                return
            }
            if (resourceName.contains(".")) {
                call.reject(
                    "ringtones[].resourceName must be a bare Android raw-resource name with no " +
                        "extension (got '$resourceName') — see PLAN.md §1.1",
                )
                return
            }
            ringtones.add(RingtoneOption(key, resourceName))
        }

        val defaultRingtone = call.getString("defaultRingtone")
        if (defaultRingtone == null || ringtones.none { it.key == defaultRingtone }) {
            call.reject("`defaultRingtone` must match one of the provided ringtones[].key")
            return
        }

        val ringDurationSeconds = call.getInt("ringDurationSeconds") ?: NativeCallConfig.DEFAULT_RING_DURATION_SECONDS
        val incomingCallRoute = call.getString("incomingCallRoute")

        val config = NativeCallConfig(
            ringtones = ringtones,
            defaultRingtoneKey = defaultRingtone,
            ringDurationSeconds = ringDurationSeconds,
            incomingCallRoute = incomingCallRoute,
        )
        NativeCallConfig.save(context, config)
        RingtoneChannelManager.registerChannels(context, ringtones)
        call.resolve()
    }

    @PluginMethod
    fun stopRinging(call: PluginCall) {
        val callId = call.getString("callId")
        if (callId == null) {
            call.reject("`callId` is required")
            return
        }
        IncomingCallService.sendAction(context, Constants.ACTION_STOP_RINGING, callId)
        call.resolve()
    }

    @PluginMethod
    fun setRingtone(call: PluginCall) {
        val key = call.getString("key")
        if (key == null) {
            call.reject("`key` is required")
            return
        }
        val config = NativeCallConfig.load(context)
        if (config == null || config.ringtones.none { it.key == key }) {
            call.reject("Unknown ringtone key '$key' — it must match a key passed to initialize()")
            return
        }
        NativeCallConfig.updateDefaultRingtone(context, key)
        call.resolve()
    }

    @PluginMethod
    fun checkFullScreenIntentPermission(call: PluginCall) {
        val granted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.canUseFullScreenIntent() ?: true
        } else {
            true
        }
        val result = JSObject()
        result.put("granted", granted)
        call.resolve(result)
    }

    @PluginMethod
    fun requestFullScreenIntentPermission(call: PluginCall) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val intent = Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                call.reject("Could not open full-screen-intent settings", e)
                return
            }
        }
        call.resolve()
    }

    private fun notifyEvent(type: NativeCallEvents.Type, callId: String) {
        val data = JSObject()
        data.put("callId", callId)
        val eventName = when (type) {
            NativeCallEvents.Type.ANSWERED -> "callAnswered"
            NativeCallEvents.Type.DECLINED -> "callDeclined"
            NativeCallEvents.Type.TIMED_OUT -> "callTimedOut"
        }
        // retainUntilConsumed=true: covers the narrower race where this
        // instance already existed but the JS side's addListener() hadn't
        // resolved yet.
        notifyListeners(eventName, data, true)
    }
}
