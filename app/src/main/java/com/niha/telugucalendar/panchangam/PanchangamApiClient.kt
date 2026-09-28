package com.niha.telugucalendar.panchangam

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar

/**
 * Fetches panchang from FreeAstroAPI (see ApiConfig.kt for the key/endpoint).
 * Returns null on ANY failure (no key set, no network, non-200, bad JSON) so
 * callers can silently fall back to the offline PanchangamCalculator instead
 * of crashing or showing an error for what is, after all, an optional lookup.
 */
object PanchangamApiClient {

    suspend fun fetchOnline(date: Calendar): PanchangamDay? = withContext(Dispatchers.IO) {
        if (ApiConfig.API_KEY.isBlank() || ApiConfig.API_KEY == "YOUR_API_KEY_HERE") {
            return@withContext null // no key configured — caller falls back to offline calc
        }
        try {
            val year = date.get(Calendar.YEAR)
            val month = date.get(Calendar.MONTH) + 1
            val day = date.get(Calendar.DAY_OF_MONTH)

            val requestBody = JSONObject().apply {
                put("year", year)
                put("month", month)
                put("day", day)
                put("hour", 12) // noon local — only affects request_time_panchang (used for rashi)
                put("minute", 0)
                put("lat", ApiConfig.DEFAULT_LAT)
                put("lng", ApiConfig.DEFAULT_LNG)
                put("tz_str", "Asia/Kolkata")
                put("ayanamsha", "lahiri")
            }

            val connection = (URL(ApiConfig.PANCHANG_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                setRequestProperty("Content-Type", "application/json")
                setRequestProperty("x-api-key", ApiConfig.API_KEY)
                doOutput = true
                connectTimeout = 10000
                readTimeout = 10000
            }

            connection.outputStream.use { it.write(requestBody.toString().toByteArray()) }

            if (connection.responseCode != 200) {
                connection.disconnect()
                return@withContext null
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            connection.disconnect()

            parseResponse(date, JSONObject(responseText))
        } catch (e: Exception) {
            null
        }
    }

    private fun parseResponse(date: Calendar, json: JSONObject): PanchangamDay? {
        return try {
            val tithiObj = json.getJSONObject("tithi")
            val tithiNumber = tithiObj.getInt("number") // 1-30
            val tithiIndex = (tithiNumber - 1).coerceIn(0, 29)

            val nakObj = json.getJSONObject("nakshatra")
            val nakshatraIndex = (nakObj.getInt("number") - 1).coerceIn(0, 26)

            val yogaObj = json.getJSONObject("yoga")
            val yogaIndex = (yogaObj.getInt("number") - 1).coerceIn(0, 26)

            // Moon rashi isn't in the top-level sunrise-day fields; it's only in
            // request_time_panchang (the panchang at the specific hour/minute we sent).
            val rashiIndex = json.optJSONObject("request_time_panchang")
                ?.optJSONObject("moon_sign")
                ?.optInt("sign_id", 1)
                ?.minus(1)
                ?.coerceIn(0, 11) ?: 0

            val lunarMonth = json.getJSONObject("lunar_month")
            val monthNumber = lunarMonth.getInt("number") // 1=Chaitra .. 12=Phalguna
            val monthType = lunarMonth.optString("month_type", "regular")

            val varaIndex = date.get(Calendar.DAY_OF_WEEK) - 1

            PanchangamDay(
                date = date,
                tithiIndex = tithiIndex,
                tithiName = TeluguNames.tithiNames[tithiIndex],
                pakshaName = if (tithiIndex < 15) TeluguNames.pakshaNames[0] else TeluguNames.pakshaNames[1],
                isAmavasya = tithiIndex == 29,
                isPournami = tithiIndex == 14,
                nakshatraIndex = nakshatraIndex,
                nakshatraName = TeluguNames.nakshatraNames[nakshatraIndex],
                rashiIndex = rashiIndex,
                rashiName = TeluguNames.rashiNames[rashiIndex],
                yogaIndex = yogaIndex,
                yogaName = TeluguNames.yogaNames[yogaIndex],
                varaName = TeluguNames.varaNames[varaIndex.coerceIn(0, 6)],
                masaName = TeluguNames.masaNamesByNumber[(monthNumber - 1).coerceIn(0, 11)],
                isAdhikaMasa = monthType == "adhika"
            )
        } catch (e: Exception) {
            null
        }
    }
}
