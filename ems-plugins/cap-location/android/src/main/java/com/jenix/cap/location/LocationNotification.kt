package com.jenix.cap.location

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat

object LocationNotification {
    fun build(context: Context, title: String, body: String): Notification {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel("jenix_location", "Location", NotificationManager.IMPORTANCE_LOW))
        return NotificationCompat.Builder(context, "jenix_location")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .build()
    }
}
