package com.nativecall.plugin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

/**
 * One notification channel per ringtone key, registered up front at
 * `initialize()`. Android locks a channel's sound/importance in at creation
 * and ignores later per-message overrides (see PLAN.md §1.2) — pre-registering
 * per choice is the only way to offer more than one ringtone at all.
 *
 * Channels are created silent (`setSound(null, null)`, vibration disabled):
 * the foreground [IncomingCallService] owns 100% of the audible ringtone and
 * vibration loop directly via MediaPlayer/Vibrator, so a call rings until
 * answered instead of the OS's single-shot channel sound (PLAN.md §1.3).
 */
internal object RingtoneChannelManager {

    fun channelId(ringtoneKey: String): String = "native_call_$ringtoneKey"

    fun registerChannels(context: Context, ringtones: List<RingtoneOption>) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = context.getSystemService(NotificationManager::class.java) ?: return
        ringtones.forEach { ringtone ->
            val channel = NotificationChannel(
                channelId(ringtone.key),
                "Incoming calls (${ringtone.key})",
                NotificationManager.IMPORTANCE_HIGH,
            ).apply {
                description = "Full-screen incoming call notifications using the '${ringtone.key}' ringtone"
                setSound(null, null)
                enableVibration(false)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            manager.createNotificationChannel(channel)
        }
    }
}
