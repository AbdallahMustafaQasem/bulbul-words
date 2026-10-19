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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
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
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Word
import com.amq.bulbulwords.data.WordRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CardColors = listOf(
    Color(0xFFFFD166), Color(0xFF9AE6B4), Color(0xFFA5D8FF), Color(0xFFFFADAD),
    Color(0xFFCDB4FF), Color(0xFFFFC6FF),
)

private fun wordLabel(w: Word): String = w.text(Settings.lang)

/** A tappable card that bounces in, shakes on a wrong tap, and pops on the right one. */
@Composable
private fun AnswerCard(
    word: Word,
    color: Color,
    size: androidx.compose.ui.unit.Dp,
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
        if (highlight) 1.18f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "sc",
    )
    val a by animateFloatAsState(if (dimmed) 0.45f else 1f, tween(200), label = "a")
    PictureCard(
        emoji = word.emoji,
        label = wordLabel(word),
        color = if (highlight) Color(0xFFB8F2C0) else color,
        size = size,
        modifier = Modifier
            .scale(sc)
            .graphicsLayer { translationX = shake.value; alpha = a },
        onClick = onClick,
    )
}

@Composable
private fun SpeakerButton(accent: Color, onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val s by animateFloatAsState(if (pressed) 0.9f else 1f, tween(120), label = "spk")
    LaunchedEffect(pressed) { if (pressed) { delay(150); pressed = false } }
    Box(
        Modifier.size(96.dp).scale(s).clip(CircleShape).background(accent)
            .clickable { pressed = true; onClick() },
        contentAlignment = Alignment.Center,
    ) { Text("🔊", fontSize = 44.sp) }
}

@Composable
private fun CelebrateOverlay(show: Boolean) {
    val s by animateFloatAsState(
        if (show) 1f else 0f, spring(Spring.DampingRatioMediumBouncy), label = "cel",
    )
    if (s > 0.01f) {
        Text("🎉", fontSize = 64.sp, modifier = Modifier.scale(s))
    } else {
        Spacer(Modifier.height(0.dp))
    }
}

// ───────────────────────── 1. ChooseGame ─────────────────────────

@Composable
fun ChooseGame(host: GameHost) {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    val words = remember(round) { host.pick(4) }
    val target = remember(round) { words.random() }
    val wrongCounts = remember(round) { mutableStateListOf(0, 0, 0, 0) }
    var solved by remember(round) { mutableStateOf(false) }
    var firstTry by remember(round) { mutableStateOf(true) }
    var speakTick by remember(round) { mutableIntStateOf(0) }

    LaunchedEffect(round, speakTick) {
        delay(350)
        host.say(target)
    }

    GameFrame(host, "اختر الصورة الصحيحة", round, score) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SpeakerButton(host.accent) { speakTick++ }
            Spacer(Modifier.height(8.dp))
            if (Settings.showText) {
                Text(
                    wordLabel(target), fontSize = 28.sp, fontWeight = FontWeight.Black,
                    color = Ink, textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(12.dp))
            for (r in 0 until 2) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (c in 0 until 2) {
                        val i = r * 2 + c
                        val w = words[i]
                        AnswerCard(
                            word = w,
                            color = CardColors[i % CardColors.size],
                            size = 150.dp,
                            wrongCount = wrongCounts[i],
                            highlight = solved && w.id == target.id,
                            dimmed = solved && w.id != target.id,
                        ) {
                            if (solved || finished) return@AnswerCard
                            if (w.id == target.id) {
                                solved = true
                                if (firstTry) host.correct(target) else host.correct()
                                if (firstTry) score++
                            } else {
                                firstTry = false
                                wrongCounts[i] = wrongCounts[i] + 1
                                host.wrong()
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            CelebrateOverlay(solved)
        }
    }

    LaunchedEffect(solved) {
        if (solved) {
            delay(1300)
            if (round + 1 >= GameHost.ROUNDS) {
                if (!finished) { finished = true; host.finish(score, GameHost.ROUNDS) }
            } else {
                round++
            }
        }
    }
}

// ───────────────────────── 2. MemoryGame ─────────────────────────

private class MemCard(val word: Word, val pair: Int)

@Composable
private fun FlipCard(
    card: MemCard,
    faceUp: Boolean,
    matched: Boolean,
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val rot by animateFloatAsState(if (faceUp || matched) 180f else 0f, tween(300), label = "flip")
    val pop by animateFloatAsState(
        if (matched) 1.08f else 1f, spring(Spring.DampingRatioMediumBouncy), label = "pop",
    )
    val showFront = rot > 90f
    Box(
        modifier
            .scale(pop)
            .graphicsLayer { rotationY = rot; cameraDistance = 14f * density }
            .clip(RoundedCornerShape(20.dp))
            .background(if (showFront) (if (matched) Color(0xFFB8F2C0) else Color.White) else color)
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        if (showFront) {
            Column(
                Modifier.graphicsLayer { rotationY = 180f },
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Pic(card.word.emoji, 48.dp)
                if (Settings.showText) {
                    Text(
                        wordLabel(card.word), fontSize = 13.sp, fontWeight = FontWeight.Bold,
                        color = Ink, textAlign = TextAlign.Center, maxLines = 1,
                    )
                }
            }
        } else {
            Text("❓", fontSize = 36.sp)
        }
    }
}

@Composable
fun MemoryGame(host: GameHost) {
    val sizes = listOf(3, 4, 6)
    val total = sizes.sum()
    var stage by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val cards = remember(stage) {
        val ws = host.pool.shuffled().take(sizes[stage])
        val list = ArrayList<MemCard>()
        ws.forEachIndexed { i, w -> list.add(MemCard(w, i)); list.add(MemCard(w, i)) }
        list.shuffled()
    }
    val up = remember(stage) { mutableStateListOf<Boolean>().also { l -> repeat(cards.size) { l.add(false) } } }
    val done = remember(stage) { mutableStateListOf<Boolean>().also { l -> repeat(cards.size) { l.add(false) } } }
    var first by remember(stage) { mutableIntStateOf(-1) }
    var matchedHere by remember(stage) { mutableIntStateOf(0) }

    val shownRound = (score * GameHost.ROUNDS) / total

    GameFrame(host, "لعبة الذاكرة", shownRound, score) {
        val cols = if (cards.size == 6) 3 else 4
        val rows = cards.chunked(cols)
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            rows.forEachIndexed { r, rowCards ->
                Row(
                    Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    rowCards.forEachIndexed { c, card ->
                        val idx = r * cols + c
                        FlipCard(
                            card = card,
                            faceUp = up[idx],
                            matched = done[idx],
                            color = CardColors[(idx * 5 + stage) % CardColors.size],
                            modifier = Modifier.weight(1f).aspectRatio(0.85f),
                        ) {
                            if (busy || finished || up[idx] || done[idx]) return@FlipCard
                            Sfx.pop()
                            up[idx] = true
                            if (first < 0) {
                                first = idx
                            } else {
                                val a = first
                                first = -1
                                busy = true
                                scope.launch {
                                    delay(700)
                                    if (cards[a].pair == card.pair) {
                                        done[a] = true; done[idx] = true
                                        host.correct(card.word)
                                        host.say(card.word)
                                        score++
                                        matchedHere++
                                        delay(900)
                                        if (matchedHere >= sizes[stage]) {
                                            if (stage + 1 >= sizes.size) {
                                                if (!finished) {
                                                    finished = true
                                                    host.finish(score, total)
                                                }
                                            } else {
                                                stage++
                                            }
                                        }
                                    } else {
                                        host.wrong()
                                        delay(300)
                                        up[a] = false; up[idx] = false
                                    }
                                    busy = false
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ───────────────────────── 3. OddOneOutGame ─────────────────────────

private class OddRound(val items: List<Word>, val odd: Word)

private fun buildOddRound(host: GameHost): OddRound {
    val byCat = host.pool.groupBy { it.cat }.filter { it.value.size >= 3 }
    val mainPool: Map<Category, List<Word>> =
        if (byCat.isNotEmpty()) byCat else WordRepo.all.groupBy { it.cat }.filter { it.value.size >= 3 }
    val main = mainPool.keys.random()
    val same = mainPool.getValue(main).shuffled().take(3)
    val others = host.pool.filter { it.cat != main }.ifEmpty { WordRepo.all.filter { it.cat != main } }
    val odd = others.random()
    return OddRound((same + odd).shuffled(), odd)
}

@Composable
fun OddOneOutGame(host: GameHost) {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    val data = remember(round) { buildOddRound(host) }
    val wrongCounts = remember(round) { mutableStateListOf(0, 0, 0, 0) }
    var solved by remember(round) { mutableStateOf(false) }
    var firstTry by remember(round) { mutableStateOf(true) }

    GameFrame(host, "من المختلف؟", round, score) {
        Column(
            Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("🤔", fontSize = 56.sp)
            Spacer(Modifier.height(12.dp))
            for (r in 0 until 2) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    for (c in 0 until 2) {
                        val i = r * 2 + c
                        val w = data.items[i]
                        AnswerCard(
                            word = w,
                            color = CardColors[(i + round) % CardColors.size],
                            size = 150.dp,
                            wrongCount = wrongCounts[i],
                            highlight = solved && w.id == data.odd.id,
                            dimmed = solved && w.id != data.odd.id,
                        ) {
                            if (solved || finished) return@AnswerCard
                            if (w.id == data.odd.id) {
                                solved = true
                                if (firstTry) host.correct(w) else host.correct()
                                host.say(w)
                                if (firstTry) score++
                            } else {
                                firstTry = false
                                wrongCounts[i] = wrongCounts[i] + 1
                                host.wrong()
                            }
                        }
                    }
                }
                Spacer(Modifier.height(14.dp))
            }
            CelebrateOverlay(solved)
        }
    }

    LaunchedEffect(solved) {
        if (solved) {
            delay(1500)
            if (round + 1 >= GameHost.ROUNDS) {
                if (!finished) { finished = true; host.finish(score, GameHost.ROUNDS) }
            } else {
                round++
            }
        }
    }
}
