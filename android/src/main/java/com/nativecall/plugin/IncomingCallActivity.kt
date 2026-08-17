package com.nativecall.plugin

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

/**
 * The screen a full-screen-intent notification launches. Wakes a locked
 * device straight to Accept/Decline — no unlocking first, matching a real
 * incoming-call experience.
 *
 * Always renders the native fallback UI (see PLAN.md §4/§7): rendering the
 * host app's own WebView route here — reusing its React call screen instead
 * of this bare layout — is tracked as a roadmap item, not yet implemented.
 * `incomingCallRoute` is accepted and persisted today so a future version can
 * add it without an API change, but the host app is responsible for
 * navigating to it from JS once `callAnswered` fires and the app is foregrounded.
 */
class IncomingCallActivity : AppCompatActivity() {

    private var callId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setShowWhenLockedAndTurnScreenOn()
        setContentView(R.layout.activity_incoming_call)
        bindFrom(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        bindFrom(intent)
    }

    private fun bindFrom(intent: Intent) {
        callId = intent.getStringExtra(Constants.EXTRA_CALL_ID)
        val title = intent.getStringExtra(Constants.EXTRA_TITLE) ?: getString(R.string.nativecall_default_title)
        val body = intent.getStringExtra(Constants.EXTRA_BODY) ?: ""

        findViewById<TextView>(R.id.nativecall_title).text = title
        findViewById<TextView>(R.id.nativecall_body).text = body
        findViewById<Button>(R.id.nativecall_accept_button).setOnClickListener { onAccept() }
        findViewById<Button>(R.id.nativecall_decline_button).setOnClickListener { onDecline() }
    }

    private fun setShowWhenLockedAndTurnScreenOn() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
            )
        }
    }

    private fun onAccept() {
        val id = callId ?: return finish()
        IncomingCallService.sendAction(this, Constants.ACTION_ANSWER, id)
        dismissKeyguard()
        launchHostApp()
        finish()
    }

    private fun onDecline() {
        val id = callId ?: return finish()
        IncomingCallService.sendAction(this, Constants.ACTION_DECLINE, id)
        finish()
    }

    private fun dismissKeyguard() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        }
    }

    /** Brings the host app's own launcher Activity to the foreground so its
     * JS `callAnswered` listener (already wired up by the host) can navigate
     * and connect the call — this plugin has zero opinion on that UI. */
    private fun launchHostApp() {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return
        launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        startActivity(launchIntent)
    }
}
