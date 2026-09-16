package com.example.telugucalendar.reminder

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

object NotificationHelper {
    const val CHANNEL_ID = "telugu_calendar_reminders"

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Calendar Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders you set from Telugu Calendar"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}
