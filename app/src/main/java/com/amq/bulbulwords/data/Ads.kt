package com.amq.bulbulwords.data

import android.app.Activity
import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback

/**
 * AdMob wrapper for a child-directed app (Google Play Families policy).
 *
 * - Every request is tagged child-directed and under the age of consent, so only non-personalised,
 *   Families-eligible demand is served and no advertising ID is used.
 * - Maximum ad content rating is G.
 * - Ads never appear during a game or on the learning screens; only after a finished game (with a long
 *   cooldown), in the password-protected parent area, or on return to the app after a long break.
 * - Debug builds use Google's sample ad units so the developer never clicks real ads.
 */
object Ads {
    private const val APP_BANNER = "ca-app-pub-2999041549385331/3064154673"
    private const val APP_INTERSTITIAL = "ca-app-pub-2999041549385331/9094201395"
    private const val APP_REWARDED_INTERSTITIAL = "ca-app-pub-2999041549385331/6160916294"
    private const val APP_REWARDED = "ca-app-pub-2999041549385331/2072216213"
    private const val APP_NATIVE = "ca-app-pub-2999041549385331/1215711370"
    private const val APP_OPEN = "ca-app-pub-2999041549385331/7589548036"

    private const val TEST_BANNER = "ca-app-pub-3940256099942544/9214589741"
    private const val TEST_INTERSTITIAL = "ca-app-pub-3940256099942544/1033173712"
    private const val TEST_REWARDED_INTERSTITIAL = "ca-app-pub-3940256099942544/5354046379"
    private const val TEST_REWARDED = "ca-app-pub-3940256099942544/5224354917"
    private const val TEST_NATIVE = "ca-app-pub-3940256099942544/2247696110"
    private const val TEST_OPEN = "ca-app-pub-3940256099942544/9257395921"

    private const val INTERSTITIAL_COOLDOWN_MS = 4 * 60_000L
    private const val OPEN_AD_MIN_BACKGROUND_MS = 30 * 60_000L
    private const val OPEN_AD_COOLDOWN_MS = 10 * 60_000L

    private var debug = false
    private var started = false
    private lateinit var appCtx: Context

    private var interstitial: InterstitialAd? = null
    private var rewarded: RewardedAd? = null
    private var rewardedInterstitial: RewardedInterstitialAd? = null
    private var appOpen: AppOpenAd? = null
    private var appOpenLoadedAt = 0L

    private var lastFullScreenAt = 0L
    private var lastOpenAt = 0L
    private var backgroundedAt = 0L
    private val sessionStart = System.currentTimeMillis()

    /** Extra play seconds granted by a rewarded ad (read by the play-time limiter). */
    var bonusSeconds by mutableIntStateOf(0)
        private set

    /** True when a rewarded ad is ready to be shown. */
    var rewardedReady by mutableStateOf(false)
        private set

    fun addBonus(seconds: Int) { bonusSeconds += seconds }
    fun resetBonus() { bonusSeconds = 0 }

    private fun id(real: String, test: String) = if (debug) test else real

    fun init(context: Context) {
        if (started) return
        started = true
        appCtx = context.applicationContext
        debug = (appCtx.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                .setTagForChildDirectedTreatment(RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE)
                .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE)
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_G)
                .build(),
        )
        MobileAds.initialize(appCtx) {
            loadInterstitial()
            loadRewarded()
            loadRewardedInterstitial()
            loadAppOpen()
        }
    }

    private fun request() = AdRequest.Builder().build()

    // ───────── Banner (parent area only) ─────────

    fun createBanner(context: Context, widthDp: Int): AdView =
        AdView(context).apply {
            setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, widthDp))
            adUnitId = id(APP_BANNER, TEST_BANNER)
            loadAd(request())
        }

    // ───────── Interstitial (after a finished game) ─────────

    private fun loadInterstitial() {
        InterstitialAd.load(appCtx, id(APP_INTERSTITIAL, TEST_INTERSTITIAL), request(), object : InterstitialAdLoadCallback() {
            override fun onAdLoaded(ad: InterstitialAd) { interstitial = ad }
            override fun onAdFailedToLoad(e: LoadAdError) { interstitial = null }
        })
    }

    private fun cooledDown() = System.currentTimeMillis() - lastFullScreenAt >= INTERSTITIAL_COOLDOWN_MS &&
        System.currentTimeMillis() - sessionStart >= 2 * 60_000L

    /** Shows an interstitial at a natural break (after a game) if one is ready and the cooldown has passed. */
    fun maybeShowInterstitial(activity: Activity) {
        val ad = interstitial ?: return
        if (!cooledDown()) return
        interstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { lastFullScreenAt = System.currentTimeMillis(); loadInterstitial() }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { loadInterstitial() }
        }
        ad.show(activity)
    }

    // ───────── Rewarded (parent chooses to add play time) ─────────

    private fun loadRewarded() {
        RewardedAd.load(appCtx, id(APP_REWARDED, TEST_REWARDED), request(), object : RewardedAdLoadCallback() {
            override fun onAdLoaded(ad: RewardedAd) { rewarded = ad; rewardedReady = true }
            override fun onAdFailedToLoad(e: LoadAdError) { rewarded = null; rewardedReady = false }
        })
    }

    fun showRewarded(activity: Activity, onReward: () -> Unit) {
        val ad = rewarded ?: return
        rewarded = null; rewardedReady = false
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { lastFullScreenAt = System.currentTimeMillis(); loadRewarded() }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { loadRewarded() }
        }
        ad.show(activity) { onReward() }
    }

    // ───────── Rewarded interstitial (parent unlocks after time is up) ─────────

    private fun loadRewardedInterstitial() {
        RewardedInterstitialAd.load(
            appCtx, id(APP_REWARDED_INTERSTITIAL, TEST_REWARDED_INTERSTITIAL), request(),
            object : RewardedInterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedInterstitialAd) { rewardedInterstitial = ad }
                override fun onAdFailedToLoad(e: LoadAdError) { rewardedInterstitial = null }
            },
        )
    }

    /** Shown when a parent unlocks the app after the time limit. [done] runs whether or not an ad was shown. */
    fun showRewardedInterstitial(activity: Activity, done: () -> Unit) {
        val ad = rewardedInterstitial
        if (ad == null) { done(); return }
        rewardedInterstitial = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { lastFullScreenAt = System.currentTimeMillis(); loadRewardedInterstitial(); done() }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { loadRewardedInterstitial(); done() }
        }
        ad.show(activity) { }
    }

    // ───────── App open (after a long break only) ─────────

    private fun loadAppOpen() {
        AppOpenAd.load(appCtx, id(APP_OPEN, TEST_OPEN), request(), object : AppOpenAd.AppOpenAdLoadCallback() {
            override fun onAdLoaded(ad: AppOpenAd) { appOpen = ad; appOpenLoadedAt = System.currentTimeMillis() }
            override fun onAdFailedToLoad(e: LoadAdError) { appOpen = null }
        })
    }

    fun onAppBackgrounded() { backgroundedAt = System.currentTimeMillis() }

    /** Called from onStart: shows an app-open ad only when the app returns after a long break. */
    fun onAppForegrounded(activity: Activity) {
        val now = System.currentTimeMillis()
        if (backgroundedAt == 0L || now - backgroundedAt < OPEN_AD_MIN_BACKGROUND_MS) return
        if (now - lastOpenAt < OPEN_AD_COOLDOWN_MS) return
        val ad = appOpen ?: run { loadAppOpen(); return }
        if (now - appOpenLoadedAt > 4 * 3_600_000L) { appOpen = null; loadAppOpen(); return }
        appOpen = null
        lastOpenAt = now
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() { lastFullScreenAt = System.currentTimeMillis(); loadAppOpen() }
            override fun onAdFailedToShowFullScreenContent(e: AdError) { loadAppOpen() }
        }
        ad.show(activity)
    }

    // ───────── Native (parent area) ─────────

    fun loadNative(context: Context, onLoaded: (NativeAd) -> Unit, onFailed: () -> Unit = {}) {
        AdLoader.Builder(context, id(APP_NATIVE, TEST_NATIVE))
            .forNativeAd { onLoaded(it) }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(e: LoadAdError) { onFailed() }
            })
            .build()
            .loadAd(request())
    }
}
