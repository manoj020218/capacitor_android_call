package com.jenix.cap.core

import android.content.Context
import android.provider.Settings

object DeviceIdentity {
    fun getDeviceId(context: Context): String {
        return Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?: "unknown-device"
    }
}
