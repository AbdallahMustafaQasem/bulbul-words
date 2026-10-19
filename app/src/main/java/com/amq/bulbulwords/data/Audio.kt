package com.amq.bulbulwords.data

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.speech.tts.TextToSpeech
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import java.util.Locale
import kotlin.math.PI
import kotlin.math.sin

/** Text-to-speech wrapper that speaks words in the selected language. */
class Speaker(context: Context) {
    private val appContext = context.applicationContext
    private var player: android.media.MediaPlayer? = null
    private var ready = false
    /** False when the device has no Arabic voice installed (UI then offers to install one). */
    var arabicOk by androidx.compose.runtime.mutableStateOf(true)
        private set
    private lateinit var tts: TextToSpeech

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            ready = status == TextToSpeech.SUCCESS
            if (ready) arabicOk = tts.isLanguageAvailable(Locale("ar")) >= TextToSpeech.LANG_AVAILABLE
        }
    }

    private fun localeFor(lang: Lang): Locale = when (lang) {
        Lang.FUSHA -> Locale("ar")
        Lang.EN -> Locale.US
    }

    /** Speak arbitrary [text] with the voice for [lang]. */
    fun sayText(text: String, lang: Lang = Settings.lang, queue: Boolean = false, slow: Boolean = false) {
        if (!ready || !Settings.voiceOn) return
        val loc = localeFor(lang)
        if (tts.setLanguage(loc) < TextToSpeech.LANG_AVAILABLE) tts.setLanguage(Locale("ar"))
        tts.setSpeechRate(if (slow) 0.4f else Settings.speechRate)
        tts.setPitch(1.15f)
        tts.speak(text, if (queue) TextToSpeech.QUEUE_ADD else TextToSpeech.QUEUE_FLUSH, null, "w")
    }

    fun say(w: Word, lang: Lang = Settings.lang, slow: Boolean = false) = sayText(w.text(lang), lang, slow = slow)

    fun praise() = sayText(listOf("أحسنت!", "برافو!", "ممتاز!", "شاطر!", "رائع!").random(), Lang.FUSHA)

    /** Speak the word's onomatopoeia (animal / bird / vehicle sound), falling back to the name. */
    fun saySound(w: Word) {
        // Prefer a real recording (res/raw/snd_<wordId>.mp3|ogg) when one is bundled; else spoken imitation.
        val resId = appContext.resources.getIdentifier("snd_" + w.id, "raw", appContext.packageName)
        if (resId != 0 && Settings.sfxOn) {
            try {
                player?.release()
                player = android.media.MediaPlayer.create(appContext, resId)?.also { mp ->
                    mp.setOnCompletionListener { it.release(); if (player === it) player = null }
                    mp.start()
                }
                if (player != null) return
            } catch (_: Exception) {}
        }
        val s = w.sound ?: w.ar
        sayText(s, if (Settings.lang == Lang.EN) Lang.FUSHA else Settings.lang)
    }

    fun stop() { tts.stop() }
    fun shutdown() { tts.stop(); tts.shutdown() }
}

/** Short synthesized sound effects (generated in code, no audio files needed). */
object Sfx {
    private const val RATE = 22050

    private fun tone(notes: List<Pair<Double, Int>>, vol: Float = 0.5f) {
        if (!Settings.sfxOn) return
        Thread {
            try {
                val samples = ArrayList<Short>()
                for ((f, ms) in notes) {
                    val n = RATE * ms / 1000
                    for (i in 0 until n) {
                        val env = minOf(1f, i / 300f, (n - i) / 600f).coerceAtLeast(0f)
                        val v = (sin(2 * PI * f * i / RATE) * 0.7 + sin(4 * PI * f * i / RATE) * 0.2) * env * vol
                        samples.add((v * Short.MAX_VALUE).toInt().toShort())
                    }
                }
                val arr = samples.toShortArray()
                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(RATE).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build()
                    )
                    .setBufferSizeInBytes(arr.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC).build()
                track.write(arr, 0, arr.size)
                track.play()
                Thread.sleep(notes.sumOf { it.second }.toLong() + 100)
                track.release()
            } catch (_: Exception) {}
        }.start()
    }

    fun tap() = tone(listOf(660.0 to 60), 0.3f)
    fun success() = tone(listOf(523.25 to 110, 659.25 to 110, 783.99 to 200))
    fun wrong() = if (Settings.calm) Unit else tone(listOf(330.0 to 140, 262.0 to 220), 0.4f)
    fun win() = tone(listOf(523.25 to 120, 659.25 to 120, 783.99 to 120, 1046.5 to 350))
    fun pop() = tone(listOf(900.0 to 40, 500.0 to 60), 0.4f)
}
