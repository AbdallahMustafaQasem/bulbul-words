package com.amq.bulbulwords.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import kotlin.math.min
import kotlin.random.Random

private class G11Item(val word: Word, val isTarget: Boolean, val x: Float, val y: Float, val size: Float)

private val g11Abstract = setOf(com.amq.bulbulwords.data.Category.ACTIONS, com.amq.bulbulwords.data.Category.FEELINGS, com.amq.bulbulwords.data.Category.NUMBERS, com.amq.bulbulwords.data.Category.LETTERS, com.amq.bulbulwords.data.Category.FAMILY, com.amq.bulbulwords.data.Category.PLACES)
private fun g11Plain(w: Word) = !w.emoji.startsWith("draw:") && !w.emoji.startsWith("txt:") && w.cat !in g11Abstract

/** Picks the target plus distractors, each distractor from a different category when possible. */
private fun g11Words(host: GameHost, others: Int): Pair<Word, List<Word>> {
    var base = host.pool.filter { g11Plain(it) }
    if (base.size < others + 1) base = WordRepo.of(null).filter { g11Plain(it) }
    val target = base.random()
    val rest = base.filter { it.id != target.id && it.emoji != target.emoji }.shuffled()
    val chosen = ArrayList<Word>()
    val cats = HashSet<Any>().apply { add(target.cat) }
    for (w in rest) {
        if (chosen.size >= others) break
        if (cats.add(w.cat)) chosen.add(w)
    }
    for (w in rest) {
        if (chosen.size >= others) break
        if (chosen.none { it.id == w.id }) chosen.add(w)
    }
    return target to chosen
}

private fun g11Layout(
    words: List<Pair<Word, Boolean>>, cols: Int, rows: Int, w: Float, h: Float, rnd: Random,
): List<G11Item> {
    val cw = w / cols
    val ch = h / rows
    val base = min(cw, ch) * 0.86f
    val slots = (0 until cols * rows).toList().shuffled(rnd)
    return words.mapIndexed { i, (word, isT) ->
        val slot = slots[i]
        val c = slot % cols
        val r = slot / cols
        val size = (base * (0.82f + rnd.nextFloat() * 0.18f)).coerceAtLeast(56f)
        val spareX = (cw - size).coerceAtLeast(0f)
        val spareY = (ch - size).coerceAtLeast(0f)
        G11Item(word, isT, c * cw + rnd.nextFloat() * spareX, r * ch + rnd.nextFloat() * spareY, size)
    }
}

@Composable
private fun G11Thing(
    item: G11Item,
    wobbleTick: Int,
    found: Boolean,
    hint: Boolean,
    onTap: () -> Unit,
) {
    val rot = remember { Animatable(0f) }
    LaunchedEffect(wobbleTick) {
        if (wobbleTick > 0) {
            repeat(2) {
                rot.animateTo(9f, tween(80))
                rot.animateTo(-9f, tween(80))
            }
            rot.animateTo(0f, tween(80))
        }
    }
    val pop = remember { Animatable(1f) }
    LaunchedEffect(found) {
        if (found) {
            pop.animateTo(1.35f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium))
            pop.animateTo(1.15f, tween(200))
        }
    }
    val inf = rememberInfiniteTransition(label = "g11p")
    val pulse by inf.animateFloat(
        1f, 1.18f, infiniteRepeatable(tween(650), RepeatMode.Reverse), label = "g11pp",
    )
    val sc = if (hint && !found) pulse else pop.value
    Box(
        Modifier
            .absoluteOffset(item.x.dp, item.y.dp)
            .size(item.size.dp)
            .graphicsLayer { rotationZ = rot.value }
            .scale(sc)
            .clip(CircleShape)
            .background(if (found) Color(0x66FFE066) else Color.Transparent)
            .clickable { onTap() },
        contentAlignment = Alignment.Center,
    ) {
        Pic(item.word.emoji, (item.size * 0.86f).dp)
        if (found) {
            Text("⭐", fontSize = 22.sp, modifier = Modifier.align(Alignment.TopEnd))
            Text("✨", fontSize = 18.sp, modifier = Modifier.align(Alignment.BottomStart))
        }
    }
}

@Composable
fun FindGame(host: GameHost) {
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    var finished by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val findAll = round >= 6
    val count = if (round < 3) 6 else 9
    val cols = if (round < 3) 2 else 3
    val rows = if (round < 3) 3 else if (findAll) 4 else 3

    val picked = remember(round) { g11Words(host, if (findAll) 9 else count - 1) }
    val target = picked.first
    val seed = remember(round) { Random.nextInt() }
    val found = remember(round) { mutableStateListOf<Int>() }
    val wobble = remember(round) { mutableStateListOf(*Array(12) { 0 }) }
    var wrongTaps by remember(round) { mutableIntStateOf(0) }
    var hostWrongDone by remember(round) { mutableStateOf(false) }
    var solved by remember(round) { mutableStateOf(false) }
    var speakTick by remember(round) { mutableIntStateOf(0) }

    val name = target.text(Settings.lang)
    val prompt = if (findAll) {
        if (Settings.lang == Lang.EN) "Find all 3 copies of: $name" else "ابحث عن ٣ نسخ من: ${arDef(name)}"
    } else {
        if (Settings.lang == Lang.EN) "Find $name" else "ابحث عن ${arDef(name)}"
    }

    LaunchedEffect(round, speakTick) {
        delay(350)
        host.speaker.sayText(prompt, Settings.lang)
    }

    GameFrame(host, "ابحث عن…", round, score) {
        Text(
            prompt,
            Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).clickable { speakTick++ }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center,
        )
        if (findAll) {
            Text(
                "${found.size}/3", fontSize = 22.sp, fontWeight = FontWeight.Bold,
                color = Ink, textAlign = TextAlign.Center,
            )
        }
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
            BoxWithConstraints(
                Modifier.widthIn(max = 640.dp).fillMaxSize().padding(bottom = 8.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFBDE7FF), Color(0xFFD8F5C0), Color(0xFF9EDB84)),
                        )
                    ),
            ) {
                val pad = 10f
                val w = maxWidth.value - pad * 2
                val h = maxHeight.value - pad * 2
                val items = remember(round, w, h) {
                    val list = ArrayList<Pair<Word, Boolean>>()
                    if (findAll) {
                        repeat(3) { list.add(target to true) }
                        picked.second.forEach { list.add(it to false) }
                    } else {
                        list.add(target to true)
                        picked.second.forEach { list.add(it to false) }
                    }
                    g11Layout(list, cols, rows, w, h, Random(seed))
                }
                androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Ltr) {
                Box(Modifier.padding(pad.dp).fillMaxSize()) {
                    items.forEachIndexed { i, item ->
                        G11Thing(
                            item = item,
                            wobbleTick = wobble[i],
                            found = i in found,
                            hint = wrongTaps >= 2 && item.isTarget && !solved,
                        ) {
                            if (solved || i in found) return@G11Thing
                            if (item.isTarget) {
                                found.add(i)
                                val allDone = !findAll || found.size >= 3
                                if (!findAll || allDone) {
                                    solved = true
                                    if (wrongTaps == 0) {
                                        score++
                                        host.correct(target)
                                    } else host.correct()
                                } else Sfx.tap()
                                host.say(target)
                                if (allDone) {
                                    scope.launch {
                                        delay(1200)
                                        if (round >= GameHost.ROUNDS - 1) {
                                            if (!finished) {
                                                finished = true
                                                host.finish(score, GameHost.ROUNDS)
                                            }
                                        } else round++
                                    }
                                }
                            } else {
                                wrongTaps++
                                wobble[i] = wobble[i] + 1
                                if (!hostWrongDone) {
                                    hostWrongDone = true
                                    host.wrong()
                                }
                            }
                        }
                    }
                }
                }
            }
        }
    }
}
