package com.ponslink.capturebrain.ui

import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.ponslink.capturebrain.R
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

private const val ADMOB_NATIVE_AD_UNIT_ID = "ca-app-pub-4559332868732922/7477192034"
private const val ADMOB_NATIVE_TEST_AD_UNIT_ID = "ca-app-pub-3940256099942544/2247696110"

/** Set to true during development; flip to false for production. */
private const val USE_TEST_AD_UNIT = false

/**
 * Native Ad card that blends into the CaptureBrain dashboard.
 * Loads a native ad via AdLoader and binds it to ad_native_card layout.
 * Collapses to nothing if the ad fails to load.
 */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var nativeAd by remember { mutableStateOf<NativeAd?>(null) }
    var adLoaded by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        val adUnitId = if (USE_TEST_AD_UNIT) ADMOB_NATIVE_TEST_AD_UNIT_ID else ADMOB_NATIVE_AD_UNIT_ID
        val mainScope = MainScope()

        val adLoader = AdLoader.Builder(context, adUnitId)
            .forNativeAd { ad ->
                // Ensure state update happens on main thread for Compose recomposition
                mainScope.launch {
                    nativeAd = ad
                    adLoaded = true
                }
            }
            .withNativeAdOptions(
                com.google.android.gms.ads.nativead.NativeAdOptions.Builder()
                    .setAdChoicesPlacement(
                        com.google.android.gms.ads.nativead.NativeAdOptions.ADCHOICES_TOP_RIGHT
                    )
                    .build()
            )
            .withAdListener(object : com.google.android.gms.ads.AdListener() {
                override fun onAdFailedToLoad(error: LoadAdError) {
                    mainScope.launch {
                        adLoaded = false
                    }
                }
            })
            .build()

        adLoader.loadAd(AdRequest.Builder().build())

        onDispose {
            nativeAd?.destroy()
        }
    }

    if (adLoaded && nativeAd != null) {
        Card(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CbAd.Panel)
        ) {
            AndroidView(
                factory = { ctx ->
                    val view = LayoutInflater.from(ctx)
                        .inflate(R.layout.ad_native_card, null) as NativeAdView

                    view.headlineView = view.findViewById(R.id.ad_headline)
                    view.bodyView = view.findViewById(R.id.ad_body)
                    view.callToActionView = view.findViewById(R.id.ad_cta)
                    view.iconView = view.findViewById(R.id.ad_icon)

                    nativeAd?.let { ad ->
                        (view.headlineView as? TextView)?.text = ad.headline
                        (view.bodyView as? TextView)?.text = ad.body
                        (view.callToActionView as? Button)?.text = ad.callToAction
                        ad.icon?.let { icon ->
                            (view.iconView as? ImageView)?.setImageDrawable(icon.drawable)
                            view.iconView?.visibility = View.VISIBLE
                        } ?: run {
                            view.iconView?.visibility = View.GONE
                        }
                        view.setNativeAd(ad)
                    }
                    view
                },
                update = { view ->
                    nativeAd?.let { ad ->
                        (view.headlineView as? TextView)?.text = ad.headline
                        (view.bodyView as? TextView)?.text = ad.body
                        (view.callToActionView as? Button)?.text = ad.callToAction
                        ad.icon?.let { icon ->
                            (view.iconView as? ImageView)?.setImageDrawable(icon.drawable)
                            view.iconView?.visibility = View.VISIBLE
                        }
                        view.setNativeAd(ad)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/* Design tokens for ad card */
internal object CbAd {
    val Panel = androidx.compose.ui.graphics.Color(0xFFF0F4FF)
}
