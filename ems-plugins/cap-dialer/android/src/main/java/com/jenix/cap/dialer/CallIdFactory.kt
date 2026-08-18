package com.jenix.cap.dialer

import java.security.MessageDigest

object CallIdFactory {
    fun build(deviceId: String, phoneNumber: String, startedAt: Long, direction: String): String {
        val raw = "$deviceId|$phoneNumber|$startedAt|$direction"
        val digest = MessageDigest.getInstance("SHA-256").digest(raw.toByteArray())
        return digest.take(12).joinToString("") { "%02x".format(it) }
    }
}
