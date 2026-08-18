package com.jenix.cap.dialer

import android.content.Context
import com.getcapacitor.JSObject

class DialerPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("jenix_cap_dialer", Context.MODE_PRIVATE)

    fun saveState(active: Boolean, number: String?, direction: String?, startedAt: String?) {
        prefs.edit().putBoolean("active", active).putString("number", number).putString("direction", direction).putString("startedAt", startedAt).apply()
    }

    fun setLastSync(value: Long) = prefs.edit().putLong("lastSync", value).apply()
    fun lastSync() = prefs.getLong("lastSync", 0L)

    fun state() = JSObject().apply {
        put("active", prefs.getBoolean("active", false))
        put("number", prefs.getString("number", null))
        put("direction", prefs.getString("direction", null))
        put("startedAt", prefs.getString("startedAt", null))
    }
}
