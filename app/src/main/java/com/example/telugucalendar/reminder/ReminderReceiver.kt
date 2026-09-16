package com.example.telugucalendar.reminder

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.telugucalendar.MainActivity

/**
 * Fires when a scheduled reminder alarm triggers. Shows a notification with
 * the title/message the user entered when setting the reminder.
 */
class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Telugu Calendar Reminder"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
        val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, 0)

        NotificationHelper.createChannel(context)

        val contentIntent = Intent(context, MainActivity::class.java)
        val contentPendingIntent = PendingIntent.getActivity(
            context, requestCode, contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        // On API 33+ POST_NOTIFICATIONS is a runtime permission; if the user
        // never granted it, skip silently rather than crashing.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED
        ) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.notify(requestCode, notification)
        }
    }

    companion object {
        const val EXTRA_TITLE = "extra_title"
        const val EXTRA_MESSAGE = "extra_message"
        const val EXTRA_REQUEST_CODE = "extra_request_code"
    }
}
