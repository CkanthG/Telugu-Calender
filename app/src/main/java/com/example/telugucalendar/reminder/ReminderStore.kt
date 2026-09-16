package com.example.telugucalendar.reminder

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class Reminder(
    val id: Int,          // also used as the AlarmManager/PendingIntent request code
    val dateKey: String,  // "yyyy-MM-dd" (Indian calendar date this reminder belongs to)
    val timeMillis: Long, // exact trigger instant (device wall-clock instant)
    val note: String
)

/**
 * Stores reminders as a JSON array in SharedPreferences. Deliberately simple
 * (no Room/DB dependency) since reminder volume for a calendar app is small.
 */
object ReminderStore {
    private const val PREFS = "telugu_calendar_reminders"
    private const val KEY_LIST = "reminders_json"

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    private fun readAll(context: Context): MutableList<Reminder> {
        val json = prefs(context).getString(KEY_LIST, null) ?: return mutableListOf()
        val arr = JSONArray(json)
        val list = mutableListOf<Reminder>()
        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            list.add(
                Reminder(
                    id = o.getInt("id"),
                    dateKey = o.getString("dateKey"),
                    timeMillis = o.getLong("timeMillis"),
                    note = o.optString("note", "")
                )
            )
        }
        return list
    }

    private fun writeAll(context: Context, reminders: List<Reminder>) {
        val arr = JSONArray()
        reminders.forEach {
            val o = JSONObject()
            o.put("id", it.id)
            o.put("dateKey", it.dateKey)
            o.put("timeMillis", it.timeMillis)
            o.put("note", it.note)
            arr.put(o)
        }
        prefs(context).edit().putString(KEY_LIST, arr.toString()).apply()
    }

    fun add(context: Context, reminder: Reminder) {
        val all = readAll(context)
        all.removeAll { it.id == reminder.id } // replace if same id reused
        all.add(reminder)
        writeAll(context, all)
    }

    fun remove(context: Context, id: Int) {
        val all = readAll(context)
        all.removeAll { it.id == id }
        writeAll(context, all)
    }

    fun getForDate(context: Context, dateKey: String): List<Reminder> {
        return readAll(context).filter { it.dateKey == dateKey }.sortedBy { it.timeMillis }
    }

    fun countForDate(context: Context, dateKey: String): Int {
        return readAll(context).count { it.dateKey == dateKey }
    }
}
