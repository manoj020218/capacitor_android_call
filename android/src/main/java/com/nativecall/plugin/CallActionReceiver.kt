package com.nativecall.plugin

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Handles Accept/Decline tapped directly from the notification tray (the
 * heads-up notification, or the shade after the screen was manually woken) —
 * the path that doesn't go through [IncomingCallActivity] at all.
 */
class CallActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val callId = intent.getStringExtra(Constants.EXTRA_CALL_ID) ?: return
        when (intent.action) {
            Constants.ACTION_ANSWER -> IncomingCallService.sendAction(context, Constants.ACTION_ANSWER, callId)
            Constants.ACTION_DECLINE -> IncomingCallService.sendAction(context, Constants.ACTION_DECLINE, callId)
        }
    }
}
