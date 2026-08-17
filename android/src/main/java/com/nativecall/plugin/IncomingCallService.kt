package com.nativecall.plugin

import android.app.Notification
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

/**
 * Owns the entire audible/haptic lifetime of one incoming call: a looping
 * ringtone, a repeating vibration pattern, and a timeout — independent of the
 * notification shade's single-shot sound (see [RingtoneChannelManager]).
 * Runs as a foreground service so it survives Doze and isn't killed mid-ring.
 */
class IncomingCallService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null
    private val timeoutHandler = Handler(Looper.getMainLooper())
    private var timeoutRunnable: Runnable? = null
    private var currentCallId: String? = null
    private var foregroundStarted = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            Constants.ACTION_START_RINGING -> handleStart(intent)
            Constants.ACTION_STOP_RINGING -> {
                ensureForegroundStartedForShutdown()
                stopRinging(emit = null)
            }
            Constants.ACTION_ANSWER -> {
                ensureForegroundStartedForShutdown()
                stopRinging(emit = NativeCallEvents.Type.ANSWERED)
            }
            Constants.ACTION_DECLINE -> {
                ensureForegroundStartedForShutdown()
                stopRinging(emit = NativeCallEvents.Type.DECLINED)
            }
            else -> {
                ensureForegroundStartedForShutdown()
                stopRinging(emit = null)
            }
        }
        return START_NOT_STICKY
    }

    private fun handleStart(intent: Intent) {
        val callId = intent.getStringExtra(Constants.EXTRA_CALL_ID)
        if (callId == null) {
            Log.e(TAG, "Missing callId, ignoring start request")
            ensureForegroundStartedForShutdown()
            stopRinging(emit = null)
            return
        }
        val title = intent.getStringExtra(Constants.EXTRA_TITLE) ?: getString(R.string.nativecall_default_title)
        val body = intent.getStringExtra(Constants.EXTRA_BODY) ?: ""
        val requestedRingtoneKey = intent.getStringExtra(Constants.EXTRA_RINGTONE_KEY)

        val config = NativeCallConfig.load(applicationContext)
        val ringtone = config?.ringtoneFor(requestedRingtoneKey)
        if (config == null || ringtone == null) {
            Log.e(TAG, "NativeCall.initialize() was never called, or no ringtone matches '$requestedRingtoneKey'. Dropping call $callId.")
            ensureForegroundStartedForShutdown()
            stopRinging(emit = null)
            return
        }

        currentCallId = callId
        val channelId = RingtoneChannelManager.channelId(ringtone.key)
        val notification = CallNotificationBuilder.build(applicationContext, callId, title, body, channelId)
        startForegroundCompat(notification)

        startRingtone(ringtone.resourceName)
        startVibration()
        scheduleTimeout(callId, config.ringDurationSeconds)
    }

    private fun startRingtone(resourceName: String) {
        releaseMediaPlayer()
        val resId = resources.getIdentifier(resourceName, "raw", packageName)
        val uri: Uri? = if (resId != 0) {
            Uri.parse("android.resource://$packageName/$resId")
        } else {
            Log.e(TAG, "Raw resource '$resourceName' not found; falling back to the system default ringtone.")
            RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        }
        if (uri == null) {
            Log.e(TAG, "No ringtone URI available at all; ringing silently (vibration still runs).")
            return
        }
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build(),
            )
            isLooping = true
            try {
                setDataSource(this@IncomingCallService, uri)
                prepare()
                start()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to play ringtone $uri", e)
            }
        }
    }

    private fun startVibration() {
        val pattern = longArrayOf(0, 1000, 1000)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = manager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun scheduleTimeout(callId: String, durationSeconds: Int) {
        cancelTimeout()
        val runnable = Runnable {
            if (currentCallId == callId) {
                stopRinging(emit = NativeCallEvents.Type.TIMED_OUT)
            }
        }
        timeoutRunnable = runnable
        timeoutHandler.postDelayed(runnable, durationSeconds * 1000L)
    }

    private fun cancelTimeout() {
        timeoutRunnable?.let { timeoutHandler.removeCallbacks(it) }
        timeoutRunnable = null
    }

    private fun stopRinging(emit: NativeCallEvents.Type?) {
        val callId = currentCallId
        cancelTimeout()
        releaseMediaPlayer()
        vibrator?.cancel()
        vibrator = null
        if (emit != null && callId != null) {
            NativeCallEvents.emit(applicationContext, emit, callId)
            if (emit == NativeCallEvents.Type.DECLINED) {
                notifyDeclineWebhook(callId)
            }
        }
        currentCallId = null
        stopForegroundCompat()
        stopSelf()
    }

    /** Declining never opens the app (by design), so there's no JS bridge to tell
     * the host's backend the call was declined — it would otherwise just sit
     * ringing until the server's own timeout. If the host app configured a
     * declineWebhookUrl at initialize(), fire a bare POST to it directly from
     * native code so the other party finds out immediately either way. */
    private fun notifyDeclineWebhook(callId: String) {
        val urlTemplate = NativeCallConfig.load(applicationContext)?.declineWebhookUrl ?: return
        val url = urlTemplate.replace("{callId}", callId)
        Thread {
            try {
                (java.net.URL(url).openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "POST"
                    connectTimeout = 10_000
                    readTimeout = 10_000
                    doOutput = false
                    connect()
                    inputStream.close()
                    disconnect()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Decline webhook POST to $url failed", e)
            }
        }.start()
    }

    private fun startForegroundCompat(notification: Notification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(Constants.NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK)
        } else {
            startForeground(Constants.NOTIFICATION_ID, notification)
        }
        foregroundStarted = true
    }

    private fun ensureForegroundStartedForShutdown() {
        if (foregroundStarted) return
        startForegroundCompat(CallNotificationBuilder.buildMinimal(applicationContext))
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun releaseMediaPlayer() {
        mediaPlayer?.let {
            try {
                if (it.isPlaying) it.stop()
            } catch (_: Exception) {
                // Already stopped/released — nothing to do.
            }
            it.release()
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        cancelTimeout()
        releaseMediaPlayer()
        vibrator?.cancel()
        super.onDestroy()
    }

    companion object {
        private const val TAG = "NativeCall"

        fun start(context: Context, callId: String, title: String?, body: String?, ringtoneKey: String?) {
            val intent = Intent(context, IncomingCallService::class.java).apply {
                action = Constants.ACTION_START_RINGING
                putExtra(Constants.EXTRA_CALL_ID, callId)
                title?.let { putExtra(Constants.EXTRA_TITLE, it) }
                body?.let { putExtra(Constants.EXTRA_BODY, it) }
                ringtoneKey?.let { putExtra(Constants.EXTRA_RINGTONE_KEY, it) }
            }
            startCompat(context, intent)
        }

        fun sendAction(context: Context, action: String, callId: String) {
            val intent = Intent(context, IncomingCallService::class.java).apply {
                this.action = action
                putExtra(Constants.EXTRA_CALL_ID, callId)
            }
            startCompat(context, intent)
        }

        private fun startCompat(context: Context, intent: Intent) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }
    }
}
