package com.jenix.cap.lifecycle

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "JenixLifecycle")
class LifecyclePlugin : Plugin() {
    private lateinit var prefs: LifecyclePrefs

    override fun load() {
        prefs = LifecyclePrefs(context)
    }

    @PluginMethod
    fun getLastBootState(call: PluginCall) {
        call.resolve(JSObject().apply {
            put("lastEvent", prefs.lastEvent())
            put("lastEventAt", prefs.lastEventAt())
        })
    }

    @PluginMethod
    fun getRecoveryStatus(call: PluginCall) {
        call.resolve(JSObject().apply {
            put("queued", prefs.queued())
            put("lastRunAt", prefs.lastRunAt())
        })
    }
}
