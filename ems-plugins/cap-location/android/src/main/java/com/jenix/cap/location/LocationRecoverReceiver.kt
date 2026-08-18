package com.jenix.cap.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat

class LocationRecoverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("com.jenix.cap.location.SYNC_NOW" == intent.action) {
            LocationSyncScheduler.schedule(context)
            return
        }
        val prefs = LocationPrefs(context)
        if (!prefs.running()) return
        val serviceIntent = Intent(context, LocationForegroundService::class.java).setAction(LocationForegroundService.ACTION_START)
        ContextCompat.startForegroundService(context, serviceIntent)
    }
}
