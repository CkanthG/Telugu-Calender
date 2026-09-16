package com.example.telugucalendar.panchangam

import java.util.Calendar
import kotlin.math.floor

data class PanchangamDay(
    val date: Calendar,
    val tithiIndex: Int,        // 0-29
    val tithiName: String,
    val pakshaName: String,
    val isAmavasya: Boolean,
    val isPournami: Boolean,
    val nakshatraIndex: Int,    // 0-26
    val nakshatraName: String,
    val rashiIndex: Int,        // moon rashi, 0-11
    val rashiName: String,
    val yogaIndex: Int,         // 0-26
    val yogaName: String,
    val varaName: String,
    val masaName: String,
    val isAdhikaMasa: Boolean
)

object PanchangamCalculator {

    // Fixed to India Standard Time so results don't depend on the device's timezone.
    private val IST = java.util.TimeZone.getTimeZone("Asia/Kolkata")

    // Reference location used for sunrise, since panchangam is location-dependent.
    // Defaulted to Hyderabad. If you need a calendar matching a different city
    // (e.g. Vijayawada, which some published Telugu calendars use), change these.
    private const val REF_LATITUDE = 17.385044
    private const val REF_LONGITUDE = 78.486671

    // Approximate Lahiri ayanamsa, linear model anchored near J2000.
    // Real Lahiri ayanamsa has small non-linear terms; this linear approximation
    // is accurate to roughly +/- 0.02-0.05 degree over the 1900-2100 range, which
    // is good enough for tithi/nakshatra work but is still an approximation —
    // verify against a published ayanamsa table if exactness matters to you.
    private fun lahiriAyanamsa(jd: Double): Double {
        val yearsFromJ2000 = (jd - 2451545.0) / 365.25
        return 23.85 + 0.013972 * yearsFromJ2000
    }

    private fun siderealSunLongitude(jd: Double): Double {
        val trop = AstronomyUtils.sunEclipticLongitude(jd)
        return norm360(trop - lahiriAyanamsa(jd))
    }

    private fun siderealMoonLongitude(jd: Double): Double {
        val trop = AstronomyUtils.moonEclipticLongitude(jd)
        return norm360(trop - lahiriAyanamsa(jd))
    }

    private fun norm360(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    /**
     * Full panchangam details for a given local calendar date.
     * Uses the actual sunrise moment (IST, at REF_LATITUDE/REF_LONGITUDE) as the
     * reference instant, matching how published panchangams label a day by
     * whichever tithi/nakshatra/yoga is active at sunrise. This fixed the
     * earlier bug where a fixed "noon" reference could land after a morning
     * transition and show the next tithi a day early.
     */
    fun calculate(date: Calendar): PanchangamDay {
        // Anchor the calendar date itself to IST, ignoring the device's timezone,
        // so results are consistent regardless of where the app is run/tested.
        val midnightIST = Calendar.getInstance(IST).apply {
            set(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH), 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val sunriseIST = AstronomyUtils.approximateSunrise(midnightIST, REF_LATITUDE, REF_LONGITUDE)
        val ref = sunriseIST

        val jd = AstronomyUtils.julianDay(ref)
        val sunLong = siderealSunLongitude(jd)
        val moonLong = siderealMoonLongitude(jd)

        // Tithi: 12-degree segments of (moon - sun) longitude difference.
        val diff = norm360(moonLong - sunLong)
        val tithiIndex = floor(diff / 12.0).toInt().coerceIn(0, 29)
        val paksha = if (tithiIndex < 15) TeluguNames.pakshaNames[0] else TeluguNames.pakshaNames[1]

        // Nakshatra: 27 segments of 13.333... degrees of moon longitude.
        val nakshatraIndex = floor(moonLong / (360.0 / 27.0)).toInt().coerceIn(0, 26)

        // Moon rashi: 30-degree segments of moon longitude.
        val moonRashiIndex = floor(moonLong / 30.0).toInt().coerceIn(0, 11)

        // Sun rashi (used for masa determination).
        val sunRashiIndex = floor(sunLong / 30.0).toInt().coerceIn(0, 11)

        // Yoga: 27 segments of (sun + moon) longitude sum.
        val yogaSum = norm360(sunLong + moonLong)
        val yogaIndex = floor(yogaSum / (360.0 / 27.0)).toInt().coerceIn(0, 26)

        val varaIndex = date.get(Calendar.DAY_OF_WEEK) - 1 // Calendar.SUNDAY = 1
        val varaName = TeluguNames.varaNames[varaIndex.coerceIn(0, 6)]

        val masaName = TeluguNames.masaNamesByRashi[sunRashiIndex]
        val isAdhika = isAdhikaMasaFor(ref)

        return PanchangamDay(
            date = date,
            tithiIndex = tithiIndex,
            tithiName = TeluguNames.tithiNames[tithiIndex],
            pakshaName = paksha,
            isAmavasya = tithiIndex == 29,
            isPournami = tithiIndex == 14,
            nakshatraIndex = nakshatraIndex,
            nakshatraName = TeluguNames.nakshatraNames[nakshatraIndex],
            rashiIndex = moonRashiIndex,
            rashiName = TeluguNames.rashiNames[moonRashiIndex],
            yogaIndex = yogaIndex,
            yogaName = TeluguNames.yogaNames[yogaIndex],
            varaName = varaName,
            masaName = masaName,
            isAdhikaMasa = isAdhika
        )
    }

    /**
     * Finds the Julian Day of the new moon (amavasya, i.e. moon-sun longitude
     * difference = 0/360) nearest to and before the given approximate JD, using
     * bisection on the wrapped longitude difference. Approximate — see file header
     * caveats on overall astronomical precision.
     */
    private fun previousNewMoonJD(approxJD: Double): Double {
        var jd = approxJD
        // Step backward in half-day increments until we cross a 0-degree boundary.
        var prevDiff = signedDiff(jd)
        var step = 0.5
        var searchJD = jd
        var found = false
        var lowJD = jd
        var highJD = jd
        repeat(120) {
            if (found) return@repeat
            searchJD -= step
            val d = signedDiff(searchJD)
            if (d > prevDiff) {
                // crossed the wrap-around point going backward
                lowJD = searchJD
                highJD = searchJD + step
                found = true
            }
            prevDiff = d
        }
        if (!found) return approxJD - 29.53 // fallback: synodic month estimate

        // Bisect between lowJD and highJD to refine the zero-crossing.
        var lo = lowJD
        var hi = highJD
        repeat(30) {
            val mid = (lo + hi) / 2.0
            val dMid = signedDiff(mid)
            val dHi = signedDiff(hi)
            if ((dMid > 180) == (dHi > 180)) {
                hi = mid
            } else {
                lo = mid
            }
        }
        return (lo + hi) / 2.0
    }

    private fun signedDiff(jd: Double): Double {
        val sunLong = siderealSunLongitude(jd)
        val moonLong = siderealMoonLongitude(jd)
        return norm360(moonLong - sunLong)
    }

    /**
     * Approximate adhika masa (leap month) detection: finds the amavasya
     * before and after the reference date; if the Sun stays within the same
     * rashi across both amavasyas (i.e. no sankranti occurred in between),
     * the lunar month is flagged as adhika masa. This is a simplified
     * heuristic — cross-check against a published panchangam for confirmation,
     * especially near suspected adhika masa years (they occur roughly once
     * every 32-33 months).
     */
    private fun isAdhikaMasaFor(ref: Calendar): Boolean {
        val jd = AstronomyUtils.julianDay(ref)
        val prevNewMoon = previousNewMoonJD(jd)
        val nextNewMoon = previousNewMoonJD(jd + 29.53 + 2.0) // search past the next one too, roughly
        val sunRashiAtPrev = floor(siderealSunLongitude(prevNewMoon) / 30.0).toInt()
        val sunRashiAtNext = floor(siderealSunLongitude(nextNewMoon) / 30.0).toInt()
        return sunRashiAtPrev == sunRashiAtNext
    }
}
