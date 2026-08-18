package com.jenix.cap.lifecycle

import android.content.Context

class LifecyclePrefs(context: Context) {
    private val prefs = context.getSharedPreferences("jenix_cap_lifecycle", Context.MODE_PRIVATE)

    fun setEvent(name: String) {
        prefs.edit().putString("last_event", name).putString("last_event_at", java.time.Instant.now().toString()).apply()
    }

    fun setRecoveryQueued(value: Boolean) = prefs.edit().putBoolean("queued", value).apply()
    fun setLastRun() = prefs.edit().putString("last_run_at", java.time.Instant.now().toString()).apply()
    fun lastEvent() = prefs.getString("last_event", "unknown") ?: "unknown"
    fun lastEventAt() = prefs.getString("last_event_at", null)
    fun queued() = prefs.getBoolean("queued", false)
    fun lastRunAt() = prefs.getString("last_run_at", null)
}
