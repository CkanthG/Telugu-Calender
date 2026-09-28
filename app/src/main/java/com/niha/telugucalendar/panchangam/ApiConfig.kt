package com.niha.telugucalendar.panchangam

/**
 * FreeAstroAPI Panchang v2 — https://www.freeastroapi.com/docs/vedic/panchang
 *
 * Get a free API key at https://www.freeastroapi.com (their site says "free in
 * 10s", no card needed to start). Paste it below. I could not confirm their
 * exact free-tier monthly request limit from the docs page — check their
 * pricing page if you plan on heavy usage.
 */
object ApiConfig {
    const val PANCHANG_URL = "https://api.freeastroapi.com/api/v2/vedic/panchang"
    const val API_KEY = "e8c699069d5f558e73a1d4cd5182853fe7ddc27ba5c917b175912e3a1b057e9b" // <-- replace with your real key

    // Reference location sent with each request — same default as the offline
    // engine (Hyderabad). Change if you want a different city's panchang.
    const val DEFAULT_LAT = 17.385044
    const val DEFAULT_LNG = 78.486671
}
