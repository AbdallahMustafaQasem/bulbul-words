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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.runtime.mutableStateListOf
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

private class G10Round(
    val items: List<Word>,
    val targets: List<Word>,
    val character: String,
)

private fun g10Plain(w: Word) = !w.emoji.startsWith("draw:") && !w.emoji.startsWith("txt:")

private fun g10Build(): List<G10Round> {
    val cats = listOf(Category.FRUITS, Category.FOOD, Category.HOME, Category.CLOTHES, Category.TRANSPORT)
    val allowed = setOf(
        "fr_apple", "fr_banana", "fr_orange", "fr_pear", "fr_strawberry", "fr_grapes", "fr_watermelon", "fr_lemon", "fr_cherries", "fr_pineapple",
        "fd_bread", "fd_cheese", "fd_egg", "fd_cookie", "fd_cake", "fd_ice_cream", "fd_milk", "fd_juice", "fd_pizza", "fd_donut",
        "hm_cup", "hm_spoon", "hm_plate", "hm_key", "hm_clock", "hm_book", "hm_pencil", "hm_teddy", "hm_balloon", "hm_gift", "hm_bucket", "hm_basket", "hm_umbrella", "hm_lamp", "hm_bottle", "hm_phone",
        "cl_tshirt", "cl_pants", "cl_dress", "cl_coat", "cl_scarf", "cl_gloves", "cl_cap", "cl_hat", "cl_sneaker", "cl_socks", "cl_backpack", "cl_glasses",
        "tr_car", "tr_bus", "tr_plane", "tr_train", "tr_boat", "tr_bike", "tr_helicopter", "tr_rocket", "tr_tractor",
    )
    val pool = cats.flatMap { WordRepo.of(it) }
        .filter { g10Plain(it) && it.id in allowed }
        .distinctBy { it.emoji }
        .distinctBy { it.ar }
    val usedTargets = mutableSetOf<String>()
    return List(GameHost.ROUNDS) { i ->
        val n = if (i < 3) 3 else 4
        val items = pool.shuffled().take(n)
        val need = if (i >= 5) 2 else 1
        val fresh = items.filter { it.id !in usedTargets }.shuffled()
        val rest = items.filter { it.id in usedTargets }.shuffled()
        val targets = (fresh + rest).take(need)
        usedTargets.addAll(targets.map { it.id })
        G10Round(items, targets, if (i % 2 == 0) "🧒" else "👩")
    }
}

private fun g10Prompt(rd: G10Round): String {
    val lang = Settings.lang
    val a = rd.targets[0].text(lang)
    val b = rd.targets.getOrNull(1)?.text(lang)
    return if (lang == Lang.EN) {
        if (b == null) "Give me the $a" else "Give me the $a, then the $b"
    } else {
        if (b == null) "أعطني ${arDef(a)}" else "أعطني ${arDef(a)} ثم ${arDef(b)}"
    }
}

@Composable
fun GiveMeGame(host: GameHost) {
    val rounds = remember { g10Build() }
    var round by remember { mutableIntStateOf(0) }
    var score by remember { mutableIntStateOf(0) }
    GameFrame(host, "أعطني…", round, score) {
        key(round) {
            G10RoundView(host, rounds[round]) { firstTry ->
                if (firstTry) score++
                if (round + 1 >= GameHost.ROUNDS) host.finish(score, GameHost.ROUNDS) else round++
            }
        }
    }
}

@Composable
private fun ColumnScope.G10RoundView(host: GameHost, rd: G10Round, onDone: (Boolean) -> Unit) {
    val given = remember { mutableStateListOf<String>() }
    var solved by remember { mutableStateOf(false) }
    var wrongUsed by remember { mutableStateOf(false) }
    var misses by remember { mutableIntStateOf(0) }
    var selected by remember { mutableIntStateOf(-1) }
    var dragIdx by remember { mutableIntStateOf(-1) }
    var thanks by remember { mutableStateOf(false) }
    var charBounds by remember { mutableStateOf<Rect?>(null) }
    val bounce = remember { Animatable(1f) }
    val shake = remember { Animatable(0f) }
    val prompt = remember { g10Prompt(rd) }
    val doneState = rememberUpdatedState(onDone)
    val lang = Settings.lang

    LaunchedEffect(Unit) {
        delay(300)
        host.speaker.sayText(prompt, lang)
    }
    LaunchedEffect(misses) {
        if (misses > 0) {
            repeat(2) {
                shake.animateTo(12f, tween(50))
                shake.animateTo(-12f, tween(50))
            }
            shake.animateTo(0f, tween(50))
        }
    }
    LaunchedEffect(given.size) {
        if (given.size > 0) {
            thanks = true
            bounce.animateTo(1.2f, tween(160))
            bounce.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessLow))
            delay(700)
            thanks = false
        }
    }
    LaunchedEffect(solved) {
        if (solved) {
            delay(1300)
            doneState.value(!wrongUsed)
        }
    }

    /** Called when an item is handed to the character. Returns true if accepted. */
    fun attempt(idx: Int): Boolean {
        if (solved) return false
        val w = rd.items[idx]
        if (w.id in given) return false
        val expected = rd.targets[given.size]
        selected = -1
        return if (w.id == expected.id) {
            given.add(w.id)
            val first = !wrongUsed
            if (first) host.correct(w) else host.correct()
            host.speaker.sayText(if (lang == Lang.EN) "Thank you!" else "شكراً!", lang)
            host.speaker.sayText(w.text(lang), lang, queue = true)
            if (given.size >= rd.targets.size) solved = true
            true
        } else {
            if (!wrongUsed) { wrongUsed = true; host.wrong() }
            misses++
            false
        }
    }

    Text(
        prompt,
        Modifier.fillMaxWidth().clickable { host.speaker.sayText(prompt, lang) },
        fontSize = 26.sp, fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center,
    )
    Spacer(Modifier.height(8.dp))

    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
        Box(
            Modifier
                .size(200.dp)
                .onGloballyPositioned { charBounds = it.boundsInRoot() }
                .graphicsLayer { translationX = shake.value }
                .scale(bounce.value)
                .clip(CircleShape)
                .background(Color(0xFFBDE3FF))
                .clickable { if (selected >= 0) attempt(selected) },
            contentAlignment = Alignment.Center,
        ) {
            Pic(rd.character, 130.dp)
            Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 2.dp)) { Pic("🤲", 56.dp) }
            if (given.isNotEmpty()) {
                Box(Modifier.align(Alignment.TopEnd).padding(top = 6.dp, end = 6.dp)) {
                    Pic(rd.items.first { it.id == given.last() }.emoji, 56.dp)
                }
            }
        }
        if (thanks) {
            Text(
                if (lang == Lang.EN) "Thank you!" else "شكراً!",
                Modifier.align(Alignment.TopCenter)
                    .clip(RoundedCornerShape(20.dp)).background(Color.White).padding(horizontal = 16.dp, vertical = 6.dp),
                fontSize = 24.sp, fontWeight = FontWeight.Black, color = Ink,
            )
        }
    }

    val rows = rd.items.indices.toList().chunked(if (rd.items.size == 3) 3 else 2)
    Column(
        Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        rows.forEach { row ->
            Row(
                Modifier.fillMaxWidth().height(104.dp).zIndex(if (dragIdx in row) 10f else 0f),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                row.forEach { idx ->
                    val w = rd.items[idx]
                    val isGiven = w.id in given
                    val glow = misses >= 2 && !solved && !isGiven && w.id == rd.targets[given.size].id
                    G10Item(
                        word = w,
                        hidden = isGiven,
                        enabled = !solved && !isGiven,
                        selected = selected == idx,
                        glow = glow,
                        onTap = { selected = if (selected == idx) -1 else idx },
                        onDragState = { d -> dragIdx = if (d) idx else if (dragIdx == idx) -1 else dragIdx },
                        onDrop = { center ->
                            val inside = charBounds?.contains(center) == true
                            if (inside) attempt(idx) else false
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun G10Item(
    word: Word,
    hidden: Boolean,
    enabled: Boolean,
    selected: Boolean,
    glow: Boolean,
    onTap: () -> Unit,
    onDragState: (Boolean) -> Unit,
    onDrop: (Offset) -> Boolean,
) {
    val ox = remember { Animatable(0f) }
    val oy = remember { Animatable(0f) }
    var bounds by remember { mutableStateOf<Rect?>(null) }
    var dragging by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val dropState = rememberUpdatedState(onDrop)
    val dragStateCb = rememberUpdatedState(onDragState)
    val enabledState = rememberUpdatedState(enabled)
    val sc by animateFloatAsState(
        if (dragging || selected || glow) 1.15f else 1f,
        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "g10sc",
    )

    Box(
        Modifier
            .size(96.dp)
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
            .background(if (selected || glow) Color(0xFFB8F2C0) else Color.White)
            .clickable(enabled = enabled && !hidden) { Sfx.tap(); onTap() }
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = {
                        if (enabledState.value) {
                            dragging = true
                            dragStateCb.value(true)
                        }
                    },
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
                        dragStateCb.value(false)
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
                        dragStateCb.value(false)
                        scope.launch {
                            launch { oy.animateTo(0f, spring()) }
                            ox.animateTo(0f, spring())
                        }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) { Pic(word.emoji, 76.dp) }
}
