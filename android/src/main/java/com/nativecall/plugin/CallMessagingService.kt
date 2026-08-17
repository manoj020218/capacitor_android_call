package com.nativecall.plugin

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Default FCM listener for apps with no other `FirebaseMessagingService`.
 * If the host app already runs one (e.g. `@capacitor/push-notifications`),
 * remove this service from the merged manifest with `tools:node="remove"`
 * and call [NativeCallMessageHandler.handleData] from that existing service
 * instead — only one `FirebaseMessagingService` can be registered per app;
 * see README "Integrating with an existing FCM listener".
 */
class CallMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        NativeCallMessageHandler.handleData(applicationContext, message.data)
    }
}

object NativeCallMessageHandler {
    private const val TAG = "NativeCall"

    /**
     * @return `true` if `data` was an `incoming_call` payload and this
     * plugin started ringing for it, `false` if it was some other message
     * type the caller should keep handling itself.
     */
    fun handleData(context: Context, data: Map<String, String>): Boolean {
        if (data["type"] != Constants.FCM_TYPE_INCOMING_CALL) return false

        val callId = data["callId"]
        if (callId == null) {
            Log.e(TAG, "Received an incoming_call message with no callId; ignoring.")
            return false
        }

        IncomingCallService.start(
            context = context,
            callId = callId,
            title = data["title"],
            body = data["body"],
            ringtoneKey = data["ringtone"],
        )
        return true
    }
}
