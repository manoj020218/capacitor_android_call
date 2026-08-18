package com.jenix.cap.push

import android.content.Context
import android.content.Intent

object JenixPushMessageHandler {
    fun handleData(context: Context, payload: Map<String, String>): Boolean {
        return when (payload["commandType"]) {
            "VIDEO_CALL" -> VideoCallBridge.forward(context, payload)
            "SYNC_NOW" -> {
                context.sendBroadcast(Intent("com.jenix.cap.location.SYNC_NOW").setPackage(context.packageName))
                true
            }
            "CONFIG_UPDATED" -> {
                context.sendBroadcast(Intent("com.jenix.cap.push.CONFIG_UPDATED").setPackage(context.packageName))
                true
            }
            "ADMIN_MESSAGE" -> {
                PushNotificationHelper.showAdminMessage(
                    context,
                    payload["title"] ?: "Admin message",
                    payload["body"] ?: "",
                )
                true
            }
            "DEVICE_STATUS_REQUEST" -> {
                context.sendBroadcast(Intent("com.jenix.cap.push.REQUEST_DEVICE_STATUS").setPackage(context.packageName))
                true
            }
            else -> false
        }
    }
}
