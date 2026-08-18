package com.jenix.cap.dialer

import android.telecom.Call
import android.telecom.InCallService

class FieldForceInCallService : InCallService() {
    private lateinit var prefs: DialerPrefs
    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            val number = call.details.handle?.schemeSpecificPart
            val direction = if (call.details.callDirection == Call.Details.DIRECTION_OUTGOING) "outgoing" else "incoming"
            val active = state != Call.STATE_DISCONNECTED
            prefs.saveState(active, number, direction, java.time.Instant.now().toString())
            if (!active) call.unregisterCallback(this)
        }
    }

    override fun onCreate() {
        super.onCreate()
        prefs = DialerPrefs(this)
    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        call.registerCallback(callback)
        callback.onStateChanged(call, call.state)
    }

    override fun onCallRemoved(call: Call) {
        prefs.saveState(false, null, null, null)
        call.unregisterCallback(callback)
        super.onCallRemoved(call)
    }
}
