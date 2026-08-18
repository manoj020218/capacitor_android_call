package com.jenix.cap.location

import android.content.Context
import com.getcapacitor.JSObject
import com.jenix.cap.core.QueueStore

class LocationPrefs(context: Context) {
    private val prefs = context.getSharedPreferences("jenix_cap_location", Context.MODE_PRIVATE)

    fun save(options: Map<String, Any?>) {
        prefs.edit()
            .putBoolean("running", options["running"] as? Boolean ?: false)
            .putLong("intervalMs", (options["intervalMs"] as? Number)?.toLong() ?: 60000L)
            .putInt("minDistanceMeters", (options["minDistanceMeters"] as? Number)?.toInt() ?: 20)
            .putString("accuracy", options["accuracy"] as? String ?: "high")
            .putInt("batchSize", (options["batchSize"] as? Number)?.toInt() ?: 100)
            .putString("employeeId", options["employeeId"] as? String)
            .putString("foregroundTitle", options["foregroundTitle"] as? String ?: "FieldForce tracking active")
            .putString("foregroundBody", options["foregroundBody"] as? String ?: "Location sync is running in the background")
            .apply()
    }

    fun setRunning(running: Boolean) = prefs.edit().putBoolean("running", running).apply()
    fun setLastLocationAt(value: String) = prefs.edit().putString("last_location_at", value).apply()
    fun running() = prefs.getBoolean("running", false)
    fun intervalMs() = prefs.getLong("intervalMs", 60000L)
    fun minDistanceMeters() = prefs.getInt("minDistanceMeters", 20)
    fun accuracy() = prefs.getString("accuracy", "high") ?: "high"
    fun batchSize() = prefs.getInt("batchSize", 100)
    fun employeeId() = prefs.getString("employeeId", null)
    fun foregroundTitle() = prefs.getString("foregroundTitle", "FieldForce tracking active")!!
    fun foregroundBody() = prefs.getString("foregroundBody", "Location sync is running in the background")!!
    fun lastLocationAt() = prefs.getString("last_location_at", null)

    fun toStatus(queueStore: QueueStore) = JSObject().apply {
        put("running", running())
        put("pendingCount", queueStore.count("location"))
        put("lastLocationAt", lastLocationAt())
        put("options", JSObject().apply {
            put("intervalMs", intervalMs())
            put("minDistanceMeters", minDistanceMeters())
            put("accuracy", accuracy())
            put("batchSize", batchSize())
            put("foregroundTitle", foregroundTitle())
            put("foregroundBody", foregroundBody())
        })
    }
}
