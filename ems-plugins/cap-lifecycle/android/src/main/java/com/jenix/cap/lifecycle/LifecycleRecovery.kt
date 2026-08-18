package com.jenix.cap.lifecycle

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

object LifecycleRecovery {
    fun enqueue(context: Context, event: String) {
        val prefs = LifecyclePrefs(context)
        prefs.setEvent(event)
        prefs.setRecoveryQueued(true)
        WorkManager.getInstance(context).enqueueUniqueWork(
            "jenix_lifecycle_recovery",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<RecoveryWorker>().build(),
        )
    }
}
