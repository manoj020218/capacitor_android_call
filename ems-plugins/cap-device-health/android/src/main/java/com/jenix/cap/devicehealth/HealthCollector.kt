package com.jenix.cap.devicehealth

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import com.getcapacitor.JSObject
import com.jenix.cap.core.CoreNetwork
import org.json.JSONObject

data class HealthSnapshot(
    val timestamp: String,
    val batteryPercent: Int?,
    val charging: Boolean?,
    val gpsEnabled: Boolean,
    val networkType: String,
    val internetAvailable: Boolean,
    val appVersion: String?,
    val androidVersion: String,
    val manufacturer: String,
    val model: String,
    val trackingServiceRunning: Boolean,
    val lastLocationAt: String?,
) {
    fun toJson(deviceId: String) = JSONObject().apply {
        put("deviceId", deviceId)
        put("timestamp", timestamp)
        put("batteryPercent", batteryPercent)
        put("charging", charging)
        put("gpsEnabled", gpsEnabled)
        put("networkType", networkType)
        put("internetAvailable", internetAvailable)
        put("appVersion", appVersion)
        put("androidVersion", androidVersion)
        put("trackingServiceRunning", trackingServiceRunning)
        put("lastLocationAt", lastLocationAt)
    }

    fun toJs() = JSObject().apply {
        put("timestamp", timestamp)
        put("batteryPercent", batteryPercent)
        put("charging", charging)
        put("gpsEnabled", gpsEnabled)
        put("networkType", networkType)
        put("internetAvailable", internetAvailable)
        put("appVersion", appVersion)
        put("androidVersion", androidVersion)
        put("manufacturer", manufacturer)
        put("model", model)
        put("trackingServiceRunning", trackingServiceRunning)
        put("lastLocationAt", lastLocationAt)
    }
}

object HealthCollector {
    fun collect(context: Context): HealthSnapshot {
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)?.takeIf { it >= 0 }
        val status = battery?.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
        val locationPrefs = context.getSharedPreferences("jenix_cap_location", Context.MODE_PRIVATE)
        return HealthSnapshot(
            timestamp = java.time.Instant.now().toString(),
            batteryPercent = level,
            charging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL,
            gpsEnabled = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isLocationEnabled,
            networkType = networkType(context),
            internetAvailable = CoreNetwork.isAvailable(context),
            appVersion = packageInfo.versionName,
            androidVersion = Build.VERSION.RELEASE ?: "unknown",
            manufacturer = Build.MANUFACTURER ?: "unknown",
            model = Build.MODEL ?: "unknown",
            trackingServiceRunning = locationPrefs.getBoolean("running", false),
            lastLocationAt = locationPrefs.getString("last_location_at", null),
        )
    }

    private fun networkType(context: Context): String {
        val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = manager.getNetworkCapabilities(manager.activeNetwork) ?: return "offline"
        return when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "wifi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "cellular"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ethernet"
            else -> "other"
        }
    }
}
