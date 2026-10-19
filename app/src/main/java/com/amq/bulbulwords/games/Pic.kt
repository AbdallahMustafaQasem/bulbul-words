package com.amq.bulbulwords.games

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.unit.Dp
import com.amq.bulbulwords.data.ALL_DRAWINGS

/**
 * Renders a word's picture. [pic] is `Word.emoji`, which is one of:
 *  - a normal emoji ("🍎"),
 *  - "txt:X" a big letter/number drawn as bold text ("txt:أ"),
 *  - "draw:key" a hand-drawn vector illustration registered in ALL_DRAWINGS.
 * Always use this instead of Text(word.emoji) so every kind of picture works everywhere.
 */
@Composable
fun Pic(pic: String, size: Dp, modifier: Modifier = Modifier, textColor: Color = Ink) {
    when {
        pic.startsWith("draw:") -> {
            val fn = ALL_DRAWINGS[pic.removePrefix("draw:")]
            Canvas(modifier.size(size)) { fn?.invoke(this) }
        }
        pic.startsWith("txt:") -> Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Text(
                pic.removePrefix("txt:"),
                modifier = Modifier.wrapContentSize(unbounded = true),
                fontSize = with(LocalDensity.current) { (size * 0.6f).toSp() },
                fontWeight = FontWeight.Black, color = textColor,
                softWrap = false, overflow = TextOverflow.Visible,
            )
        }
        else -> Box(modifier.size(size), contentAlignment = Alignment.Center) {
            Text(pic, fontSize = with(LocalDensity.current) { (size * 0.82f).toSp() })
        }
    }
}
