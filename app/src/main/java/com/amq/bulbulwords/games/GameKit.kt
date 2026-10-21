package com.amq.bulbulwords.games

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.amq.bulbulwords.data.Settings
import com.amq.bulbulwords.data.Sfx
import com.amq.bulbulwords.data.Speaker
import com.amq.bulbulwords.data.Word

/**
 * Everything a game needs. Each game is a `@Composable fun XxxGame(host: GameHost)`.
 * A game plays [ROUNDS] rounds, calls [correct]/[wrong] per answer, then calls [finish] once.
 */
class GameHost(
    val pool: List<Word>,
    val speaker: Speaker,
    val accent: Color,
    val onBack: () -> Unit,
    private val onFinish: (score: Int, total: Int) -> Unit,
) {
    fun say(w: Word) = speaker.say(w)
    /** Last answer feedback shown by [FeedbackOverlay]: id changes on every answer; ok = correct or not. */
    var feedbackId by androidx.compose.runtime.mutableIntStateOf(0)
        private set
    var feedbackOk by androidx.compose.runtime.mutableStateOf(true)
        private set

    fun correct(w: Word? = null) {
        if (w != null) com.amq.bulbulwords.data.Progress.addPoint(w.id, 2)
        Sfx.success()
        feedbackOk = true; feedbackId++
    }

    fun wrong() {
        Sfx.wrong()
        if (!Settings.calm) { feedbackOk = false; feedbackId++ }
    }
    fun finish(score: Int, total: Int) = onFinish(score, total)

    /** [n] distinct random words from the pool (pool is guaranteed to have >= 6 words). */
    fun pick(n: Int): List<Word> = pool.shuffled().take(n)

    companion object { const val ROUNDS = 8 }
}

val KidBg = Brush.verticalGradient(listOf(Color(0xFFFFF4D6), Color(0xFFFFE0F0), Color(0xFFD9ECFF)))
val Ink = Color(0xFF3B2F63)

/** A big colorful circular "picture" made from an emoji — our illustration building block. */
@Composable
fun EmojiBubble(
    emoji: String,
    size: Dp = 120.dp,
    color: Color = Color(0xFFFFD166),
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Box(
        modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.radialGradient(listOf(Color.White, color.copy(alpha = 0.55f), color)))
            .let { if (onClick != null) it.clickable { Sfx.tap(); onClick() } else it },
        contentAlignment = Alignment.Center,
    ) { Pic(emoji, (size.value * 0.63f).dp) }
}

/** Rounded card with emoji + optional label; used for answer choices. */
@Composable
fun PictureCard(
    emoji: String,
    label: String? = null,
    color: Color = Color.White,
    size: Dp = 140.dp,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier
            .size(size)
            .clip(RoundedCornerShape(28.dp))
            .background(color)
            .clickable { Sfx.tap(); onClick() }
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Pic(emoji, (size.value * 0.6f).dp)
        if (label != null && Settings.showText) {
            Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink, textAlign = TextAlign.Center)
        }
    }
}

/** Standard frame: title bar with back arrow and round counter. Put the game body in [content]. */
@Composable
fun GameFrame(
    host: GameHost,
    title: String,
    round: Int,
    score: Int = 0,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit,
) {
    Column(
        Modifier.fillMaxSize().background(KidBg).statusBarsPadding().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(host.accent).clickable { host.onBack() },
                contentAlignment = Alignment.Center,
            ) { Text("➜", fontSize = 22.sp, color = Color.White) }
            Text(
                title, Modifier.weight(1f), textAlign = TextAlign.Center,
                fontSize = 22.sp, fontWeight = FontWeight.Black, color = Ink,
            )
            Text("⭐ $score", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(GameHost.ROUNDS) { i ->
                Box(
                    Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp))
                        .background(if (i < round) host.accent else Color(0x33000000))
                )
            }
        }
        Spacer(Modifier.height(16.dp))
        content()
    }
}

/** Adds the Arabic definite article to each word ("تفاحة خضراء" becomes "التفاحة الخضراء"). */
fun arDef(s: String): String =
    s.trim().split(" ").filter { it.isNotEmpty() }.joinToString(" ") { w -> if (w.startsWith("ال")) w else "ال$w" }

/**
 * Big, brief answer icon drawn over every game: a green tick with stars for a correct answer, a soft
 * orange cross for a wrong one. Lives in the host so all games show the same feedback.
 */
@Composable
fun FeedbackOverlay(host: GameHost) {
    var visible by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    androidx.compose.runtime.LaunchedEffect(host.feedbackId) {
        if (host.feedbackId == 0) return@LaunchedEffect
        visible = true
        kotlinx.coroutines.delay(if (host.feedbackOk) 850 else 600)
        visible = false
    }
    androidx.compose.animation.AnimatedVisibility(
        visible,
        enter = androidx.compose.animation.scaleIn(androidx.compose.animation.core.spring(androidx.compose.animation.core.Spring.DampingRatioMediumBouncy)) +
            androidx.compose.animation.fadeIn(),
        exit = androidx.compose.animation.fadeOut(),
    ) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            val ok = host.feedbackOk
            Box(
                Modifier.size(150.dp).clip(CircleShape)
                    .background(if (ok) Color(0xFF2EB872) else Color(0xFFFF9F1C)),
                contentAlignment = Alignment.Center,
            ) { Text(if (ok) "✔" else "✖", fontSize = 84.sp, color = Color.White, fontWeight = FontWeight.Black) }
            if (ok) Text("⭐✨", fontSize = 40.sp, modifier = Modifier.padding(bottom = 230.dp))
        }
    }
}
