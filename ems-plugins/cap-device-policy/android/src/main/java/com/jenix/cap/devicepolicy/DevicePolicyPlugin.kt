package com.jenix.cap.devicepolicy

import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "JenixDevicePolicy")
class DevicePolicyPlugin : Plugin() {
    @PluginMethod
    fun getManagementStatus(call: PluginCall) {
        call.resolve(DevicePolicyStatus.status(context))
    }

    @PluginMethod
    fun getCapabilities(call: PluginCall) {
        call.resolve(DevicePolicyStatus.capabilities(context))
    }

    @PluginMethod
    fun isDeviceOwner(call: PluginCall) {
        val status = DevicePolicyStatus.status(context)
        call.resolve(JSObject().apply { put("value", status.getBool("deviceOwner")) })
    }

    @PluginMethod
    fun isProfileOwner(call: PluginCall) {
        val status = DevicePolicyStatus.status(context)
        call.resolve(JSObject().apply { put("value", status.getBool("profileOwner")) })
    }
}
