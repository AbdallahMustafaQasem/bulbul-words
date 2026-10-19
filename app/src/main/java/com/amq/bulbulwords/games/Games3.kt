package com.amq.bulbulwords.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amq.bulbulwords.data.Lang
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Word
import com.amq.bulbulwords.data.WordRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.random.Random

private val G3Colors = listOf(
    Color(0xFFFF6B6B), Color(0xFFFFD166), Color(0xFF2EC4B6), Color(0xFF9B5DE5),
    Color(0xFF00BBF9), Color(0xFFF15BB5), Color(0xFF8AC926), Color(0xFFFF9F1C),
)

/* ───────────────────────────── 1. Sounds ───────────────────────────── */

@Composable
fun SoundsGame(host: GameHost) {
    val soundPool = remember {
        val p = host.pool.filter { it.sound != null }.distinctBy { it.emoji }
        if (p.size >= 3) p else WordRepo.all.filter { it.sound != null }.distinctBy { it.emoji }
    }
    val targets = remember {
        val s = soundPool.shuffled()
        List(GameHost.ROUNDS) { s[it % s.size] }
    }
    var round by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var missed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val target = targets[round]
    val choices = remember(round) {
        (soundPool.filter { it.emoji != target.emoji }.shuffled().take(2) + target).shuffled()
    }

    LaunchedEffect(round) {
        delay(500)
        host.speaker.saySound(target)
    }

    val pulse = rememberInfiniteTransition(label = "pulse")
    val pulseScale by pulse.animateFloat(
        1f, 1.12f,
        infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "ps",
    )

    GameFrame(host, "أصوات", round, score) {
        Spacer(Modifier.height(8.dp))
        Text("من صاحب هذا الصوت؟", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
        Spacer(Modifier.height(20.dp))
        Box(
            Modifier
                .graphicsLayer { scaleX = pulseScale; scaleY = pulseScale }
                .size(150.dp)
                .clip(CircleShape)
                .background(Brush.radialGradient(listOf(Color.White, host.accent.copy(alpha = 0.6f), host.accent)))
                .clickable { host.speaker.saySound(target) },
            contentAlignment = Alignment.Center,
        ) { Text("🔊", fontSize = 76.sp) }
        Spacer(Modifier.height(36.dp))
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            choices.forEachIndexed { i, w ->
                val bounce = remember(round, i) { Animatable(0f) }
                LaunchedEffect(round, i) {
                    delay(i * 120L)
                    bounce.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                }
                PictureCard(
                    emoji = w.emoji,
                    label = null,
                    color = G3Colors[(round + i) % G3Colors.size].copy(alpha = 0.35f).compositeOverWhite(),
                    size = 104.dp,
                    modifier = Modifier.graphicsLayer { scaleX = bounce.value; scaleY = bounce.value },
                ) {
                    if (locked) return@PictureCard
                    if (w.emoji == target.emoji) {
                        locked = true
                        if (!missed) host.correct(target) else host.correct()
                        if (!missed) score++
                        host.speaker.say(target)
                        val finalScore = score
                        scope.launch {
                            delay(1800)
                            if (round + 1 >= GameHost.ROUNDS) {
                                host.finish(finalScore, GameHost.ROUNDS)
                            } else {
                                round++
                                missed = false
                                locked = false
                            }
                        }
                    } else {
                        missed = true
                        host.wrong()
                    }
                }
            }
        }
    }
}

private fun Color.compositeOverWhite(): Color {
    val a = alpha
    return Color(red * a + (1 - a), green * a + (1 - a), blue * a + (1 - a), 1f)
}

/* ───────────────────────────── 2. Balloons ───────────────────────────── */

@Composable
fun BalloonsGame(host: GameHost) {
    val targets = remember { host.pool.shuffled().let { s -> List(GameHost.ROUNDS) { s[it % s.size] } } }
    var round by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var missed by remember { mutableStateOf(false) }
    var poppedIdx by remember { mutableStateOf(-1) }
    val scope = rememberCoroutineScope()

    val target = targets[round]
    val balloons: List<Word> = remember(round) {
        (host.pool.filter { it.emoji != target.emoji }.distinctBy { it.emoji }.shuffled().take(3) + target).shuffled()
    }
    // per-round random layout jitter (x, y in 0..1) and colors
    val jitter = remember(round) { List(4) { Random.nextFloat() to Random.nextFloat() } }
    val colors = remember(round) { G3Colors.shuffled().take(4) }

    LaunchedEffect(round) {
        delay(600)
        host.say(target)
    }

    GameFrame(host, "البالونات", round, score) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            EmojiBubble("🔊", 60.dp, host.accent, onClick = { host.say(target) })
            Spacer(Modifier.width(12.dp))
            if (Settings.showText) {
                Text(
                    target.text(Settings.lang), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Ink,
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        BoxWithConstraints(Modifier.fillMaxWidth().weight(1f)) {
            val cellW = maxWidth / 2
            val cellH = maxHeight / 2
            val bw = 112.dp
            val bh = 150.dp
            key(round) {
                balloons.forEachIndexed { i, w ->
                    val col = i % 2
                    val row = i / 2
                    val (jx, jy) = jitter[i]
                    val x = cellW * col + ((cellW - bw).coerceAtLeast(0.dp)) * jx
                    val y = cellH * row + ((cellH - bh - 30.dp).coerceAtLeast(0.dp)) * jy
                    Balloon(
                        word = w,
                        color = colors[i],
                        w = bw, h = bh,
                        phase = i * 0.9f,
                        popped = poppedIdx == i,
                        enterDelay = i * 150L,
                        modifier = Modifier.offset(x = x, y = y),
                    ) {
                        if (locked) return@Balloon
                        if (w.emoji == target.emoji) {
                            locked = true
                            poppedIdx = i
                            Sfx.pop()
                            if (!missed) host.correct(target) else host.correct()
                            if (!missed) score++
                            host.say(target)
                            val finalScore = score
                            scope.launch {
                                delay(1500)
                                if (round + 1 >= GameHost.ROUNDS) {
                                    host.finish(finalScore, GameHost.ROUNDS)
                                } else {
                                    round++
                                    poppedIdx = -1
                                    missed = false
                                    locked = false
                                }
                            }
                        } else {
                            missed = true
                            host.wrong()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Balloon(
    word: Word,
    color: Color,
    w: Dp,
    h: Dp,
    phase: Float,
    popped: Boolean,
    enterDelay: Long,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val enter = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(enterDelay)
        enter.animateTo(1f, tween(1100, easing = FastOutSlowInEasing))
    }
    val drift = rememberInfiniteTransition(label = "drift")
    val t by drift.animateFloat(
        0f, 6.2831855f,
        infiniteRepeatable(tween(3200, easing = androidx.compose.animation.core.LinearEasing), RepeatMode.Restart),
        label = "t",
    )
    val popScale by animateFloatAsState(
        if (popped) 1.8f else 1f, tween(260), label = "popScale",
    )
    val popAlpha by animateFloatAsState(
        if (popped) 0f else 1f, tween(260), label = "popAlpha",
    )
    Column(
        modifier
            .graphicsLayer {
                translationY = (1f - enter.value) * 700f + sin(t + phase) * 18f
                translationX = sin(t * 0.5f + phase) * 8f
                scaleX = popScale
                scaleY = popScale
                alpha = popAlpha
            }
            .clickable(enabled = !popped) { onClick() },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            Modifier
                .size(width = w, height = h - 30.dp)
                .clip(RoundedCornerShape(percent = 50))
                .background(Brush.radialGradient(listOf(Color.White, color.copy(alpha = 0.7f), color))),
            contentAlignment = Alignment.Center,
        ) { Pic(word.emoji, 64.dp) }
        Box(Modifier.size(width = 14.dp, height = 8.dp).clip(RoundedCornerShape(2.dp)).background(color))
        Box(Modifier.size(width = 2.dp, height = 22.dp).background(Color(0x88000000)))
    }
}

/* ───────────────────────────── 3. Count ───────────────────────────── */

private val ArNumWords = listOf("", "واحد", "اثنان", "ثلاثة", "أربعة", "خمسة", "ستة")
private val EnNumWords = listOf("", "one", "two", "three", "four", "five", "six")
private const val ArDigits = "٠١٢٣٤٥٦٧٨٩"

private fun numeral(n: Int): String =
    if (Settings.lang != Lang.EN) ArDigits[n].toString() else n.toString()

@Composable
fun CountGame(host: GameHost) {
    val counts = remember {
        (((1..6).shuffled()) + List(2) { Random.nextInt(1, 7) }).take(GameHost.ROUNDS)
    }
    var round by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var locked by remember { mutableStateOf(false) }
    var missed by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val n = counts[round]
    val emoji = remember(round) { host.pick(1).first().emoji }
    val options = remember(round) {
        ((1..6).filter { it != n }.shuffled().take(2) + n).shuffled()
    }

    val bob = rememberInfiniteTransition(label = "bob")
    val bobScale by bob.animateFloat(
        0.92f, 1.08f,
        infiniteRepeatable(tween(650, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bs",
    )

    GameFrame(host, "العدّ", round, score) {
        Spacer(Modifier.height(4.dp))
        Text(
            if (Settings.lang != Lang.EN) "كم عددها؟" else "How many?",
            fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Ink,
        )
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White.copy(alpha = 0.75f))
                .padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            val rows = (0 until n).chunked(3)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
            ) {
                rows.forEach { r ->
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        r.forEach { idx ->
                            key(round, idx) {
                                val pop = remember { Animatable(0f) }
                                LaunchedEffect(Unit) {
                                    delay(idx * 150L)
                                    pop.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
                                }
                                Pic(
                                    emoji, 76.dp,
                                    modifier = Modifier.graphicsLayer {
                                        val s = pop.value * (if (idx % 2 == 0) bobScale else 2f - bobScale)
                                        scaleX = s; scaleY = s
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(
            Modifier.fillMaxWidth().padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            options.forEachIndexed { i, opt ->
                Box(
                    Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(G3Colors[(opt + i) % G3Colors.size])
                        .clickable {
                            if (locked) return@clickable
                            Sfx.tap()
                            if (opt == n) {
                                locked = true
                                host.correct()
                                if (!missed) score++
                                if (Settings.lang != Lang.EN) host.speaker.sayText(ArNumWords[n])
                                else host.speaker.sayText(EnNumWords[n], Lang.EN)
                                val finalScore = score
                                scope.launch {
                                    delay(1700)
                                    if (round + 1 >= GameHost.ROUNDS) {
                                        host.finish(finalScore, GameHost.ROUNDS)
                                    } else {
                                        round++
                                        missed = false
                                        locked = false
                                    }
                                }
                            } else {
                                missed = true
                                host.wrong()
                            }
                        },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        numeral(opt), fontSize = 56.sp, fontWeight = FontWeight.Black,
                        color = Color.White, textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
