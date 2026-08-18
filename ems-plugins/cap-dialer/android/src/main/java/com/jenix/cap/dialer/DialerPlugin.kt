package com.jenix.cap.dialer

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.core.content.ContextCompat
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.ActivityCallback

@CapacitorPlugin(name = "JenixDialer")
class DialerPlugin : Plugin() {
    private lateinit var prefs: DialerPrefs

    override fun load() {
        prefs = DialerPrefs(context)
    }

    @PluginMethod
    fun isDefaultDialer(call: PluginCall) {
        call.resolve(JSObject().apply { put("value", DialerRoleHelper.isDefault(context)) })
    }

    @PluginMethod
    fun requestDefaultDialer(call: PluginCall) {
        startActivityForResult(call, DialerRoleHelper.requestIntent(context), "handleRoleResult")
    }

    @ActivityCallback
    private fun handleRoleResult(call: PluginCall, result: ActivityResult) {
        call.resolve(JSObject().apply { put("requested", true) })
    }

    @PluginMethod
    fun dial(call: PluginCall) {
        val number = call.getString("number")
        if (number.isNullOrEmpty()) {
            call.reject("number is required", "INVALID_ARGUMENT")
            return
        }
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            call.reject("CALL_PHONE permission required", "PERMISSION_DENIED")
            return
        }
        val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$number")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
        call.resolve(JSObject().apply { put("started", true) })
    }

    @PluginMethod
    fun getCallState(call: PluginCall) {
        call.resolve(prefs.state())
    }

    @PluginMethod
    fun getRecentCalls(call: PluginCall) {
        val limit = call.getInt("limit", 20) ?: 20
        call.resolve(JSObject().apply { put("items", CallLogBridge.toArray(CallLogBridge.read(context, limit))) })
    }

    @PluginMethod
    fun syncCalls(call: PluginCall) {
        WorkManager.getInstance(context).enqueueUniqueWork(
            "jenix_dialer_sync",
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequestBuilder<CallSyncWorker>().build(),
        )
        call.resolve(JSObject().apply { put("queued", true) })
    }
}
