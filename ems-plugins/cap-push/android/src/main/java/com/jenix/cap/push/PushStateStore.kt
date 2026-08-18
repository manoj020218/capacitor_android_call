package com.jenix.cap.push

import android.content.Context

class PushStateStore(context: Context) {
    private val prefs = context.getSharedPreferences("jenix_cap_push", Context.MODE_PRIVATE)

    fun token() = prefs.getString("token", null)
    fun saveToken(token: String) = prefs.edit().putString("token", token).apply()
    fun saveLastCommand(type: String) = prefs.edit().putString("last_command", type).apply()
    fun lastCommand() = prefs.getString("last_command", null)
}
