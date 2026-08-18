package com.jenix.cap.devicehealth

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DeviceHealthRecoverReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val prefs = HeartbeatPrefs(context)
        if (!prefs.isRunning()) return
        HeartbeatScheduler(context).triggerNow()
        HeartbeatScheduler(context).start(prefs.intervalMinutes())
    }
}
