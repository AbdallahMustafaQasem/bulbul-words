package com.amq.bulbulwords.data

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

/** User settings, persisted. Call [init] once from the Activity. */
object Settings {
    private lateinit var p: SharedPreferences

    var lang by mutableStateOf(Lang.FUSHA)
    var voiceOn by mutableStateOf(true)
    var sfxOn by mutableStateOf(true)
    var showText by mutableStateOf(true)
    var speechRate by mutableStateOf(0.8f)
    var childName by mutableStateOf("")
    var calm by mutableStateOf(false)
    var dailyGoal by mutableStateOf(10)
    /** Play-session limit in minutes; 0 = no limit. */
    var playMinutes by mutableStateOf(0)
    /** True once the parent tapped "Rate now"; the rating dialog stops appearing. */
    var rated by mutableStateOf(false)

    fun init(c: Context) {
        p = c.getSharedPreferences("settings", Context.MODE_PRIVATE)
        lang = Lang.values().firstOrNull { it.name == p.getString("lang", "") } ?: Lang.FUSHA
        voiceOn = p.getBoolean("voice", true)
        sfxOn = p.getBoolean("sfx", true)
        showText = p.getBoolean("showText", true)
        speechRate = p.getFloat("rate", 0.8f)
        childName = p.getString("name", "") ?: ""
        calm = p.getBoolean("calm", false)
        dailyGoal = p.getInt("goal", 10)
        playMinutes = p.getInt("playMin", 0)
        rated = p.getBoolean("rated", false)
    }

    fun save() = p.edit()
        .putString("lang", lang.name).putBoolean("voice", voiceOn).putBoolean("sfx", sfxOn)
        .putBoolean("showText", showText).putFloat("rate", speechRate).putString("name", childName).putBoolean("calm", calm).putInt("goal", dailyGoal).putInt("playMin", playMinutes).putBoolean("rated", rated)
        .apply()
}

/** Child progress tracking, persisted. */
object Progress {
    private lateinit var p: SharedPreferences
    var version by mutableStateOf(0) // bump to recompose readers

    fun init(c: Context) { p = c.getSharedPreferences("progress", Context.MODE_PRIVATE) }

    fun record(gameId: String, score: Int, total: Int) {
        p.edit()
            .putInt("g_${gameId}_plays", plays(gameId) + 1)
            .putInt("g_${gameId}_correct", correct(gameId) + score)
            .putInt("g_${gameId}_total", total(gameId) + total)
            .putInt("g_${gameId}_best", maxOf(best(gameId), score * 100 / maxOf(total, 1)))
            .apply()
        version++
    }

    fun plays(g: String) = p.getInt("g_${g}_plays", 0)
    fun correct(g: String) = p.getInt("g_${g}_correct", 0)
    fun total(g: String) = p.getInt("g_${g}_total", 0)
    /** Best result as a percentage 0..100. */
    fun best(g: String) = p.getInt("g_${g}_best", 0)

    fun today(): Int {
        val d = java.time.LocalDate.now().toString()
        return if (p.getString("day", "") == d) p.getInt("dayCount", 0) else 0
    }
    private fun bumpToday() {
        val d = java.time.LocalDate.now().toString()
        val n = today() + 1
        p.edit().putString("day", d).putInt("dayCount", n).apply()
        version++
    }

    const val MASTERY = 2
    /** Stickers earned during the current screen, shown once as a celebration. */
    var justUnlocked by mutableStateOf<List<String>>(emptyList())

    fun points(id: String) = p.getInt("pt_$id", 0)
    fun isMastered(id: String) = points(id) >= MASTERY
    fun addPoint(id: String, amount: Int = 1) {
        val old = points(id)
        val n = old + amount
        p.edit().putInt("pt_$id", n).apply()
        if (old < MASTERY && n >= MASTERY) justUnlocked = justUnlocked + id
        version++
    }

    fun markLearned(wordId: String) {
        bumpToday()
        val day = java.time.LocalDate.now().toString()
        if (p.getString("lp_$wordId", "") != day) {
            p.edit().putString("lp_$wordId", day).apply()
            addPoint(wordId)
        }
        val s = learned().toMutableSet()
        if (s.add(wordId)) { p.edit().putStringSet("learned", s).apply(); version++ }
    }
    fun learned(): Set<String> = p.getStringSet("learned", emptySet()) ?: emptySet()
    fun stars(): Int = learned().size / 5 + (p.getInt("totalStars", 0))
    fun addStars(n: Int) { p.edit().putInt("totalStars", p.getInt("totalStars", 0) + n).apply(); version++ }
}

/** Parent-area password, chosen by the parent on first use. Only a salted hash is stored. */
object ParentLock {
    private lateinit var p: SharedPreferences

    fun init(c: Context) { p = c.getSharedPreferences("parent", Context.MODE_PRIVATE) }

    fun hasPassword() = p.contains("hash")

    private fun hash(salt: String, pw: String): String {
        val md = java.security.MessageDigest.getInstance("SHA-256")
        return android.util.Base64.encodeToString(md.digest((salt + pw).toByteArray(Charsets.UTF_8)), android.util.Base64.NO_WRAP)
    }

    fun set(pw: String) {
        val saltBytes = ByteArray(16).also { java.security.SecureRandom().nextBytes(it) }
        val salt = android.util.Base64.encodeToString(saltBytes, android.util.Base64.NO_WRAP)
        p.edit().putString("salt", salt).putString("hash", hash(salt, pw)).apply()
    }

    fun check(pw: String): Boolean {
        val salt = p.getString("salt", null) ?: return false
        return hash(salt, pw) == p.getString("hash", null)
    }
}
