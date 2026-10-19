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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import kotlinx.coroutines.delay

private val G7Colors = listOf(
    Color(0xFFFFD166), Color(0xFF9AE6B4), Color(0xFFA5D8FF), Color(0xFFFFADAD),
    Color(0xFFCDB4FF), Color(0xFFFFC6FF),
)

private fun g7Lang(): Lang = if (Settings.lang == Lang.EN) Lang.EN else Lang.FUSHA
private fun g7En(): Boolean = Settings.lang == Lang.EN

private fun g7Speak(host: GameHost, text: String) = host.speaker.sayText(text, g7Lang())

@Composable
private fun G7Shake(tick: Int): Animatable<Float, androidx.compose.animation.core.AnimationVector1D> {
    val shake = remember { Animatable(0f) }
    LaunchedEffect(tick) {
        if (tick > 0) {
            repeat(3) {
                shake.animateTo(14f, tween(50))
                shake.animateTo(-14f, tween(50))
            }
            shake.animateTo(0f, tween(50))
        }
    }
    return shake
}

@Composable
private fun G7Celebrate(show: Boolean) {
    val s by animateFloatAsState(
        if (show) 1f else 0f, spring(Spring.DampingRatioMediumBouncy), label = "g7cel",
    )
    if (s > 0.01f) Text("🎉", fontSize = 56.sp, modifier = Modifier.scale(s))
    else Spacer(Modifier.height(0.dp))
}

// ───────────────────────── 1. SequenceGame ─────────────────────────

private class G7Step(val ar: String, val en: String, val emoji: String) {
    fun text() = if (g7En()) en else ar
}

private class G7Routine(val steps: List<G7Step>)

private val g7Routines: List<G7Routine> = listOf(
    G7Routine(listOf(
        G7Step("نفتح الماء", "Turn on the water", "🚰"),
        G7Step("نضع الصابون", "Use soap", "🧼"),
        G7Step("ننشف اليدين", "Dry hands", "🧻"),
    )),
    G7Routine(listOf(
        G7Step("نأخذ الفرشاة", "Take the brush", "🪥"),
        G7Step("ننظف أسناننا", "Brush the teeth", "🦷"),
        G7Step("نشطف الفم", "Rinse", "🚰"),
    )),
    G7Routine(listOf(
        G7Step("نلبس القميص", "Put on the shirt", "👕"),
        G7Step("نلبس البنطال", "Put on the pants", "👖"),
        G7Step("نلبس الحذاء", "Put on the shoes", "👟"),
    )),
    G7Routine(listOf(
        G7Step("نجلس ونغسل أيدينا", "Sit and wash hands", "🍽️"),
        G7Step("نأكل بالملعقة", "Eat with a spoon", "🥄"),
        G7Step("نشرب الحليب", "Drink milk", "🥛"),
    )),
    G7Routine(listOf(
        G7Step("نزرع البذرة", "Plant the seed", "🌱"),
        G7Step("نسقيها بالماء", "Water it", "💧"),
        G7Step("تصبح زهرة", "It becomes a flower", "🌻"),
    )),
    G7Routine(listOf(
        G7Step("نستحم", "Take a bath", "🛁"),
        G7Step("نسمع قصة", "Hear a story", "📖"),
        G7Step("ننام", "Go to sleep", "🛌"),
    )),
    G7Routine(listOf(
        G7Step("نخلط المكونات", "Mix it", "🥣"),
        G7Step("نخبز في الفرن", "Bake it", "🔥"),
        G7Step("نأكل الخبز", "Eat the bread", "🍞"),
    )),
    G7Routine(listOf(
        G7Step("نلبس المعطف", "Put on the coat", "🧥"),
        G7Step("نفتح الباب", "Open the door", "🚪"),
        G7Step("نركب السيارة", "Get in the car", "🚗"),
    )),
    G7Routine(listOf(
        G7Step("نأخذ كوباً", "Take a cup", "🥛"),
        G7Step("نصب العصير", "Pour the juice", "🧃"),
        G7Step("نشرب", "Drink", "😋"),
    )),
    G7Routine(listOf(
        G7Step("نفتح الكتاب", "Open the book", "📖"),
        G7Step("نقرأ القصة", "Read the story", "👀"),
        G7Step("نغلق الكتاب", "Close the book", "📕"),
    )),
)

private fun g7WholeSequence(r: G7Routine): String {
    val s = r.steps
    return if (g7En()) "First ${s[0].en}, then ${s[1].en}, and finally ${s[2].en}."
    else "أولاً ${s[0].ar}، ثم ${s[1].ar}، وأخيراً ${s[2].ar}."
}

@Composable
private fun G7StepCard(
    step: G7Step,
    color: Color,
    placed: Boolean,
    hint: Boolean,
    shakeTick: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val shake = G7Shake(shakeTick)
    val pulse = rememberInfiniteTransition(label = "g7pulse")
    val ps by pulse.animateFloat(
        1f, 1.12f,
        infiniteRepeatable(tween(500, easing = LinearEasing), RepeatMode.Reverse), label = "g7ps",
    )
    val sc = if (hint && !placed) ps else 1f
    Column(
        modifier
            .graphicsLayer { translationX = shake.value; alpha = if (placed) 0f else 1f }
            .scale(sc)
            .clip(RoundedCornerShape(24.dp))
            .background(if (hint) Color(0xFFFFF3B0) else color)
            .clickable(enabled = !placed) { Sfx.tap(); onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Pic(step.emoji, 64.dp)
        if (Settings.showText) {
            Text(
                step.text(), fontSize = 14.sp, fontWeight = FontWeight.Bold,
                color = Ink, textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun G7Slot(n: Int, step: G7Step?, modifier: Modifier) {
    val sc by animateFloatAsState(
        if (step != null) 1f else 0.94f, spring(Spring.DampingRatioMediumBouncy), label = "g7slot",
    )
    Column(
        modifier
            .scale(sc)
            .clip(RoundedCornerShape(20.dp))
            .background(if (step != null) Color(0xFFB8F2C0) else Color(0x55FFFFFF))
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("$n", fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink)
        if (step != null) Pic(step.emoji, 48.dp) else Spacer(Modifier.height(48.dp))
    }
}

@Composable
fun SequenceGame(host: GameHost) {
    val total = 5
    val routines = remember { g7Routines.shuffled().take(total) }
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val routine = routines[round]
    val order = remember(round) {
        var o: List<Int>
        do { o = listOf(0, 1, 2).shuffled() } while (o == listOf(0, 1, 2))
        o
    }
    val placed = remember(round) { mutableStateListOf<Int>() } // step indices in order
    val shakeTicks = remember(round) { mutableStateListOf(0, 0, 0) }
    var misses by remember(round) { mutableIntStateOf(0) }
    var everMissed by remember(round) { mutableStateOf(false) }
    var done by remember(round) { mutableStateOf(false) }

    LaunchedEffect(done) {
        if (done) {
            delay(900)
            g7Speak(host, g7WholeSequence(routine))
            host.correct()
            if (!everMissed) score++
            delay(4200)
            if (round + 1 >= total) {
                if (!finished) { finished = true; host.finish(score, total) }
            } else {
                round++
            }
        }
    }

    GameFrame(host, "ماذا بعد؟", (round * GameHost.ROUNDS) / total, score) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (n in 0 until 3) {
                    G7Slot(n + 1, placed.getOrNull(n)?.let { routine.steps[it] }, Modifier.weight(1f))
                }
            }
            Spacer(Modifier.height(24.dp))
            Text("👆", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (p in 0 until 3) {
                    val si = order[p]
                    val isPlaced = si in placed
                    G7StepCard(
                        step = routine.steps[si],
                        color = G7Colors[(p + round) % G7Colors.size],
                        placed = isPlaced,
                        hint = misses >= 2 && !done && si == placed.size,
                        shakeTick = shakeTicks[p],
                        modifier = Modifier.weight(1f).aspectRatio(0.8f),
                    ) {
                        if (done || finished || isPlaced) return@G7StepCard
                        if (si == placed.size) {
                            placed.add(si)
                            misses = 0
                            g7Speak(host, routine.steps[si].text())
                            if (placed.size == 3) done = true
                        } else {
                            misses++
                            shakeTicks[p] = shakeTicks[p] + 1
                            if (!everMissed) host.wrong()
                            everMissed = true
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            G7Celebrate(done)
        }
    }
}

// ───────────────────────── 2. FunctionGame ─────────────────────────

private class G7Fn(
    val emoji: String,
    val qAr: String, val qEn: String,
    val aAr: String, val aEn: String,
) {
    fun q() = if (g7En()) qEn else qAr
    fun a() = if (g7En()) aEn else aAr
}

private val g7Fns: List<G7Fn> = listOf(
    G7Fn("🥄", "بماذا نأكل؟", "What do we eat with?", "نأكل بالملعقة", "We eat with a spoon"),
    G7Fn("🥛", "بماذا نشرب؟", "What do we drink from?", "نشرب من الكوب", "We drink from a cup"),
    G7Fn("🪥", "بماذا ننظف أسناننا؟", "What do we brush our teeth with?", "ننظف أسناننا بالفرشاة", "We brush our teeth with a toothbrush"),
    G7Fn("🛏️", "أين ننام؟", "Where do we sleep?", "ننام على السرير", "We sleep in a bed"),
    G7Fn("👟", "ماذا نلبس في أقدامنا؟", "What do we wear on our feet?", "نلبس الحذاء", "We wear shoes"),
    G7Fn("✏️", "بماذا نكتب؟", "What do we write with?", "نكتب بالقلم", "We write with a pencil"),
    G7Fn("🧼", "بماذا نغسل أيدينا؟", "What do we wash our hands with?", "نغسل أيدينا بالصابون", "We wash our hands with soap"),
    G7Fn("⚽", "بماذا نلعب؟", "What do we play with?", "نلعب بالكرة", "We play with a ball"),
    G7Fn("✂️", "بماذا نقص الورق؟", "What do we cut paper with?", "نقص الورق بالمقص", "We cut paper with scissors"),
    G7Fn("☂️", "ماذا نحمل عندما تمطر؟", "What do we carry when it rains?", "نحمل المظلة", "We carry an umbrella"),
    G7Fn("🔑", "بماذا نفتح الباب؟", "What do we open the door with?", "نفتح الباب بالمفتاح", "We open the door with a key"),
    G7Fn("📱", "بماذا نتصل؟", "What do we call with?", "نتصل بالهاتف", "We call with a phone"),
    G7Fn("📖", "ماذا نقرأ؟", "What do we read?", "نقرأ الكتاب", "We read a book"),
    G7Fn("💇", "ماذا نفعل بالمشط؟", "What do we use a comb for?", "نسرّح شعرنا بالمشط", "We comb our hair with a comb"),
    G7Fn("🚲", "ماذا نركب؟", "What do we ride?", "نركب الدراجة", "We ride a bicycle"),
)

@Composable
private fun G7Choice(
    fn: G7Fn,
    color: Color,
    highlight: Boolean,
    dimmed: Boolean,
    shakeTick: Int,
    modifier: Modifier,
    onClick: () -> Unit,
) {
    val shake = G7Shake(shakeTick)
    val sc by animateFloatAsState(
        if (highlight) 1.15f else 1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "g7sc",
    )
    val a by animateFloatAsState(if (dimmed) 0.45f else 1f, tween(200), label = "g7a")
    Box(
        modifier
            .scale(sc)
            .graphicsLayer { translationX = shake.value; alpha = a }
            .clip(RoundedCornerShape(28.dp))
            .background(if (highlight) Color(0xFFB8F2C0) else color)
            .clickable { Sfx.tap(); onClick() },
        contentAlignment = Alignment.Center,
    ) { Pic(fn.emoji, 72.dp) }
}

@Composable
fun FunctionGame(host: GameHost) {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }

    val picks = remember(round) {
        val target = g7Fns.random()
        val distractors = g7Fns.filter { it !== target }.shuffled().take(2)
        Pair(target, (listOf(target) + distractors).shuffled())
    }
    val target = picks.first
    val choices = picks.second
    val shakeTicks = remember(round) { mutableStateListOf(0, 0, 0) }
    var solved by remember(round) { mutableStateOf(false) }
    var firstTry by remember(round) { mutableStateOf(true) }

    LaunchedEffect(round) {
        delay(350)
        g7Speak(host, target.q())
    }

    LaunchedEffect(solved) {
        if (solved) {
            delay(2600)
            if (round + 1 >= GameHost.ROUNDS) {
                if (!finished) { finished = true; host.finish(score, GameHost.ROUNDS) }
            } else {
                round++
            }
        }
    }

    GameFrame(host, "ما الوظيفة؟", round, score) {
        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                Modifier.fillMaxWidth().clip(RoundedCornerShape(28.dp)).background(Color.White)
                    .clickable { g7Speak(host, target.q()) }.padding(20.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "🔊 " + target.q(), fontSize = 30.sp, fontWeight = FontWeight.Black,
                    color = Ink, textAlign = TextAlign.Center,
                )
            }
            Spacer(Modifier.height(28.dp))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                choices.forEachIndexed { i, c ->
                    G7Choice(
                        fn = c,
                        color = G7Colors[(i + round) % G7Colors.size],
                        highlight = solved && c === target,
                        dimmed = solved && c !== target,
                        shakeTick = shakeTicks[i],
                        modifier = Modifier.weight(1f).aspectRatio(0.8f),
                    ) {
                        if (solved || finished) return@G7Choice
                        if (c === target) {
                            solved = true
                            host.correct()
                            if (firstTry) score++
                            g7Speak(host, target.a())
                        } else {
                            shakeTicks[i] = shakeTicks[i] + 1
                            if (firstTry) host.wrong()
                            firstTry = false
                        }
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            if (solved && Settings.showText) {
                Text(
                    target.a(), fontSize = 24.sp, fontWeight = FontWeight.Bold,
                    color = Ink, textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
            }
            G7Celebrate(solved)
        }
    }
}
