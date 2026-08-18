package com.jenix.cap.devicehealth

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "JenixDeviceHealth")
class DeviceHealthPlugin : Plugin() {
    private lateinit var prefs: HeartbeatPrefs
    private lateinit var scheduler: HeartbeatScheduler

    override fun load() {
        prefs = HeartbeatPrefs(context)
        scheduler = HeartbeatScheduler(context)
    }

    @PluginMethod
    fun getStatus(call: PluginCall) {
        call.resolve(HealthCollector.collect(context).toJs())
    }

    @PluginMethod
    fun startHeartbeat(call: PluginCall) {
        val interval = maxOf(15, call.getInt("intervalMinutes", 15) ?: 15)
        prefs.saveInterval(interval)
        prefs.saveRunning(true)
        scheduler.start(interval)
        call.resolve(JSObject().apply {
            put("running", true)
            put("intervalMinutes", interval)
        })
    }

    @PluginMethod
    fun stopHeartbeat(call: PluginCall) {
        prefs.saveRunning(false)
        scheduler.stop()
        call.resolve(JSObject().apply { put("running", false) })
    }

    @PluginMethod
    fun sendHeartbeatNow(call: PluginCall) {
        scheduler.triggerNow()
        call.resolve(JSObject().apply { put("queued", true) })
    }
}
