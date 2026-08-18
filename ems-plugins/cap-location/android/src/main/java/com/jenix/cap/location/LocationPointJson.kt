package com.jenix.cap.location

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Location
import android.os.BatteryManager
import androidx.core.location.LocationCompat
import org.json.JSONArray
import org.json.JSONObject

object LocationPointJson {
    fun fromLocation(context: Context, location: Location): String {
        val battery = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = battery?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)?.takeIf { it >= 0 }
        return JSONObject().apply {
            put("timestamp", java.time.Instant.ofEpochMilli(location.time).toString())
            put("latitude", location.latitude)
            put("longitude", location.longitude)
            put("accuracy", location.accuracy.toDouble())
            put("speed", location.speed.toDouble())
            put("heading", location.bearing.toDouble())
            put("battery", level)
            put("mockLocation", LocationCompat.isMock(location))
        }.toString()
    }

    fun toBatch(rows: List<com.jenix.cap.core.QueueRow>) = JSONArray().apply {
        rows.forEach { put(JSONObject(it.payload)) }
    }
}
