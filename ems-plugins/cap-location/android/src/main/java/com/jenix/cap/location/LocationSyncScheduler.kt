package com.jenix.cap.location

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object LocationSyncScheduler {
    fun schedule(context: Context) {
        val request = OneTimeWorkRequestBuilder<LocationSyncWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork("jenix_location_sync", ExistingWorkPolicy.KEEP, request)
    }
}
