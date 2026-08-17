package com.nativecall.plugin

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

internal object CallNotificationBuilder {

    private const val FALLBACK_CHANNEL_ID = "native_call_fallback"

    fun build(
        context: Context,
        callId: String,
        title: String,
        body: String,
        channelId: String,
    ): Notification {
        val fullScreenIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(Constants.EXTRA_CALL_ID, callId)
            putExtra(Constants.EXTRA_TITLE, title)
            putExtra(Constants.EXTRA_BODY, body)
        }
        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            callId.hashCode(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        // The Accept action must launch an Activity directly (PendingIntent.getActivity),
        // not go through a BroadcastReceiver that then calls startActivity() itself —
        // Android 12+ blocks that as a "notification trampoline" background activity
        // launch, even from a notification action tap. Reuses IncomingCallActivity with
        // an auto-answer flag so it accepts immediately instead of waiting for a second
        // tap on its own Accept button.
        val answerIntent = Intent(context, IncomingCallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(Constants.EXTRA_CALL_ID, callId)
            putExtra(Constants.EXTRA_TITLE, title)
            putExtra(Constants.EXTRA_BODY, body)
            putExtra(Constants.EXTRA_AUTO_ANSWER, true)
        }
        val answerPendingIntent = PendingIntent.getActivity(
            context,
            (Constants.ACTION_ANSWER + callId).hashCode(),
            answerIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val declinePendingIntent = actionPendingIntent(context, Constants.ACTION_DECLINE, callId)

        return NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(context.applicationInfo.icon)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setOngoing(true)
            .setAutoCancel(false)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .addAction(0, context.getString(R.string.nativecall_decline), declinePendingIntent)
            .addAction(0, context.getString(R.string.nativecall_accept), answerPendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    /**
     * Used only to satisfy the "must call startForeground() within the grace
     * period" contract when the service is revived directly into a stop/answer/
     * decline action (e.g. the process died between ringing and the user
     * tapping a notification action) with no real call state left to show.
     */
    fun buildMinimal(context: Context): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(NotificationManager::class.java)
            if (manager?.getNotificationChannel(FALLBACK_CHANNEL_ID) == null) {
                manager?.createNotificationChannel(
                    NotificationChannel(FALLBACK_CHANNEL_ID, "Call cleanup", NotificationManager.IMPORTANCE_LOW).apply {
                        setSound(null, null)
                        enableVibration(false)
                    },
                )
            }
        }
        return NotificationCompat.Builder(context, FALLBACK_CHANNEL_ID)
            .setContentTitle(context.getString(R.string.nativecall_default_title))
            .setSmallIcon(context.applicationInfo.icon)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(false)
            .build()
    }

    private fun actionPendingIntent(context: Context, action: String, callId: String): PendingIntent {
        val intent = Intent(context, CallActionReceiver::class.java).apply {
            this.action = action
            putExtra(Constants.EXTRA_CALL_ID, callId)
        }
        return PendingIntent.getBroadcast(
            context,
            (action + callId).hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}
