package com.amq.bulbulwords.games

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amq.bulbulwords.data.Lang
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Word
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ───────────────────────── Shared "pick the match" round engine ─────────────────────────

@Composable
private fun <T> PickGame(
    host: GameHost,
    title: String,
    targets: List<T>,
    all: List<T>,
    optionCount: Int,
    nameOf: (T) -> String,
    showTarget: @Composable (T) -> Unit,
    showOption: @Composable (T) -> Unit,
) {
    var round by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    val target = targets[round]
    val options = remember(round) {
        (all.filter { it != target }.shuffled().take(optionCount - 1) + target).shuffled()
    }
    var wrongs by remember(round) { mutableStateOf(setOf<Int>()) }
    var done by remember(round) { mutableStateOf(false) }

    LaunchedEffect(round) {
        delay(350)
        host.speaker.sayText(nameOf(target))
    }
    LaunchedEffect(round, done) {
        if (done) {
            delay(1400)
            if (round + 1 >= targets.size) host.finish(score, targets.size) else round++
        }
    }

    GameFrame(host, title, round, score) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            val pulse = rememberInfiniteTransition(label = "pulse")
            val s by pulse.animateFloat(
                0.95f, 1.06f,
                infiniteRepeatable(tween(700), RepeatMode.Reverse), label = "s",
            )
            Box(
                Modifier.scale(s).clip(RoundedCornerShape(32.dp)).background(Color.White)
                    .clickable { Sfx.tap(); host.speaker.sayText(nameOf(target)) }
                    .padding(16.dp),
                contentAlignment = Alignment.Center,
            ) { showTarget(target) }
            Spacer(Modifier.height(20.dp))
            options.chunked(2).forEach { rowItems ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    rowItems.forEach { item ->
                        val idx = options.indexOf(item)
                        val isRight = item == target
                        val sc by animateFloatAsState(
                            if (done && isRight) 1.15f else 1f,
                            spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow), label = "sc",
                        )
                        Box(
                            Modifier.padding(vertical = 8.dp).scale(sc).size(140.dp)
                                .alpha(if (idx in wrongs) 0.3f else 1f)
                                .clip(RoundedCornerShape(28.dp))
                                .background(Color.White)
                                .border(
                                    if (done && isRight) 6.dp else 2.dp,
                                    if (done && isRight) Color(0xFF2EC4B6) else Color(0x22000000),
                                    RoundedCornerShape(28.dp),
                                )
                                .clickable {
                                    if (done || idx in wrongs) return@clickable
                                    if (isRight) {
                                        host.correct()
                                        if (wrongs.isEmpty()) score++
                                        done = true
                                    } else {
                                        host.wrong()
                                        wrongs = wrongs + idx
                                    }
                                },
                            contentAlignment = Alignment.Center,
                        ) { showOption(item) }
                    }
                }
            }
        }
    }
}

private fun darker(c: Color) = Color(c.red * 0.7f, c.green * 0.7f, c.blue * 0.7f, 1f)

// ───────────────────────── 1. Shapes ─────────────────────────

private enum class ShapeKind(val ar: String, val en: String, val color: Color) {
    CIRCLE("دائرة", "Circle", Color(0xFFE53935)),
    SQUARE("مربع", "Square", Color(0xFF1E88E5)),
    TRIANGLE("مثلث", "Triangle", Color(0xFF43A047)),
    STAR("نجمة", "Star", Color(0xFFFDD835)),
    HEART("قلب", "Heart", Color(0xFFEC407A)),
    RECTANGLE("مستطيل", "Rectangle", Color(0xFFFB8C00)),
    DIAMOND("معين", "Diamond", Color(0xFF8E24AA)),
    OVAL("بيضاوي", "Oval", Color(0xFF00ACC1)),
}

private fun shapePath(kind: ShapeKind, w: Float, h: Float): Path {
    val p = Path()
    when (kind) {
        ShapeKind.CIRCLE -> p.addOval(Rect(w * 0.06f, h * 0.06f, w * 0.94f, h * 0.94f))
        ShapeKind.SQUARE -> p.addRect(Rect(w * 0.12f, h * 0.12f, w * 0.88f, h * 0.88f))
        ShapeKind.RECTANGLE -> p.addRect(Rect(w * 0.04f, h * 0.27f, w * 0.96f, h * 0.73f))
        ShapeKind.OVAL -> p.addOval(Rect(w * 0.04f, h * 0.22f, w * 0.96f, h * 0.78f))
        ShapeKind.TRIANGLE -> {
            p.moveTo(w * 0.5f, h * 0.08f)
            p.lineTo(w * 0.95f, h * 0.88f)
            p.lineTo(w * 0.05f, h * 0.88f)
            p.close()
        }
        ShapeKind.DIAMOND -> {
            p.moveTo(w * 0.5f, h * 0.04f)
            p.lineTo(w * 0.88f, h * 0.5f)
            p.lineTo(w * 0.5f, h * 0.96f)
            p.lineTo(w * 0.12f, h * 0.5f)
            p.close()
        }
        ShapeKind.STAR -> {
            val cx = w / 2f
            val cy = h * 0.53f
            val outer = w * 0.48f
            val inner = outer * 0.42f
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) outer else inner
                val a = -PI / 2 + i * PI / 5
                val x = cx + (r * cos(a)).toFloat()
                val y = cy + (r * sin(a)).toFloat()
                if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
            }
            p.close()
        }
        ShapeKind.HEART -> {
            p.moveTo(w * 0.5f, h * 0.9f)
            p.cubicTo(w * 0.1f, h * 0.6f, w * 0.0f, h * 0.3f, w * 0.2f, h * 0.15f)
            p.cubicTo(w * 0.35f, h * 0.02f, w * 0.5f, h * 0.15f, w * 0.5f, h * 0.28f)
            p.cubicTo(w * 0.5f, h * 0.15f, w * 0.65f, h * 0.02f, w * 0.8f, h * 0.15f)
            p.cubicTo(w * 1.0f, h * 0.3f, w * 0.9f, h * 0.6f, w * 0.5f, h * 0.9f)
            p.close()
        }
    }
    return p
}

private fun DrawScope.drawKid(kind: ShapeKind) {
    val path = shapePath(kind, size.width, size.height)
    drawPath(path, kind.color)
    drawPath(path, darker(kind.color), style = Stroke(width = size.width * 0.035f))
    // little shine
    drawCircle(Color.White.copy(alpha = 0.45f), size.width * 0.05f, Offset(size.width * 0.36f, size.height * 0.4f))
}

@Composable
private fun ShapeCanvas(kind: ShapeKind, size: Dp) {
    Canvas(Modifier.size(size)) { drawKid(kind) }
}

@Composable
fun ShapesGame(host: GameHost) {
    val en = Settings.lang == Lang.EN
    val targets = remember { ShapeKind.values().toList().shuffled().take(GameHost.ROUNDS) }
    PickGame(
        host = host,
        title = "الأشكال",
        targets = targets,
        all = ShapeKind.values().toList(),
        optionCount = 4,
        nameOf = { if (Settings.lang == Lang.EN) it.en else it.ar },
        showTarget = { k ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ShapeCanvas(k, 170.dp)
                if (Settings.showText) {
                    Text(
                        if (en) k.en else k.ar, fontSize = 26.sp,
                        fontWeight = FontWeight.Black, color = Ink, textAlign = TextAlign.Center,
                    )
                }
            }
        },
        showOption = { k -> ShapeCanvas(k, 100.dp) },
    )
}

// ───────────────────────── 3. Colors ─────────────────────────

private class ColorItem(val ar: String, val en: String, val color: Color)

private val COLOR_ITEMS = listOf(
    ColorItem("أحمر", "Red", Color(0xFFE53935)),
    ColorItem("أزرق", "Blue", Color(0xFF1E88E5)),
    ColorItem("أخضر", "Green", Color(0xFF43A047)),
    ColorItem("أصفر", "Yellow", Color(0xFFFDD835)),
    ColorItem("برتقالي", "Orange", Color(0xFFFB8C00)),
    ColorItem("بنفسجي", "Purple", Color(0xFF8E24AA)),
    ColorItem("وردي", "Pink", Color(0xFFFF80AB)),
    ColorItem("بني", "Brown", Color(0xFF795548)),
    ColorItem("أسود", "Black", Color(0xFF212121)),
    ColorItem("أبيض", "White", Color(0xFFFFFFFF)),
)

@Composable
fun ColorsGame(host: GameHost) {
    val targets = remember { COLOR_ITEMS.shuffled().take(GameHost.ROUNDS) }
    PickGame(
        host = host,
        title = "الألوان",
        targets = targets,
        all = COLOR_ITEMS,
        optionCount = 4,
        nameOf = { if (Settings.lang == Lang.EN) it.en else it.ar },
        showTarget = { c ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔊", fontSize = 40.sp)
                Spacer(Modifier.size(12.dp))
                Text(
                    if (Settings.showText) (if (Settings.lang == Lang.EN) c.en else c.ar) else "?",
                    fontSize = 48.sp, fontWeight = FontWeight.Black, color = Ink,
                    textAlign = TextAlign.Center,
                )
            }
        },
        showOption = { c ->
            Box(
                Modifier.size(104.dp).clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.55f), c.color, darker(c.color)),
                            center = Offset(120f, 100f), radius = 260f,
                        )
                    )
                    .border(3.dp, Color(0x33000000), CircleShape)
            )
        },
    )
}

// ───────────────────────── 2. Puzzle ─────────────────────────

@Composable
private fun PieceView(emoji: String, idx: Int, size: Dp) {
    Canvas(Modifier.size(size)) {
        drawIntoCanvas { c ->
            val s = this.size.width
            val paint = android.graphics.Paint().apply {
                isAntiAlias = true
                textSize = s * 2f * 0.8f
                textAlign = android.graphics.Paint.Align.CENTER
            }
            val fm = paint.fontMetrics
            val baseline = s - (fm.ascent + fm.descent) / 2f
            val n = c.nativeCanvas
            n.save()
            n.clipRect(0f, 0f, s, s)
            n.translate(-(idx % 2) * s, -(idx / 2) * s)
            n.drawText(emoji, s, baseline, paint)
            n.restore()
        }
    }
}

@Composable
fun PuzzleGame(host: GameHost) {
    val total = 4
    val words = remember {
        fun plain(l: List<Word>) = l.filter { !it.emoji.startsWith("draw:") && !it.emoji.startsWith("txt:") }
        val a = plain(host.pool)
        (if (a.size >= total) a else plain(com.amq.bulbulwords.data.WordRepo.all)).shuffled().take(total)
    }
    var wrongHere by remember { mutableStateOf(false) }
    var p by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    val word = words[p]
    val placed = remember(p) { mutableStateListOf(false, false, false, false) }
    val tray = remember(p) { listOf(0, 1, 2, 3).shuffled() }
    var selected by remember(p) { mutableStateOf<Int?>(null) }
    val complete = placed.all { it }
    LaunchedEffect(p) { wrongHere = false }

    LaunchedEffect(p, complete) {
        if (complete) {
            host.speaker.sayText(word.text(Settings.lang))
            delay(2400)
            if (p + 1 >= total) host.finish(score, total) else p++
        }
    }

    val slot = 108.dp
    val pieceBg = Color(0xFFFFFBEA)
    val progress = p * 2 + placed.count { it } / 2

    GameFrame(host, "البازل", progress, score) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if (complete) (if (Settings.showText) word.text(Settings.lang) else "🎉") else "🧩",
                fontSize = 30.sp, fontWeight = FontWeight.Black, color = Ink,
            )
            Spacer(Modifier.height(10.dp))
            // board 2x2
            Column(
                Modifier.clip(RoundedCornerShape(24.dp)).background(Color.White).padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                for (r in 0..1) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        for (cc in 0..1) {
                            val i = r * 2 + cc
                            val filled = placed[i]
                            Box(
                                Modifier.size(slot).clip(RoundedCornerShape(12.dp))
                                    .background(if (filled) pieceBg else Color(0xFFE6E9F5))
                                    .border(
                                        2.dp,
                                        if (!filled && selected != null) host.accent else Color(0x22000000),
                                        RoundedCornerShape(12.dp),
                                    )
                                    .clickable {
                                        val sel = selected
                                        if (filled || sel == null || complete) return@clickable
                                        if (sel == i) {
                                            if (placed.count { it } == 3 && !wrongHere) host.correct(word) else host.correct()
                                            placed[i] = true
                                            selected = null
                                            if (placed.all { it }) score++
                                        } else {
                                            host.wrong()
                                            wrongHere = true
                                            selected = null
                                        }
                                    },
                                contentAlignment = Alignment.Center,
                            ) {
                                if (filled) PieceView(word.emoji, i, slot)
                                else Text("${i + 1}", fontSize = 28.sp, color = Color(0x44000000), fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
            // tray
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tray.forEach { id ->
                    val isSel = selected == id
                    val sc by animateFloatAsState(
                        if (isSel) 1.18f else 1f,
                        spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessMedium), label = "pc",
                    )
                    Box(Modifier.size(80.dp), contentAlignment = Alignment.Center) {
                        if (!placed[id]) {
                            Box(
                                Modifier.scale(sc).size(76.dp).clip(RoundedCornerShape(12.dp))
                                    .background(pieceBg)
                                    .border(
                                        if (isSel) 4.dp else 2.dp,
                                        if (isSel) host.accent else Color(0x33000000),
                                        RoundedCornerShape(12.dp),
                                    )
                                    .clickable { Sfx.tap(); selected = if (isSel) null else id },
                            ) { PieceView(word.emoji, id, 76.dp) }
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            // reference picture
            EmojiBubble(word.emoji, 72.dp, host.accent)
        }
    }
}
