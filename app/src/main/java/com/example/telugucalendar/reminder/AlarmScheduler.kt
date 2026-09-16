package com.example.telugucalendar.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import java.util.Calendar

/**
 * Schedules a one-off exact alarm for a given date/time using AlarmManager.
 *
 * NOTE: On Android 12+ (API 31+), scheduling *exact* alarms requires the
 * SCHEDULE_EXACT_ALARM permission (declared in the manifest) and, on some
 * OEM builds, the user may need to grant "Alarms & reminders" access manually
 * in system settings if canScheduleExactAlarms() returns false. This code
 * falls back to a non-exact alarm in that case so it still fires, just
 * possibly a little later than requested.
 */
object AlarmScheduler {

    fun schedule(
        context: Context,
        requestCode: Int,
        triggerAtMillis: Long,
        title: String,
        message: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra(ReminderReceiver.EXTRA_TITLE, title)
            putExtra(ReminderReceiver.EXTRA_MESSAGE, message)
            putExtra(ReminderReceiver.EXTRA_REQUEST_CODE, requestCode)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val canScheduleExact = Build.VERSION.SDK_INT < Build.VERSION_CODES.S ||
                alarmManager.canScheduleExactAlarms()

        if (canScheduleExact) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent
            )
        } else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent)
        }
    }

    fun cancel(context: Context, requestCode: Int) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /** True if exact alarms need the user to grant permission via system settings (API 31+ only). */
    fun needsExactAlarmPermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return false
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        return !alarmManager.canScheduleExactAlarms()
    }
}
