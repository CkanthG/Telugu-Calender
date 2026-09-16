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
}
