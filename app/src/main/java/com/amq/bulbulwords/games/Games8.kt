package com.amq.bulbulwords.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.amq.bulbulwords.data.Category
import com.amq.bulbulwords.data.Lang
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Word
import com.amq.bulbulwords.data.WordRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ───────────────────────── 1. WhereGame ─────────────────────────

private enum class G8Rel(val ar: String, val en: String) {
    ON("فوق", "on"),
    UNDER("تحت", "under"),
    IN("داخل", "in"),
    NEXT("بجانب", "next to"),
    FRONT("أمام", "in front of"),
    BEHIND("خلف", "behind"),
}

private class G8Thing(val emoji: String, val ar: String, val en: String)

private val G8Containers = listOf(
    G8Thing("📦", "الصندوق", "box"),
    G8Thing("🪑", "الكرسي", "chair"),
    G8Thing("🛋️", "الأريكة", "sofa"),
    G8Thing("🧺", "السلة", "basket"),
    G8Thing("🏠", "البيت", "house"),
)

private val G8Objects = listOf(
    G8Thing("🧸", "الدب", "bear"),
    G8Thing("⚽", "الكرة", "ball"),
    G8Thing("🐱", "القطة", "cat"),
    G8Thing("🐶", "الكلب", "dog"),
    G8Thing("🍎", "التفاحة", "apple"),
)

private class G8Round(
    val container: G8Thing,
    val obj: G8Thing,
    val rels: List<G8Rel>,
    val correct: Int,
)

private fun g8MakeRound(index: Int): G8Round {
    val pool = if (index < 4) listOf(G8Rel.ON, G8Rel.UNDER, G8Rel.IN, G8Rel.NEXT) else G8Rel.values().toList()
    val rels = pool.shuffled().take(3)
    val containers = if (G8Rel.IN in rels) G8Containers.filter { it.emoji == "📦" || it.emoji == "🧺" } else G8Containers
    return G8Round(containers.random(), G8Objects.random(), rels, (0..2).random())
}

private fun g8Sentence(r: G8Round, rel: G8Rel): String =
    if (Settings.lang == Lang.EN) "Where is the ${r.obj.en}? The ${r.obj.en} is ${rel.en} the ${r.container.en}"
    else "أين ${r.obj.ar}؟ ${r.obj.ar} ${rel.ar} ${r.container.ar}"

@Composable
private fun G8Scene(
    rel: G8Rel,
    container: G8Thing,
    obj: G8Thing,
    wrongCount: Int,
    celebrate: Boolean,
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
        if (celebrate) 1.12f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "g8sc",
    )
    val a by animateFloatAsState(if (dimmed) 0.45f else 1f, tween(200), label = "g8a")
    Box(
        Modifier
            .fillMaxWidth(0.8f)
            .height(124.dp)
            .scale(sc)
            .graphicsLayer { translationX = shake.value; alpha = a }
            .clip(RoundedCornerShape(28.dp))
            .background(if (celebrate) Color(0xFFB8F2C0) else Color.White)
            .clickable { Sfx.tap(); onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(112.dp), contentAlignment = Alignment.Center) {
            when (rel) {
                G8Rel.ON -> {
                    Pic(container.emoji, 62.dp, Modifier.offset(0.dp, 10.dp))
                    Pic(obj.emoji, 32.dp, Modifier.offset(0.dp, (-34).dp))
                }
                G8Rel.UNDER -> {
                    Pic(container.emoji, 62.dp, Modifier.offset(0.dp, (-14).dp))
                    Pic(obj.emoji, 32.dp, Modifier.offset(0.dp, 40.dp))
                }
                G8Rel.IN -> {
                    Pic(container.emoji, 66.dp)
                    Pic(obj.emoji, 28.dp, Modifier.offset(0.dp, 0.dp))
                }
                G8Rel.NEXT -> {
                    Pic(container.emoji, 62.dp, Modifier.offset((-24).dp, 0.dp))
                    Pic(obj.emoji, 32.dp, Modifier.offset(38.dp, 14.dp))
                }
                G8Rel.FRONT -> {
                    Pic(container.emoji, 62.dp, Modifier.offset((-6).dp, (-8).dp))
                    Pic(obj.emoji, 36.dp, Modifier.offset(14.dp, 28.dp))
                }
                G8Rel.BEHIND -> {
                    Pic(obj.emoji, 34.dp, Modifier.offset(24.dp, (-16).dp))
                    Pic(container.emoji, 66.dp, Modifier.offset((-8).dp, 8.dp))
                }
            }
        }
    }
}

@Composable
private fun G8SpeakButton(accent: Color, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (pressed) 0.9f else 1f, tween(120), label = "g8spk")
    LaunchedEffect(pressed) { if (pressed) { delay(150); pressed = false } }
    Box(
        Modifier.size(80.dp).scale(s).clip(CircleShape).background(accent)
            .clickable { pressed = true; onClick() },
        contentAlignment = Alignment.Center,
    ) { Text("🔊", fontSize = 38.sp) }
}

@Composable
fun WhereGame(host: GameHost) {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    val rd = remember(round) { g8MakeRound(round) }
    val wrongCounts = remember(round) { mutableStateListOf(0, 0, 0) }
    var solved by remember(round) { mutableStateOf(false) }
    var firstTry by remember(round) { mutableStateOf(true) }
    var wrongReported by remember(round) { mutableStateOf(false) }
    var speakTick by remember(round) { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val sentence = g8Sentence(rd, rd.rels[rd.correct])

    LaunchedEffect(round, speakTick) {
        delay(350)
        host.speaker.sayText(sentence, Settings.lang)
    }

    GameFrame(host, "أين؟", round, score) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            G8SpeakButton(host.accent) { speakTick++ }
            Spacer(Modifier.height(6.dp))
            Text(
                sentence, fontSize = 22.sp, fontWeight = FontWeight.Black,
                color = Ink, textAlign = TextAlign.Center,
            )
            Spacer(Modifier.height(12.dp))
            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in 0..2) {
                    G8Scene(
                        rel = rd.rels[i], container = rd.container, obj = rd.obj,
                        wrongCount = wrongCounts[i],
                        celebrate = solved && i == rd.correct,
                        dimmed = solved && i != rd.correct,
                    ) {
                        if (!solved) {
                            if (i == rd.correct) {
                                solved = true
                                if (firstTry) score++
                                host.correct()
                                host.speaker.sayText(sentence, Settings.lang)
                                scope.launch {
                                    delay(2200)
                                    if (round + 1 >= GameHost.ROUNDS) host.finish(score, GameHost.ROUNDS)
                                    else round++
                                }
                            } else {
                                firstTry = false
                                if (!wrongReported) { wrongReported = true; host.wrong() }
                                wrongCounts[i] = wrongCounts[i] + 1
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

// ───────────────────────── 2. SentenceGame ─────────────────────────

private val G8CoreAr = listOf("أريد", "أحب", "أين", "أنا")
private val G8CoreEn = listOf("I want", "I like", "Where is", "I am")
private val G8CoreColors = listOf(Color(0xFFFF9F1C), Color(0xFFF15BB5), Color(0xFF4361EE), Color(0xFF2EC4B6))
private val G8CardColors = listOf(
    Color(0xFFFFD166), Color(0xFF9AE6B4), Color(0xFFA5D8FF), Color(0xFFFFADAD),
    Color(0xFFCDB4FF), Color(0xFFFFC6FF),
)

private fun g8Core(i: Int) = if (Settings.lang == Lang.EN) G8CoreEn[i] else G8CoreAr[i]
private fun g8WordText(w: Word) = w.text(Settings.lang)
private fun g8Compose(core: Int?, w: Word?): String =
    listOfNotNull(core?.let { g8Core(it) }, w?.let { if (Settings.lang != Lang.EN && (core == 1 || core == 2)) arDef(g8WordText(it)) else g8WordText(it) }).joinToString(" ")

private fun g8PlainPool(): List<Word> =
    (WordRepo.of(Category.FOOD) + WordRepo.of(Category.FRUITS) + WordRepo.of(Category.ANIMALS).filter { it.id in setOf("an_cat", "an_dog", "an_cow", "an_horse", "an_sheep", "an_elephant", "an_monkey", "an_bear", "an_panda", "an_rabbit", "an_giraffe", "an_duck", "an_pig") })
        .filter { !it.emoji.startsWith("draw:") && !it.emoji.startsWith("txt:") }
        .distinctBy { it.id }

@Composable
fun SentenceGame(host: GameHost) {
    val rounds = 5
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    val cards = remember(round) {
        val p = g8PlainPool().shuffled()
        p.take(6)
    }
    val target = remember(round) { cards.random() }
    val targetCore = remember(round) { listOf(0, 1, 2).random() }
    var chosenCore by remember(round) { mutableStateOf<Int?>(null) }
    var chosenWord by remember(round) { mutableStateOf<Word?>(null) }
    var solved by remember(round) { mutableStateOf(false) }
    var firstTry by remember(round) { mutableStateOf(true) }
    var hint by remember(round) { mutableStateOf(false) }
    var promptTick by remember(round) { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    val promptSentence = g8Compose(targetCore, target)
    val promptSpoken = if (Settings.lang == Lang.EN) "Say: $promptSentence" else "قل: $promptSentence"

    LaunchedEffect(round, promptTick) {
        delay(400)
        host.speaker.sayText(promptSpoken, Settings.lang)
    }

    val pulse = rememberInfiniteTransition(label = "g8pulse")
    val pulseScale by pulse.animateFloat(
        1f, 1.15f,
        infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse), label = "g8ps",
    )
    val celebrate by animateFloatAsState(
        if (solved) 1f else 0f, spring(Spring.DampingRatioMediumBouncy), label = "g8cel",
    )

    GameFrame(host, "جملتي", round, score) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // Prompt
            Row(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xCCFFFFFF))
                    .clickable { promptTick++ }.padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Pic(target.emoji, 52.dp)
                Spacer(Modifier.size(10.dp))
                Text(
                    promptSpoken, fontSize = 22.sp, fontWeight = FontWeight.Black,
                    color = Ink, textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(10.dp))

            // Sentence strip
            Row(
                Modifier.fillMaxWidth().height(84.dp).clip(RoundedCornerShape(24.dp))
                    .background(if (solved) Color(0xFFB8F2C0) else Color.White).padding(8.dp)
                    .scale(1f + 0.08f * celebrate),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                val c = chosenCore
                val w = chosenWord
                if (c == null && w == null) {
                    Text("···", fontSize = 32.sp, color = Color(0x553B2F63), fontWeight = FontWeight.Black)
                }
                if (c != null) {
                    Box(
                        Modifier.clip(RoundedCornerShape(18.dp)).background(G8CoreColors[c])
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        contentAlignment = Alignment.Center,
                    ) { Text(g8Core(c), fontSize = 22.sp, fontWeight = FontWeight.Black, color = Color.White) }
                }
                if (c != null && w != null) Spacer(Modifier.size(10.dp))
                if (w != null) {
                    Box(
                        Modifier.clip(RoundedCornerShape(18.dp)).background(Color(0xFFFFF4D6)).padding(6.dp),
                        contentAlignment = Alignment.Center,
                    ) { Pic(w.emoji, 56.dp) }
                }
            }
            Spacer(Modifier.height(10.dp))

            // Core words
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                for (i in 0..3) {
                    val sel = chosenCore == i
                    val pulsing = hint && !solved && !sel && i == targetCore
                    Box(
                        Modifier.weight(1f).height(68.dp)
                            .scale(if (pulsing) pulseScale else if (sel) 1.06f else 1f)
                            .clip(RoundedCornerShape(22.dp))
                            .background(G8CoreColors[i].copy(alpha = if (sel) 1f else 0.85f))
                            .clickable {
                                if (!solved) { Sfx.tap(); chosenCore = i; host.speaker.sayText(g8Core(i), Settings.lang) }
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            g8Core(i), fontSize = 19.sp, fontWeight = FontWeight.Black,
                            color = Color.White, textAlign = TextAlign.Center,
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))

            // Picture grid
            for (row in 0..1) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    for (col in 0..2) {
                        val idx = row * 3 + col
                        val w = cards[idx]
                        val sel = chosenWord?.id == w.id
                        val pulsing = hint && !solved && !sel && w.id == target.id
                        PictureCard(
                            emoji = w.emoji,
                            label = g8WordText(w),
                            color = if (sel) Color(0xFFB8F2C0) else G8CardColors[idx],
                            size = 100.dp,
                            modifier = Modifier.scale(if (pulsing) pulseScale else if (sel) 1.06f else 1f),
                        ) {
                            if (!solved) { chosenWord = w; host.speaker.sayText(g8WordText(w), Settings.lang) }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            // Clear + speak
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(72.dp).clip(CircleShape).background(Color(0xFFFFADAD))
                        .clickable {
                            if (!solved) { Sfx.tap(); chosenCore = null; chosenWord = null }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("🧽", fontSize = 34.sp) }
                Box(
                    Modifier.size(96.dp).clip(CircleShape).background(host.accent)
                        .clickable {
                            val c = chosenCore
                            val w = chosenWord
                            if (!solved && (c != null || w != null)) {
                                host.speaker.sayText(g8Compose(c, w), Settings.lang)
                                if (c == targetCore && w?.id == target.id) {
                                    solved = true
                                    if (firstTry) score++
                                    host.correct(target)
                                    scope.launch {
                                        delay(2000)
                                        if (round + 1 >= rounds) host.finish(score, rounds) else round++
                                    }
                                } else {
                                    firstTry = false
                                    hint = true
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) { Text("🔊", fontSize = 48.sp) }
                Box(Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                    if (solved) Text("🎉", fontSize = 48.sp, modifier = Modifier.scale(celebrate))
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}
