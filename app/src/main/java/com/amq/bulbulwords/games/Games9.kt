package com.amq.bulbulwords.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amq.bulbulwords.data.Lang
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Word
import com.amq.bulbulwords.data.WORDS_FEELINGS
import kotlinx.coroutines.delay

private class G9Situation(val emoji: String, val ar: String, val en: String, val feelingId: String)

private val G9_SITUATIONS = listOf(
    G9Situation("🎂", "اليوم عيد ميلاده", "It is his birthday", "fe_happy"),
    G9Situation("🧸", "انكسرت لعبته", "His toy broke", "fe_sad"),
    G9Situation("⛈️", "رعد قوي", "Loud thunder", "fe_scared"),
    G9Situation("🌙", "حان وقت النوم", "It is bedtime", "fe_sleepy"),
    G9Situation("🍽️", "لم يأكل منذ الصباح", "He has not eaten since morning", "fe_hungry"),
    G9Situation("☀️", "الجو حار جداً", "The weather is very hot", "fe_hot"),
    G9Situation("❄️", "الجو بارد جداً", "The weather is very cold", "fe_cold"),
    G9Situation("🎁", "فتح هدية", "He opened a present", "fe_surprised"),
    G9Situation("😤", "أخذوا لعبته", "They took his toy", "fe_angry"),
    G9Situation("🤧", "عنده حرارة وسعال", "He has a fever and a cough", "fe_sick"),
)

private val G9_COLORS = listOf(Color(0xFFFFD166), Color(0xFF9AE6B4), Color(0xFFA5D8FF))

private fun g9Name(w: Word): String = w.text(Settings.lang)

private fun g9Sentence(w: Word): String =
    if (Settings.lang == Lang.EN) "He is ${w.en.lowercase()}" else "هو ${w.ar}"

private fun g9Question(): String =
    if (Settings.lang == Lang.EN) "How does he feel?" else "كيف يشعر؟"

private fun g9Feeling(id: String): Word? = WORDS_FEELINGS.firstOrNull { it.id == id }

@Composable
private fun G9Card(
    w: Word,
    color: Color,
    wrongCount: Int,
    highlight: Boolean,
    dimmed: Boolean,
    onClick: () -> Unit,
) {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(wrongCount) {
        if (wrongCount > 0) {
            repeat(3) {
                shake.animateTo(14f, tween(50))
                shake.animateTo(-14f, tween(50))
            }
            shake.animateTo(0f, tween(50))
        }
    }
    val sc by animateFloatAsState(
        if (highlight) 1.15f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "g9sc",
    )
    val a by animateFloatAsState(if (dimmed) 0.45f else 1f, tween(200), label = "g9a")
    Column(
        Modifier
            .size(width = 104.dp, height = 150.dp)
            .scale(sc)
            .graphicsLayer { translationX = shake.value; alpha = a }
            .clip(RoundedCornerShape(28.dp))
            .background(if (highlight) Color(0xFFB8F2C0) else color)
            .clickable { Sfx.tap(); onClick() }
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Pic(w.emoji, 64.dp)
        if (Settings.showText) {
            Spacer(Modifier.height(4.dp))
            Text(
                g9Name(w), fontSize = 18.sp, fontWeight = FontWeight.Black,
                color = Ink, textAlign = TextAlign.Center, maxLines = 2,
            )
        }
    }
}

@Composable
fun FeelingsGame(host: GameHost) {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    // Pre-pick distinct content for the whole game.
    val faceTargets = remember { WORDS_FEELINGS.shuffled().take(4) }
    val situations = remember {
        G9_SITUATIONS.filter { g9Feeling(it.feelingId) != null }.shuffled().take(4)
    }

    val isFace = round < 4
    val target: Word = remember(round) {
        if (round < 4) faceTargets[round]
        else g9Feeling(situations[(round - 4).coerceIn(0, situations.size - 1)].feelingId)!!
    }
    val situation: G9Situation? = if (isFace) null else situations[(round - 4).coerceIn(0, situations.size - 1)]
    val options = remember(round) {
        (WORDS_FEELINGS.filter { it.id != target.id }.shuffled().take(2) + target).shuffled()
    }
    val wrongCounts = remember(round) { mutableStateListOf(0, 0, 0) }
    var solved by remember(round) { mutableStateOf(false) }
    var firstTry by remember(round) { mutableStateOf(true) }
    var wrongSent by remember(round) { mutableStateOf(false) }

    val bounce = remember(round) { Animatable(0.6f) }
    LaunchedEffect(round) {
        bounce.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }
    val pulse by animateFloatAsState(
        if (solved) 1.2f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "g9pulse",
    )

    LaunchedEffect(round) {
        delay(350)
        if (situation != null) {
            host.speaker.sayText(if (Settings.lang == Lang.EN) situation.en else situation.ar)
            delay(1500)
        }
        host.speaker.sayText(g9Question())
    }

    GameFrame(host, "كيف يشعر؟", round, score) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val big = if (isFace) target.emoji else situation!!.emoji
            val bigShown = if (solved) target.emoji else big
            Box(
                Modifier
                    .scale(bounce.value * pulse)
                    .size(190.dp)
                    .clip(RoundedCornerShape(48.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) { Pic(bigShown, 130.dp) }
            Spacer(Modifier.height(10.dp))
            if (situation != null && Settings.showText) {
                Text(
                    if (Settings.lang == Lang.EN) situation.en else situation.ar,
                    fontSize = 24.sp, fontWeight = FontWeight.Black, color = Ink,
                    textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 8.dp),
                )
                Spacer(Modifier.height(4.dp))
            }
            Text(
                if (solved) g9Sentence(target) else g9Question(),
                fontSize = 26.sp, fontWeight = FontWeight.Black,
                color = if (solved) Color(0xFF2E9E50) else Ink, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(18.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                options.forEachIndexed { i, w ->
                    G9Card(
                        w = w,
                        color = G9_COLORS[i % G9_COLORS.size],
                        wrongCount = wrongCounts[i],
                        highlight = solved && w.id == target.id,
                        dimmed = solved && w.id != target.id,
                    ) {
                        if (solved || finished) return@G9Card
                        if (w.id == target.id) {
                            solved = true
                            if (firstTry) { host.correct(target); score++ } else host.correct()
                        } else {
                            firstTry = false
                            wrongCounts[i] = wrongCounts[i] + 1
                            if (!wrongSent) { wrongSent = true; host.wrong() }
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(solved) {
        if (solved) {
            host.speaker.sayText(g9Sentence(target))
            delay(1800)
            if (round + 1 >= GameHost.ROUNDS) {
                if (!finished) { finished = true; host.finish(score, GameHost.ROUNDS) }
            } else {
                round++
            }
        }
    }
}
