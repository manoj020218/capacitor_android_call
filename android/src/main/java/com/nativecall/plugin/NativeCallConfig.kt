package com.nativecall.plugin

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

data class RingtoneOption(val key: String, val resourceName: String)

/**
 * The configuration passed to `NativeCall.initialize()`, persisted to disk so
 * it survives process death — a call can arrive via FCM with the host app's
 * process fully killed, long after the JS layer that called `initialize()`
 * is gone.
 */
data class NativeCallConfig(
    val ringtones: List<RingtoneOption>,
    val defaultRingtoneKey: String,
    val ringDurationSeconds: Int,
    val incomingCallRoute: String?,
) {
    /** Resolves an incoming FCM payload's `ringtone` key, falling back to the
     * configured default if the key is missing or unrecognized. */
    fun ringtoneFor(key: String?): RingtoneOption? =
        ringtones.firstOrNull { it.key == key } ?: ringtones.firstOrNull { it.key == defaultRingtoneKey }

    companion object {
        private const val PREFS_NAME = "native_call_config"
        private const val KEY_RINGTONES = "ringtones"
        private const val KEY_DEFAULT_RINGTONE = "default_ringtone"
        private const val KEY_RING_DURATION = "ring_duration_seconds"
        private const val KEY_ROUTE = "incoming_call_route"
        const val DEFAULT_RING_DURATION_SECONDS = 45

        private fun prefs(context: Context): SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        fun save(context: Context, config: NativeCallConfig) {
            val ringtonesJson = JSONArray()
            config.ringtones.forEach {
                ringtonesJson.put(JSONObject().put("key", it.key).put("resourceName", it.resourceName))
            }
            prefs(context)
                .edit()
                .putString(KEY_RINGTONES, ringtonesJson.toString())
                .putString(KEY_DEFAULT_RINGTONE, config.defaultRingtoneKey)
                .putInt(KEY_RING_DURATION, config.ringDurationSeconds)
                .putString(KEY_ROUTE, config.incomingCallRoute)
                .apply()
        }

        fun load(context: Context): NativeCallConfig? {
            val p = prefs(context)
            val ringtonesJson = p.getString(KEY_RINGTONES, null) ?: return null
            val defaultKey = p.getString(KEY_DEFAULT_RINGTONE, null) ?: return null
            val ringtones = mutableListOf<RingtoneOption>()
            val arr = JSONArray(ringtonesJson)
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                ringtones.add(RingtoneOption(obj.getString("key"), obj.getString("resourceName")))
            }
            return NativeCallConfig(
                ringtones = ringtones,
                defaultRingtoneKey = defaultKey,
                ringDurationSeconds = p.getInt(KEY_RING_DURATION, DEFAULT_RING_DURATION_SECONDS),
                incomingCallRoute = p.getString(KEY_ROUTE, null),
            )
        }

        /** Used by `setRingtone()` — does not touch registered channels, which
         * were already created for every configured ringtone at `initialize()`. */
        fun updateDefaultRingtone(context: Context, key: String) {
            prefs(context).edit().putString(KEY_DEFAULT_RINGTONE, key).apply()
        }
    }
}
