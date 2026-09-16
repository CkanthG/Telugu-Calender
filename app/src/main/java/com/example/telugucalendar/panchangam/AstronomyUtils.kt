package com.example.telugucalendar.panchangam

import java.util.Calendar
import java.util.TimeZone
import kotlin.math.*

/**
 * NOTE ON ACCURACY (please read):
 * These are LOW-to-MEDIUM precision approximations of solar and lunar ecliptic
 * longitude, based on standard, widely published astronomical series (the kind
 * found in general astronomical-algorithms references). They are NOT full-precision
 * ephemeris calculations (like Swiss Ephemeris or NASA JPL data), so results can be
 * off from a professional panchangam by up to roughly tens of minutes, and
 * occasionally close to an hour right at a tithi/nakshatra boundary transition.
 * If you need ritual-grade accuracy, verify against a trusted panchangam source,
 * or later swap this engine for a proper ephemeris library.
 */
object AstronomyUtils {

    /** Julian Day Number (UT) for a given UTC calendar instant. */
    fun julianDay(cal: Calendar): Double {
        val utc = cal.clone() as Calendar
        utc.timeZone = TimeZone.getTimeZone("UTC")
        val year = utc.get(Calendar.YEAR)
        val month = utc.get(Calendar.MONTH) + 1
        val day = utc.get(Calendar.DAY_OF_MONTH)
        val hour = utc.get(Calendar.HOUR_OF_DAY)
        val minute = utc.get(Calendar.MINUTE)
        val second = utc.get(Calendar.SECOND)

        val dayFraction = day + (hour + minute / 60.0 + second / 3600.0) / 24.0
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + dayFraction + b - 1524.5
    }

    private fun norm360(deg: Double): Double {
        var d = deg % 360.0
        if (d < 0) d += 360.0
        return d
    }

    /**
     * Apparent geocentric ecliptic longitude of the Sun, in degrees (0-360).
     * Based on the standard low-precision solar position series
     * (mean longitude + equation of center), accuracy ~0.01-0.1 degree.
     */
    fun sunEclipticLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0
        val l0 = norm360(280.46646 + 36000.76983 * t + 0.0003032 * t * t)
        val m = norm360(357.52911 + 35999.05029 * t - 0.0001537 * t * t)
        val mRad = Math.toRadians(m)
        val c = (1.914602 - 0.004817 * t - 0.000014 * t * t) * sin(mRad) +
                (0.019993 - 0.000101 * t) * sin(2 * mRad) +
                0.000289 * sin(3 * mRad)
        val trueLong = l0 + c
        val omega = 125.04 - 1934.136 * t
        val apparentLong = trueLong - 0.00569 - 0.00478 * sin(Math.toRadians(omega))
        return norm360(apparentLong)
    }

    /**
     * Geocentric ecliptic longitude of the Moon, in degrees (0-360).
     * Truncated periodic-term series (main terms only) — approximate,
     * typical accuracy on the order of a few tenths of a degree.
     */
    fun moonEclipticLongitude(jd: Double): Double {
        val t = (jd - 2451545.0) / 36525.0

        val lPrime = norm360(218.3164477 + 481267.88123421 * t - 0.0015786 * t * t)
        val d = norm360(297.8501921 + 445267.1114034 * t - 0.0018819 * t * t)
        val m = norm360(357.5291092 + 35999.0502909 * t - 0.0001536 * t * t)
        val mPrime = norm360(134.9633964 + 477198.8675055 * t + 0.0089970 * t * t)
        val f = norm360(93.2720950 + 483202.0175233 * t - 0.0036539 * t * t)

        val dR = Math.toRadians(d)
        val mR = Math.toRadians(m)
        val mpR = Math.toRadians(mPrime)
        val fR = Math.toRadians(f)

        // Main periodic terms for longitude correction (degrees), largest terms only.
        var sumL = 0.0
        sumL += 6.288774 * sin(mpR)
        sumL += 1.274027 * sin(2 * dR - mpR)
        sumL += 0.658314 * sin(2 * dR)
        sumL += 0.213618 * sin(2 * mpR)
        sumL += -0.185116 * sin(mR)
        sumL += -0.114332 * sin(2 * fR)
        sumL += 0.058793 * sin(2 * dR - 2 * mpR)
        sumL += 0.057066 * sin(2 * dR - mR - mpR)
        sumL += 0.053322 * sin(2 * dR + mpR)
        sumL += 0.045758 * sin(2 * dR - mR)
        sumL += -0.040923 * sin(mR - mpR)
        sumL += -0.034720 * sin(dR)
        sumL += -0.030383 * sin(mR + mpR)
        sumL += 0.015327 * sin(2 * dR - 2 * fR)
        sumL += -0.012528 * sin(mpR + 2 * fR)
        sumL += 0.010980 * sin(mpR - 2 * fR)
        sumL += 0.010675 * sin(4 * dR - mpR)
        sumL += 0.010034 * sin(3 * mpR)
        sumL += 0.008548 * sin(4 * dR - 2 * mpR)

        return norm360(lPrime + sumL)
    }

    /** Sunrise time (approximate, local) for a given date and lat/lon, as Calendar in local timezone. */
    fun approximateSunrise(cal: Calendar, latitude: Double, longitude: Double): Calendar {
        // Simple approximate sunrise algorithm (NOAA-style, low precision).
        val zenith = 90.833 // official zenith for sunrise/sunset
        val local = cal.clone() as Calendar
        val dayOfYear = local.get(Calendar.DAY_OF_YEAR)

        val lngHour = longitude / 15.0
        val t = dayOfYear + ((6 - lngHour) / 24)

        val mAnomaly = (0.9856 * t) - 3.289
        var trueLong = mAnomaly + (1.916 * sin(Math.toRadians(mAnomaly))) +
                (0.020 * sin(Math.toRadians(2 * mAnomaly))) + 282.634
        trueLong = norm360(trueLong)

        var raAngle = Math.toDegrees(atan(0.91764 * tan(Math.toRadians(trueLong))))
        raAngle = norm360(raAngle)
        val lQuadrant = floor(trueLong / 90.0) * 90.0
        val raQuadrant = floor(raAngle / 90.0) * 90.0
        raAngle = raAngle + (lQuadrant - raQuadrant)
        raAngle /= 15.0

        val sinDec = 0.39782 * sin(Math.toRadians(trueLong))
        val cosDec = cos(asin(sinDec))
        val cosH = (cos(Math.toRadians(zenith)) - (sinDec * sin(Math.toRadians(latitude)))) /
                (cosDec * cos(Math.toRadians(latitude)))

        if (cosH > 1 || cosH < -1) {
            // Sun never rises/sets on this date at this latitude; fall back to 6:00 local.
            local.set(Calendar.HOUR_OF_DAY, 6)
            local.set(Calendar.MINUTE, 0)
            local.set(Calendar.SECOND, 0)
            return local
        }

        var hAngle = Math.toDegrees(acos(cosH))
        hAngle = 360 - hAngle // rising
        hAngle /= 15.0

        val localT = hAngle + raAngle - (0.06571 * t) - 6.622
        var ut = localT - lngHour
        ut = ((ut % 24) + 24) % 24

        val tzOffsetHours = local.timeZone.getOffset(local.timeInMillis) / 3600000.0
        var localTime = ut + tzOffsetHours
        localTime = ((localTime % 24) + 24) % 24

        val hours = floor(localTime).toInt()
        val minutes = floor((localTime - hours) * 60).toInt()

        local.set(Calendar.HOUR_OF_DAY, hours)
        local.set(Calendar.MINUTE, minutes)
        local.set(Calendar.SECOND, 0)
        return local
    }
}
