package com.jenix.cap.dialer

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject

object CallLogBridge {
    fun read(context: Context, limit: Int, sinceMs: Long = 0L): List<JSObject> {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) return emptyList()
        val items = mutableListOf<JSObject>()
        val selection = if (sinceMs > 0) "${CallLog.Calls.DATE} > ?" else null
        val args = if (sinceMs > 0) arrayOf(sinceMs.toString()) else null
        context.contentResolver.query(CallLog.Calls.CONTENT_URI, null, selection, args, "${CallLog.Calls.DATE} DESC")?.use { cursor ->
            while (cursor.moveToNext() && items.size < limit) {
                val date = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DATE))
                val duration = cursor.getLong(cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION))
                val type = cursor.getInt(cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE))
                val direction = if (type == CallLog.Calls.OUTGOING_TYPE) "outgoing" else "incoming"
                val status = when (type) {
                    CallLog.Calls.MISSED_TYPE -> "missed"
                    CallLog.Calls.REJECTED_TYPE -> "rejected"
                    CallLog.Calls.BLOCKED_TYPE -> "cancelled"
                    else -> if (duration > 0) "answered" else "failed"
                }
                items += JSObject().apply {
                    put("phoneNumber", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)))
                    put("direction", direction)
                    put("status", status)
                    put("startedAt", java.time.Instant.ofEpochMilli(date).toString())
                    put("answeredAt", if (status == "answered") java.time.Instant.ofEpochMilli(date).toString() else null)
                    put("endedAt", java.time.Instant.ofEpochMilli(date + (duration * 1000)).toString())
                    put("durationSeconds", duration)
                    put("contactName", cursor.getString(cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)))
                }
            }
        }
        return items
    }

    fun toArray(items: List<JSObject>) = JSArray().apply { items.forEach { put(it) } }
}
