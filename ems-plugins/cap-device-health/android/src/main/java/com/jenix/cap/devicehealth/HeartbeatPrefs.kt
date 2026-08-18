package com.jenix.cap.devicehealth

import android.content.Context

class HeartbeatPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("jenix_cap_device_health", Context.MODE_PRIVATE)

    fun intervalMinutes() = prefs.getInt("interval_minutes", 15)
    fun isRunning() = prefs.getBoolean("running", false)
    fun saveInterval(minutes: Int) = prefs.edit().putInt("interval_minutes", minutes).apply()
    fun saveRunning(running: Boolean) = prefs.edit().putBoolean("running", running).apply()
}
