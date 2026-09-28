package com.niha.telugucalendar.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.niha.telugucalendar.ads.AdConfig
import com.niha.telugucalendar.ads.AdManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * Fixed-size banner (AdSize.BANNER 320x50, AdSize.MEDIUM_RECTANGLE 300x250, etc).
 * Waits for SDK initialization, skips itself if the ad unit is still an "XXXX"
 * placeholder, and destroys the AdView when it leaves the composition.
 */
@Composable
fun BannerAd(
    adUnitId: String,
    adSize: AdSize,
    modifier: Modifier = Modifier
) {
    val sdkReady by AdManager.sdkReady
    if (!AdConfig.isConfigured(adUnitId)) {
        AdPlaceholder("Banner — set your production ad unit ID in AdConfig", 50.dp, modifier)
        return
    }
    if (!sdkReady) {
        Spacer(modifier = modifier.height(adSize.height.dp)) // reserve space, avoids layout jump
        return
    }

    val context = LocalContext.current
    val adView = remember(adUnitId) {
        AdView(context).apply {
            setAdSize(adSize)
            this.adUnitId = adUnitId
            loadAd(AdRequest.Builder().build())
        }
    }
    DisposableEffect(adView) { onDispose { adView.destroy() } }

    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        AndroidView(factory = { adView })
    }
}

/** Visible stand-in for ad formats that aren't wired up yet (native, rewarded...). */
@Composable
fun AdPlaceholder(label: String, height: Dp = 100.dp, modifier: Modifier = Modifier) {
    if (!AdConfig.SHOW_PLACEHOLDER_SLOTS) return
    Surface(
        modifier = modifier.fillMaxWidth().height(height),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
            Text("📢 $label", style = MaterialTheme.typography.labelMedium)
        }
    }
}
