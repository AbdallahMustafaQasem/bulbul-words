package com.amq.bulbulwords.games

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.amq.bulbulwords.data.Category
import com.amq.bulbulwords.data.Lang
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Word
import com.amq.bulbulwords.data.WordRepo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private class G6Round(
    val feed: Boolean,
    val subject: Word?,
    val target: Word,
    val options: List<Word>,
)

private fun g6Plain(w: Word) = !w.emoji.startsWith("draw:") && !w.emoji.startsWith("txt:")

private fun g6Build(): List<G6Round> {
    val friendly = setOf("an_cat", "an_dog", "an_cow", "an_horse", "an_sheep", "an_goat", "an_elephant", "an_monkey", "an_bear", "an_panda", "an_rabbit", "an_hamster", "an_giraffe", "an_pig", "an_duck", "an_lion")
    val animals = WordRepo.of(Category.ANIMALS).filter { g6Plain(it) && it.id in friendly }.shuffled()
    val foods = (WordRepo.of(Category.FRUITS) + WordRepo.of(Category.VEGETABLES) + WordRepo.of(Category.FOOD))
        .filter { g6Plain(it) }.shuffled()
    val clothes = WordRepo.of(Category.CLOTHES).filter { g6Plain(it) }.shuffled()
    var fi = 0
    var ci = 0
    return List(GameHost.ROUNDS) { i ->
        if (i % 2 == 0 && foods.size >= 3) {
            val start = (fi * 3) % foods.size
            val trio = List(3) { foods[(start + it) % foods.size] }
            fi++
            val t = trio.random()
            G6Round(true, animals.getOrNull((i / 2) % maxOf(1, animals.size)), t, trio.shuffled())
        } else {
            val src = if (clothes.size >= 3) clothes else foods
            val start = (ci * 3) % src.size
            val trio = List(3) { src[(start + it) % src.size] }
            ci++
            val t = trio.random()
            G6Round(false, null, t, trio.shuffled())
        }
    }
}

private fun g6Prompt(rd: G6Round): String {
    val lang = Settings.lang
    val t = rd.target.text(lang)
    return if (rd.feed) {
        val a = rd.subject?.text(lang) ?: ""
        if (lang == Lang.EN) "The $a wants $t" else "${arDef(a)} يريد $t"
    } else {
        if (lang == Lang.EN) "Put on the $t" else "ألبس ${arDef(t)}"
    }
}

@Composable
fun DressFeedGame(host: GameHost) {
    val rounds = remember { g6Build() }
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    GameFrame(host, "ألبس وأطعم", round, score) {
        key(round) {
            G6RoundView(host, rounds[round]) { firstTry ->
                if (firstTry) score++
                if (round + 1 >= GameHost.ROUNDS) host.finish(score, GameHost.ROUNDS) else round++
            }
        }
    }
}

@Composable
private fun androidx.compose.foundation.layout.ColumnScope.G6RoundView(host: GameHost, rd: G6Round, onDone: (Boolean) -> Unit) {
    var solved by remember { mutableStateOf(false) }
    var wrongUsed by remember { mutableStateOf(false) }
    var wrongCount by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var targetBounds by remember { mutableStateOf<Rect?>(null) }
    val bounce = remember { Animatable(1f) }
    val shake = remember { Animatable(0f) }
    val prompt = remember { g6Prompt(rd) }
    val doneState = rememberUpdatedState(onDone)

    LaunchedEffect(Unit) {
        delay(300)
        host.speaker.sayText(prompt)
    }
    LaunchedEffect(wrongCount) {
        if (wrongCount > 0) {
            repeat(2) {
                shake.animateTo(12f, tween(50))
                shake.animateTo(-12f, tween(50))
            }
            shake.animateTo(0f, tween(50))
        }
    }
    LaunchedEffect(solved) {
        if (solved) {
            bounce.animateTo(1.25f, tween(160))
            bounce.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessLow))
            delay(1000)
            doneState.value(!wrongUsed)
        }
    }

    fun attempt(idx: Int, inside: Boolean) {
        if (solved) return
        val w = rd.options[idx]
        if (inside && w.id == rd.target.id) {
            solved = true
            selected = -1
            if (!wrongUsed) host.correct(w) else host.correct()
            host.speaker.sayText(w.text(Settings.lang), queue = true)
        } else {
            if (!wrongUsed) { wrongUsed = true; host.wrong() }
            wrongCount++
            selected = -1
        }
    }

    Text(
        prompt,
        Modifier.fillMaxWidth().clickable { host.speaker.sayText(prompt) },
        fontSize = 24.sp, fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(8.dp))

    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(240.dp)
                .onGloballyPositioned { targetBounds = it.boundsInRoot() }
                .graphicsLayer { translationX = shake.value }
                .scale(bounce.value)
                .clip(CircleShape)
                .background(if (rd.feed) Color(0xFFFFE08A) else Color(0xFFBDE3FF))
                .clickable { if (selected >= 0) attempt(selected, true) },
            contentAlignment = Alignment.Center,
        ) {
            Pic(if (rd.feed) (rd.subject?.emoji ?: "🐶") else "🧒", 170.dp)
            if (solved) {
                if (rd.feed) {
                    Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 8.dp)) { Pic(rd.target.emoji, 64.dp) }
                } else {
                    Box(Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 6.dp)) { Pic(rd.target.emoji, 90.dp) }
                }
            }
        }
    }

    Row(
        Modifier.fillMaxWidth().height(130.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        rd.options.forEachIndexed { idx, w ->
            val hidden = solved && w.id == rd.target.id
            G6Item(
                word = w,
                hidden = hidden,
                enabled = !solved,
                selected = selected == idx,
                onTap = { selected = if (selected == idx) -1 else idx },
                onDrop = { center ->
                    val inside = targetBounds?.contains(center) == true
                    attempt(idx, inside)
                    inside && w.id == rd.target.id
                },
            )
        }
    }
}

@Composable
private fun G6Item(
    word: Word,
    hidden: Boolean,
    enabled: Boolean,
    selected: Boolean,
    onTap: () -> Unit,
    onDrop: (Offset) -> Boolean,
) {
    val ox = remember { Animatable(0f) }
    val oy = remember { Animatable(0f) }
    var bounds by remember { mutableStateOf<Rect?>(null) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dropState = rememberUpdatedState(onDrop)
    val enabledState = rememberUpdatedState(enabled)
    val sc by animateFloatAsState(
        if (dragging || selected) 1.2f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "g6sc",
    )

    Box(
        Modifier
            .size(100.dp)
            .zIndex(if (dragging) 10f else 0f)
            .onGloballyPositioned { bounds = it.boundsInRoot() }
            .graphicsLayer {
                translationX = ox.value
                translationY = oy.value
                alpha = if (hidden) 0f else 1f
                scaleX = sc
                scaleY = sc
            }
            .clip(RoundedCornerShape(26.dp))
            .background(if (selected) Color(0xFFB8F2C0) else Color.White)
            .clickable(enabled = enabled && !hidden) { Sfx.tap(); onTap() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { if (enabledState.value) dragging = true },
                    onDrag = { change, amt ->
                        if (enabledState.value) {
                            change.consume()
                            scope.launch {
                                ox.snapTo(ox.value + amt.x)
                                oy.snapTo(oy.value + amt.y)
                            }
                        }
                    },
                    onDragEnd = {
                        dragging = false
                        val b = bounds
                        val ok = if (b != null && enabledState.value) {
                            dropState.value(b.center + Offset(ox.value, oy.value))
                        } else false
                        if (!ok) {
                            scope.launch {
                                launch { oy.animateTo(0f, spring(Spring.DampingRatioMediumBouncy)) }
                                ox.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
                                repeat(2) {
                                    ox.animateTo(10f, tween(40))
                                    ox.animateTo(-10f, tween(40))
                                }
                                ox.animateTo(0f, tween(40))
                            }
                        }
                    },
                    onDragCancel = {
                        dragging = false
                        scope.launch {
                            launch { oy.animateTo(0f, spring()) }
                            ox.animateTo(0f, spring())
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) { Pic(word.emoji, 80.dp) }
}
