package com.niha.telugucalendar.ads

/**
 * Central AdMob configuration. Test IDs below are Google's official sample
 * IDs (https://developers.google.com/admob/android/test-ads).
 *
 * BEFORE RELEASE:
 *  1. Set USE_TEST_ADS = false
 *  2. Replace every PROD_* value with your real ad unit IDs from the AdMob console
 *     (create one ad unit per placement — AdMob recommends this for reporting)
 *  3. Replace admob_app_id in res/values/ads.xml with your real AdMob App ID
 *  Serving your own live ads while testing can get your AdMob account suspended.
 */
object AdConfig {

    const val USE_TEST_ADS = true

    // ---- Placement on/off switches (flip any to false to remove that ad) ----
    const val SHOW_CALENDAR_BOTTOM_BANNER = true
    const val SHOW_CALENDAR_INLINE_BANNER = true
    const val SHOW_DETAIL_INLINE_BANNER = true
    const val SHOW_DETAIL_BOTTOM_BANNER = true
    const val SHOW_INTERSTITIAL_ON_BACK = true
    const val SHOW_PLACEHOLDER_SLOTS = true // visible boxes for native / rewarded (not wired yet)

    // ---- Interstitial frequency cap (avoid annoying users / policy issues) ----
    const val INTERSTITIAL_EVERY_N_BACKS = 3          // show on every 3rd "back" from a day
    const val INTERSTITIAL_MIN_INTERVAL_MS = 60_000L  // and never more than once a minute

    // Paste your device's hashed ID here (logcat prints it as
    // "Use RequestConfiguration.Builder().setTestDeviceIds(...)") to get test ads
    // even when using your real ad unit IDs. Remove before release.
    val TEST_DEVICE_IDS: List<String> = listOf(/* "ABCDEF012345" */)

    // ---- Google sample (test) ad unit IDs — Android ----
    private const val TEST_BANNER_FIXED = "ca-app-pub-3940256099942544/6300978111"
    const val TEST_BANNER_ADAPTIVE = "ca-app-pub-3940256099942544/9214589741" // for adaptive banners (not used yet)
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_APP_OPEN = "ca-app-pub-3940256099942544/9257395921"

    // ---- YOUR production ad unit IDs (placeholders — replace) ----
    private const val PROD_BANNER_CALENDAR_BOTTOM = "ca-app-pub-XXXXXXXXXXXXXXXX/1111111111"
    private const val PROD_BANNER_CALENDAR_INLINE = "ca-app-pub-XXXXXXXXXXXXXXXX/1111111112"
    private const val PROD_BANNER_DETAIL_INLINE = "ca-app-pub-XXXXXXXXXXXXXXXX/2222222222"
    private const val PROD_BANNER_DETAIL_BOTTOM = "ca-app-pub-XXXXXXXXXXXXXXXX/3333333333"
    private const val PROD_INTERSTITIAL_BACK = "ca-app-pub-XXXXXXXXXXXXXXXX/4444444444"
    private const val PROD_NATIVE_DETAIL = "ca-app-pub-XXXXXXXXXXXXXXXX/5555555555"
    private const val PROD_REWARDED_SUPPORT = "ca-app-pub-XXXXXXXXXXXXXXXX/6666666666"
    private const val PROD_APP_OPEN = "ca-app-pub-XXXXXXXXXXXXXXXX/7777777777"

    // ---- Resolved IDs (test vs production) ----
    val bannerCalendarBottom get() = if (USE_TEST_ADS) TEST_BANNER_FIXED else PROD_BANNER_CALENDAR_BOTTOM
    val bannerCalendarInline get() = if (USE_TEST_ADS) TEST_BANNER_FIXED else PROD_BANNER_CALENDAR_INLINE
    val bannerDetailInline get() = if (USE_TEST_ADS) TEST_BANNER_FIXED else PROD_BANNER_DETAIL_INLINE
    val bannerDetailBottom get() = if (USE_TEST_ADS) TEST_BANNER_FIXED else PROD_BANNER_DETAIL_BOTTOM
    val interstitialBack get() = if (USE_TEST_ADS) TEST_INTERSTITIAL else PROD_INTERSTITIAL_BACK
    val nativeDetail get() = if (USE_TEST_ADS) TEST_NATIVE else PROD_NATIVE_DETAIL
    val rewardedSupport get() = if (USE_TEST_ADS) TEST_REWARDED else PROD_REWARDED_SUPPORT
    val appOpen get() = if (USE_TEST_ADS) TEST_APP_OPEN else PROD_APP_OPEN
    val rewardedInterstitial get() = TEST_REWARDED_INTERSTITIAL // placeholder only

    /** False while a production ID is still the "XXXX" placeholder, so we never send bogus requests. */
    fun isConfigured(adUnitId: String) = !adUnitId.contains("XXXX")
}
