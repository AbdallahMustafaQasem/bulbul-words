package com.amq.bulbulwords

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.amq.bulbulwords.data.Progress
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Speaker
import com.amq.bulbulwords.ui.App

class MainActivity : ComponentActivity() {
    private lateinit var speaker: Speaker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // Phones stay portrait; tablets (smallest width >= 600dp) rotate freely.
        requestedOrientation = if (resources.configuration.smallestScreenWidthDp < 600)
            android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        else android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        Settings.init(applicationContext)
        Progress.init(applicationContext)
        com.amq.bulbulwords.data.ParentLock.init(applicationContext)
        speaker = Speaker(applicationContext)
        com.amq.bulbulwords.data.Ads.init(applicationContext)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) { App(speaker) }
        }
    }

    override fun onStart() {
        super.onStart()
        com.amq.bulbulwords.data.Ads.onAppForegrounded(this)
    }

    override fun onStop() {
        com.amq.bulbulwords.data.Ads.onAppBackgrounded()
        super.onStop()
    }

    override fun onDestroy() {
        speaker.shutdown()
        super.onDestroy()
    }
}
