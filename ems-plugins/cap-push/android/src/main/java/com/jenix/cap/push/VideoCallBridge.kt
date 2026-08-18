package com.jenix.cap.push

import android.content.Context

object VideoCallBridge {
    fun isAvailable(): Boolean {
        return try {
            Class.forName("com.nativecall.plugin.NativeCallMessageHandler")
            true
        } catch (_: Exception) {
            false
        }
    }

    fun forward(context: Context, payload: Map<String, String>): Boolean {
        return try {
            val clazz = Class.forName("com.nativecall.plugin.NativeCallMessageHandler")
            val method = clazz.getMethod("handleData", Context::class.java, Map::class.java)
            method.invoke(null, context, payload) as? Boolean ?: false
        } catch (_: Exception) {
            false
        }
    }
}
