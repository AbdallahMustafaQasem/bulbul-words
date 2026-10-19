package com.amq.bulbulwords.games

import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amq.bulbulwords.data.Category
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Word
import com.amq.bulbulwords.data.WordRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private fun g4Words(host: GameHost): List<Word> {
    val base = host.pick(GameHost.ROUNDS)
    if (base.isEmpty()) return base
    return List(GameHost.ROUNDS) { base[it % base.size] }
}

@Composable
private fun g4Bounce(): Float {
    val t = rememberInfiniteTransition(label = "g4b")
    val s by t.animateFloat(
        1f, 1.08f,
        infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "g4bs",
    )
    return s
}

@Composable
private fun G4BigButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .height(76.dp)
            .clip(RoundedCornerShape(38.dp))
            .background(color)
            .clickable { Sfx.tap(); onClick() }
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) { Text(text, fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color.White) }
}

// ───────────────────────────── 1. SpeakGame ─────────────────────────────

@Composable
fun SpeakGame(host: GameHost) {
    val words = remember { g4Words(host) }
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var celebrating by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pulse = g4Bounce()
    val word = words[round.coerceAtMost(words.lastIndex)]

    LaunchedEffect(round) {
        delay(350)
        host.say(word)
    }

    GameFrame(host, "قل الكلمة", round, score) {
        Column(
            Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(8.dp))
            Box(contentAlignment = Alignment.Center) {
                EmojiBubble(
                    word.emoji, size = 220.dp, color = word.cat.color,
                    modifier = Modifier.scale(if (celebrating) 1.15f else pulse),
                    onClick = { host.say(word) },
                )
                if (celebrating) {
                    Text("⭐", fontSize = 72.sp, modifier = Modifier.align(Alignment.TopEnd))
                    Text("✨", fontSize = 56.sp, modifier = Modifier.align(Alignment.TopStart))
                    Text("🌟", fontSize = 56.sp, modifier = Modifier.align(Alignment.BottomEnd))
                }
            }
            Spacer(Modifier.height(16.dp))
            if (Settings.showText) {
                Text(
                    word.text(Settings.lang), fontSize = 44.sp, fontWeight = FontWeight.Black,
                    color = Ink, textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(24.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    Modifier.size(76.dp).clip(CircleShape).background(Color(0xFF4361EE))
                        .clickable { Sfx.tap(); host.say(word) },
                    contentAlignment = Alignment.Center,
                ) { Text("🔁", fontSize = 34.sp) }
                Box(
                    Modifier.weight(1f).height(84.dp).clip(RoundedCornerShape(42.dp))
                        .background(Color(0xFF2EB872))
                        .clickable(enabled = !celebrating) {
                            celebrating = true
                            score++
                            host.correct(word)
                            scope.launch {
                                delay(1100)
                                celebrating = false
                                if (round + 1 >= GameHost.ROUNDS) {
                                    host.finish(score, GameHost.ROUNDS)
                                } else {
                                    round++
                                }
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text("✔ قلتها!", fontSize = 30.sp, fontWeight = FontWeight.Black, color = Color.White)
                }
            }
        }
    }
}

// ───────────────────────────── 2. SortGame ─────────────────────────────

@Composable
fun SortGame(host: GameHost) {
    val setup = remember {
        val groups = WordRepo.all.groupBy { it.cat }.filter { it.value.size >= 4 }
        val cats: List<Category> = groups.keys.shuffled().take(2)
        val items = cats.flatMap { c -> groups.getValue(c).shuffled().take(4) }.shuffled()
        cats to items
    }
    val cats = setup.first
    val items = setup.second
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var missed by remember { mutableStateOf(false) }
    var locked by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shake = remember { Animatable(0f) }
    val pop = remember { Animatable(1f) }
    val pulse = g4Bounce()
    val total = items.size
    val word = items[round.coerceAtMost(items.lastIndex)]

    GameFrame(host, "رتّب", round, score) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("أين تذهب؟", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(8.dp))
            EmojiBubble(
                word.emoji, size = 190.dp, color = Color(0xFFFFD166),
                modifier = Modifier
                    .graphicsLayer { translationX = shake.value; scaleX = pop.value * pulse; scaleY = pop.value * pulse },
            )
            Spacer(Modifier.height(10.dp))
            if (Settings.showText) {
                Text(word.text(Settings.lang), fontSize = 30.sp, fontWeight = FontWeight.Black, color = Ink)
            }
            Spacer(Modifier.height(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                cats.forEach { cat ->
                    Column(
                        Modifier
                            .weight(1f)
                            .height(190.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .background(cat.color)
                            .clickable(enabled = !locked) {
                                Sfx.tap()
                                if (word.cat == cat) {
                                    locked = true
                                    if (!missed) score++
                                    if (!missed) host.correct(word) else host.correct()
                                    host.say(word)
                                    scope.launch {
                                        pop.animateTo(1.4f, tween(150))
                                        pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy))
                                        delay(900)
                                        missed = false
                                        if (round + 1 >= total) {
                                            host.finish(score, total)
                                        } else {
                                            round++
                                            locked = false
                                        }
                                    }
                                } else {
                                    missed = true
                                    host.wrong()
                                    scope.launch {
                                        repeat(3) {
                                            shake.animateTo(24f, tween(50))
                                            shake.animateTo(-24f, tween(50))
                                        }
                                        shake.animateTo(0f, tween(50))
                                    }
                                }
                            }
                            .padding(10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(cat.emoji, fontSize = 72.sp)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            cat.ar, fontSize = 22.sp, fontWeight = FontWeight.Black,
                            color = Color.White, textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

// ───────────────────────────── 3. ShadowGame ─────────────────────────────

@Composable
private fun G4Silhouette(emoji: String, size: androidx.compose.ui.unit.Dp, modifier: Modifier = Modifier) {
    Canvas(modifier.size(size)) {
        drawIntoCanvas { c ->
            val p = Paint().apply {
                isAntiAlias = true
                textSize = this@Canvas.size.minDimension * 0.78f
                textAlign = Paint.Align.CENTER
                colorFilter = PorterDuffColorFilter(android.graphics.Color.BLACK, PorterDuff.Mode.SRC_IN)
            }
            val fm = p.fontMetrics
            val y = this@Canvas.size.height / 2f - (fm.ascent + fm.descent) / 2f
            c.nativeCanvas.drawText(emoji, this@Canvas.size.width / 2f, y, p)
        }
    }
}

@Composable
fun ShadowGame(host: GameHost) {
    val rounds = remember {
        fun plain(l: List<Word>) = l.filter { !it.emoji.startsWith("draw:") && !it.emoji.startsWith("txt:") }
        val pp = plain(host.pool).let { if (it.size >= 3) it else plain(WordRepo.all) }
        val base = pp.shuffled().take(GameHost.ROUNDS)
        val words = List(GameHost.ROUNDS) { base[it % base.size] }
        words.map { w ->
            val others = pp.filter { it.emoji != w.emoji }.shuffled().take(2)
            w to (others + w).shuffled()
        }
    }
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var missed by remember { mutableStateOf(false) }
    var revealed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val shake = remember { Animatable(0f) }
    val reveal = remember { Animatable(0.3f) }
    val pulse = g4Bounce()
    val (word, choices) = rounds[round.coerceAtMost(rounds.lastIndex)]

    GameFrame(host, "الظل", round, score) {
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("من هذا الظل؟", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink)
            Spacer(Modifier.height(12.dp))
            Box(
                Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFFFF1B8)),
                contentAlignment = Alignment.Center,
            ) {
                if (!revealed) {
                    G4Silhouette(
                        word.emoji, 200.dp,
                        Modifier.graphicsLayer { translationX = shake.value; scaleX = pulse; scaleY = pulse },
                    )
                } else {
                    Pic(
                        word.emoji, 158.dp,
                        modifier = Modifier.graphicsLayer { scaleX = reveal.value; scaleY = reveal.value },
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            if (revealed && Settings.showText) {
                Text(word.text(Settings.lang), fontSize = 32.sp, fontWeight = FontWeight.Black, color = Ink)
            } else {
                Spacer(Modifier.height(40.dp))
            }
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                choices.forEach { c ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        PictureCard(c.emoji, null, Color.White, 104.dp) {
                            if (revealed) return@PictureCard
                            if (c.emoji == word.emoji) {
                                revealed = true
                                if (!missed) score++
                                if (!missed) host.correct(word) else host.correct()
                                host.say(word)
                                scope.launch {
                                    reveal.snapTo(0.3f)
                                    reveal.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessLow))
                                    delay(1200)
                                    missed = false
                                    if (round + 1 >= GameHost.ROUNDS) {
                                        host.finish(score, GameHost.ROUNDS)
                                    } else {
                                        revealed = false
                                        round++
                                    }
                                }
                            } else {
                                missed = true
                                host.wrong()
                                scope.launch {
                                    repeat(3) {
                                        shake.animateTo(20f, tween(50))
                                        shake.animateTo(-20f, tween(50))
                                    }
                                    shake.animateTo(0f, tween(50))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
