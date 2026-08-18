package com.jenix.cap.location

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.jenix.cap.core.DeviceIdentity
import com.jenix.cap.core.QueueStore

class LocationForegroundService : Service() {
    private val queue by lazy { QueueStore(this) }
    private val prefs by lazy { LocationPrefs(this) }
    private val client by lazy { LocationServices.getFusedLocationProviderClient(this) }
    private val callback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                val payload = LocationPointJson.fromLocation(this@LocationForegroundService, location)
                val timestamp = java.time.Instant.ofEpochMilli(location.time).toString()
                queue.enqueue("location", payload, "${DeviceIdentity.getDeviceId(this@LocationForegroundService)}:$timestamp")
                prefs.setLastLocationAt(timestamp)
                LocationSyncScheduler.schedule(this@LocationForegroundService)
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> stopTracking()
            ACTION_SYNC -> LocationSyncScheduler.schedule(this)
            else -> startTracking()
        }
        return START_STICKY
    }

    private fun startTracking() {
        if (!hasLocationPermission()) {
            stopSelf()
            return
        }
        startForeground(4101, LocationNotification.build(this, prefs.foregroundTitle(), prefs.foregroundBody()))
        prefs.setRunning(true)
        client.removeLocationUpdates(callback)
        val request = LocationRequest.Builder(priority(), prefs.intervalMs())
            .setMinUpdateDistanceMeters(prefs.minDistanceMeters().toFloat())
            .build()
        client.requestLocationUpdates(request, callback, Looper.getMainLooper())
    }

    private fun stopTracking() {
        prefs.setRunning(false)
        client.removeLocationUpdates(callback)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        client.removeLocationUpdates(callback)
        super.onDestroy()
    }

    private fun priority() = when (prefs.accuracy()) {
        "low" -> Priority.PRIORITY_LOW_POWER
        "balanced" -> Priority.PRIORITY_BALANCED_POWER_ACCURACY
        else -> Priority.PRIORITY_HIGH_ACCURACY
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        const val ACTION_START = "com.jenix.cap.location.START"
        const val ACTION_STOP = "com.jenix.cap.location.STOP"
        const val ACTION_SYNC = "com.jenix.cap.location.SYNC"
    }
}
