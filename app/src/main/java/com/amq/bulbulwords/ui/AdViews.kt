package com.amq.bulbulwords.ui

import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.amq.bulbulwords.data.Ads
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView

/** Adaptive banner. Only placed inside the password-protected parent area. */
@Composable
fun BannerAd(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    val width = LocalConfiguration.current.screenWidthDp
    val view = remember(width) { Ads.createBanner(ctx, width) }
    DisposableEffect(view) { onDispose { view.destroy() } }
    AndroidView(factory = { view }, modifier = modifier.fillMaxWidth())
}

/** Native ad built from plain views (headline, body, icon, media, call to action). Parent area only. */
@Composable
fun NativeAdCard(modifier: Modifier = Modifier) {
    val ctx = LocalContext.current
    var ad by remember { mutableStateOf<NativeAd?>(null) }
    DisposableEffect(Unit) {
        Ads.loadNative(ctx, onLoaded = { ad?.destroy(); ad = it })
        onDispose { ad?.destroy(); ad = null }
    }
    val loaded = ad ?: return
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { c ->
            val dp = c.resources.displayMetrics.density
            val pad = (12 * dp).toInt()
            val headline = TextView(c).apply { textSize = 16f; setTypeface(typeface, android.graphics.Typeface.BOLD); setTextColor(0xFF1B1B3A.toInt()) }
            val body = TextView(c).apply { textSize = 13f; setTextColor(0xFF444466.toInt()) }
            val icon = ImageView(c).apply { layoutParams = LinearLayout.LayoutParams((40 * dp).toInt(), (40 * dp).toInt()) }
            val media = MediaView(c).apply { layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (150 * dp).toInt()) }
            val cta = Button(c)
            val badge = TextView(c).apply { text = "Ad"; textSize = 11f; setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xFFFFB703.toInt()); setPadding(8, 2, 8, 2) }
            val top = LinearLayout(c).apply {
                orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
                addView(icon); addView(headline, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f).apply { marginStart = pad / 2; marginEnd = pad / 2 }); addView(badge)
            }
            val column = LinearLayout(c).apply {
                orientation = LinearLayout.VERTICAL; setPadding(pad, pad, pad, pad); setBackgroundColor(0xFFFFFFFF.toInt())
                addView(top); addView(body); addView(media); addView(cta)
            }
            NativeAdView(c).apply {
                addView(column)
                headlineView = headline; bodyView = body; iconView = icon; mediaView = media; callToActionView = cta
                tag = arrayOf(headline, body, icon, media, cta)
            }
        },
        update = { v ->
            @Suppress("UNCHECKED_CAST")
            val t = v.tag as Array<Any>
            (t[0] as TextView).text = loaded.headline
            (t[1] as TextView).text = loaded.body
            val ic = t[2] as ImageView
            loaded.icon?.let { ic.setImageDrawable(it.drawable); ic.visibility = android.view.View.VISIBLE } ?: run { ic.visibility = android.view.View.GONE }
            (t[3] as MediaView).mediaContent = loaded.mediaContent
            (t[4] as Button).text = loaded.callToAction ?: "Open"
            v.setNativeAd(loaded)
        },
    )
}

/** The hosting Activity of a Compose context (needed to show full-screen ads). */
fun android.content.Context.findActivity(): android.app.Activity? {
    var c: android.content.Context? = this
    while (c is android.content.ContextWrapper) {
        if (c is android.app.Activity) return c
        c = c.baseContext
    }
    return null
}
