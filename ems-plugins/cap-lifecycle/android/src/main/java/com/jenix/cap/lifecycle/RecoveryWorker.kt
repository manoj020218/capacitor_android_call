package com.jenix.cap.lifecycle

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class RecoveryWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val prefs = LifecyclePrefs(applicationContext)
        prefs.setRecoveryQueued(false)
        applicationContext.sendBroadcast(Intent("com.jenix.cap.lifecycle.RECOVER_HEARTBEAT").setPackage(applicationContext.packageName))
        applicationContext.sendBroadcast(Intent("com.jenix.cap.lifecycle.RECOVER_LOCATION").setPackage(applicationContext.packageName))
        prefs.setLastRun()
        return Result.success()
    }
}
