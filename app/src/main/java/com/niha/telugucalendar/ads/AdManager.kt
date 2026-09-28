package com.niha.telugucalendar.ads

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

/** Walks the ContextWrapper chain to find the hosting Activity (needed to show full-screen ads). */
fun Context.findActivity(): Activity? {
    var c: Context = this
    while (c is ContextWrapper) {
        if (c is Activity) return c
        c = c.baseContext
    }
    return null
}

/**
 * Initializes the Mobile Ads SDK (once) and manages a single preloaded interstitial.
 * Pattern follows Google's docs: set RequestConfiguration first, initialize off the
 * main thread, then make ad calls on the main thread.
 */
object AdManager {
    private const val TAG = "AdManager"

    private val initStarted = AtomicBoolean(false)

    /** Compose-observable: banners wait for this before requesting ads. */
    val sdkReady = mutableStateOf(false)

    private var interstitialAd: InterstitialAd? = null
    private var isLoadingInterstitial = false
    private var backCount = 0
    private var lastInterstitialShownAt = 0L

    fun initialize(activity: Activity) {
        if (initStarted.getAndSet(true)) return

        if (AdConfig.TEST_DEVICE_IDS.isNotEmpty()) {
            MobileAds.setRequestConfiguration(
                RequestConfiguration.Builder().setTestDeviceIds(AdConfig.TEST_DEVICE_IDS).build()
            )
        }

        val appContext = activity.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            // Initialize the Google Mobile Ads SDK on a background thread.
            MobileAds.initialize(appContext) {
                sdkReady.value = true
            }
            // Ad loading must happen on the main thread.
            activity.runOnUiThread { loadInterstitial(activity) }
        }
    }

    private fun loadInterstitial(context: Context) {
        val unitId = AdConfig.interstitialBack
        if (!AdConfig.SHOW_INTERSTITIAL_ON_BACK || !AdConfig.isConfigured(unitId)) return
        if (isLoadingInterstitial || interstitialAd != null) return
        isLoadingInterstitial = true

        InterstitialAd.load(
            context,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    Log.d(TAG, "Interstitial loaded.")
                    interstitialAd = ad
                    isLoadingInterstitial = false
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d(TAG, "Interstitial failed: ${adError.message}")
                    interstitialAd = null
                    isLoadingInterstitial = false
                }
            }
        )
    }

    /**
     * Call when the user leaves a day-detail screen. Shows an interstitial only if:
     * enabled, one is loaded, it's every Nth call, and the minimum interval has passed.
     * [onDone] ALWAYS runs exactly once (after the ad closes, or immediately if none shown),
     * so navigation is never blocked by ad problems.
     */
    fun showInterstitialThen(activity: Activity, onDone: () -> Unit) {
        backCount++
        val now = SystemClock.elapsedRealtime()
        val ad = interstitialAd
        val allowed = AdConfig.SHOW_INTERSTITIAL_ON_BACK &&
                backCount % AdConfig.INTERSTITIAL_EVERY_N_BACKS == 0 &&
                now - lastInterstitialShownAt >= AdConfig.INTERSTITIAL_MIN_INTERVAL_MS

        if (allowed && ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    interstitialAd = null // a loaded ad can only be shown once
                    lastInterstitialShownAt = SystemClock.elapsedRealtime()
                    loadInterstitial(activity) // preload the next one
                    onDone()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    interstitialAd = null
                    loadInterstitial(activity)
                    onDone()
                }
            }
            ad.show(activity)
        } else {
            if (ad == null) loadInterstitial(activity)
            onDone()
        }
    }
}
