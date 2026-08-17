package com.nativecall.plugin

import android.content.Context
import org.json.JSONObject

/**
 * In-process fan-out from native call outcomes (Activity buttons, notification
 * actions, service timeout) to the attached Capacitor plugin instance — plus a
 * durable one-slot mailbox for the case a bridge isn't attached yet, which
 * happens whenever the host app process was fully killed and only got
 * relaunched *by* the user tapping Accept/Decline on the lock screen.
 * [NativeCallPlugin.load] drains this mailbox once the JS listeners are wired up.
 */
internal object NativeCallEvents {

    enum class Type { ANSWERED, DECLINED, TIMED_OUT }

    fun interface Listener {
        fun onCallEvent(type: Type, callId: String)
    }

    @Volatile
    private var listener: Listener? = null

    fun setListener(l: Listener?) {
        listener = l
    }

    fun emit(context: Context, type: Type, callId: String) {
        persist(context, type, callId)
        listener?.onCallEvent(type, callId)
    }

    fun consumePending(context: Context): Pair<Type, String>? {
        val prefs = prefs(context)
        val raw = prefs.getString(KEY_PENDING, null) ?: return null
        prefs.edit().remove(KEY_PENDING).apply()
        return try {
            val obj = JSONObject(raw)
            Type.valueOf(obj.getString("type")) to obj.getString("callId")
        } catch (e: Exception) {
            null
        }
    }

    private fun persist(context: Context, type: Type, callId: String) {
        val json = JSONObject()
            .put("type", type.name)
            .put("callId", callId)
            .put("timestamp", System.currentTimeMillis())
        prefs(context).edit().putString(KEY_PENDING, json.toString()).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences("native_call_events", Context.MODE_PRIVATE)

    private const val KEY_PENDING = "pending"
}
