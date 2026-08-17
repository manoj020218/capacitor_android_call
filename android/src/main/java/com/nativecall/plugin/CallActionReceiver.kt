package com.nativecall.plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handles Decline tapped directly from the notification tray. Accept is
 * handled by [IncomingCallActivity] instead (via a PendingIntent.getActivity,
 * not a broadcast) — Android 12+ blocks a BroadcastReceiver from calling
 * startActivity() itself as a "notification trampoline," which declining
 * doesn't need since it never opens an Activity.
 */
class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getStringExtra(Constants.EXTRA_CALL_ID) ?: return
        if (intent.action == Constants.ACTION_DECLINE) {
            IncomingCallService.sendAction(context, Constants.ACTION_DECLINE, callId)
        }
    }
}
