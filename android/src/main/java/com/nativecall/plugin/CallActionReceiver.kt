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
            Constants.ACTION_ANSWER -> {
                IncomingCallService.sendAction(context, Constants.ACTION_ANSWER, callId)
                launchHostApp(context)
            }
            Constants.ACTION_DECLINE -> IncomingCallService.sendAction(context, Constants.ACTION_DECLINE, callId)
        }
    }

    /** Mirrors [IncomingCallActivity.launchHostApp] — this path has no window of its
     * own to dismiss the keyguard from, but still needs to foreground the host app so
     * its JS `callAnswered` listener can navigate and connect the call. */
    private fun launchHostApp(context: Context) {
        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        context.startActivity(launchIntent)
    }
}
