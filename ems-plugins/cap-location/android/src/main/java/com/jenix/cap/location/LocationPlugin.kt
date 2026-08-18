package com.jenix.cap.location

import android.Manifest
import android.content.Intent
import androidx.core.content.ContextCompat
import com.getcapacitor.JSObject
import com.getcapacitor.PermissionState
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import com.google.android.gms.location.LocationServices
import com.jenix.cap.core.QueueStore

@CapacitorPlugin(
    name = "JenixLocation",
    permissions = [
        Permission(alias = "location", strings = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION]),
        Permission(alias = "background", strings = [Manifest.permission.ACCESS_BACKGROUND_LOCATION]),
    ],
)
class LocationPlugin : Plugin() {
    private lateinit var prefs: LocationPrefs
    private lateinit var queue: QueueStore

    override fun load() {
        prefs = LocationPrefs(context)
        queue = QueueStore(context)
    }

    @PluginMethod
    fun startTracking(call: PluginCall) {
        if (getPermissionState("location") != PermissionState.GRANTED) {
            call.reject("Location permission required", "PERMISSION_DENIED")
            return
        }
        prefs.save(mapOf(
            "running" to true,
            "intervalMs" to (call.getLong("intervalMs") ?: 60000L),
            "minDistanceMeters" to (call.getInt("minDistanceMeters") ?: 20),
            "accuracy" to (call.getString("accuracy") ?: "high"),
            "batchSize" to (call.getInt("batchSize") ?: 100),
            "employeeId" to call.getString("employeeId"),
            "foregroundTitle" to call.getString("foregroundTitle"),
            "foregroundBody" to call.getString("foregroundBody"),
        ))
        ContextCompat.startForegroundService(context, Intent(context, LocationForegroundService::class.java).setAction(LocationForegroundService.ACTION_START))
        call.resolve(prefs.toStatus(queue))
    }

    @PluginMethod
    fun stopTracking(call: PluginCall) {
        context.startService(Intent(context, LocationForegroundService::class.java).setAction(LocationForegroundService.ACTION_STOP))
        prefs.setRunning(false)
        call.resolve(JSObject().apply { put("running", false) })
    }

    @PluginMethod
    fun getCurrentLocation(call: PluginCall) {
        val client = LocationServices.getFusedLocationProviderClient(context)
        client.lastLocation.addOnSuccessListener { location ->
            call.resolve(JSObject().apply {
                put("latitude", location?.latitude)
                put("longitude", location?.longitude)
                put("accuracy", location?.accuracy)
                put("timestamp", location?.time?.let { java.time.Instant.ofEpochMilli(it).toString() })
            })
        }.addOnFailureListener { error -> call.reject(error.message ?: "Location failed", "NOT_SUPPORTED") }
    }

    @PluginMethod
    fun getTrackingStatus(call: PluginCall) = call.resolve(prefs.toStatus(queue))

    @PluginMethod
    fun getPendingCount(call: PluginCall) = call.resolve(JSObject().apply { put("count", queue.count("location")) })

    @PluginMethod
    fun syncNow(call: PluginCall) {
        LocationSyncScheduler.schedule(context)
        call.resolve(JSObject().apply { put("queued", true) })
    }

    @PluginMethod
    override fun checkPermissions(call: PluginCall) {
        call.resolve(permissionStatus())
    }

    @PluginMethod
    override fun requestPermissions(call: PluginCall) {
        requestAllPermissions(call, "permissionsCallback")
    }

    @PermissionCallback
    private fun permissionsCallback(call: PluginCall) {
        call.resolve(permissionStatus())
    }

    private fun permissionStatus() = JSObject().apply {
        put("location", getPermissionState("location").toString().lowercase())
        put("background", getPermissionState("background").toString().lowercase())
    }
}
