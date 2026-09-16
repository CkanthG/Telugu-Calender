package com.example.telugucalendar.panchangam

import java.util.Calendar
import java.util.TimeZone

/**
 * Single source of truth for "Indian time" across the app. Always use this
 * instead of Calendar.getInstance() / TimeZone.getDefault() for anything that
 * decides what the current panchangam date is — the app should show the same
 * calendar/panchangam regardless of which timezone the device itself is set to.
 */
object IndiaTime {
    val IST: TimeZone = TimeZone.getTimeZone("Asia/Kolkata")

    /** The current date/time in India, regardless of the device's own timezone. */
    fun now(): Calendar = Calendar.getInstance(IST)

    /**
     * Stable "yyyy-MM-dd" key built directly from a Calendar's YEAR/MONTH/DAY_OF_MONTH
     * fields (not from its instant), so it matches whatever calendar day the cell
     * represents regardless of the Calendar object's own hour/timezone quirks.
     */
    fun dateKey(cal: Calendar): String {
        val y = cal.get(Calendar.YEAR)
        val m = cal.get(Calendar.MONTH) + 1
        val d = cal.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }
}
