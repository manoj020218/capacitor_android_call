package com.jenix.cap.devicehealth

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

class HeartbeatScheduler(context: Context) {
    private val manager = WorkManager.getInstance(context)

    fun start(intervalMinutes: Int) {
        val request = PeriodicWorkRequestBuilder<HeartbeatWorker>(intervalMinutes.toLong(), TimeUnit.MINUTES)
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        manager.enqueueUniquePeriodicWork("jenix_device_health", ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    fun stop() {
        manager.cancelUniqueWork("jenix_device_health")
        manager.cancelUniqueWork("jenix_device_health_now")
    }

    fun triggerNow() {
        val request = OneTimeWorkRequestBuilder<HeartbeatWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        manager.enqueueUniqueWork("jenix_device_health_now", ExistingWorkPolicy.REPLACE, request)
    }
}
