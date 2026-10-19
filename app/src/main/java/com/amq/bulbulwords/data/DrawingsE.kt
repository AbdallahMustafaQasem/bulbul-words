package com.amq.bulbulwords.data

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import kotlin.math.cos
import kotlin.math.sin

private val SkinE = Color(0xFFFFCBA4)
private val SkinShadeE = Color(0xFFF2B48C)
private val HairE = Color(0xFF5B3A29)
private val InkE = Color(0xFF3B2A2A)
private val CheekE = Color(0x66FF6B81)
private val WhiteE = Color(0xFFFFFFFF)

private fun DrawScope.bgE(c: Color) {
    val s = size.minDimension
    drawRoundRect(c, Offset.Zero, Size(s, s), CornerRadius(s * 0.18f))
}

/** Cute round-headed kid. cx, cy, r are in pixels. mouth: 0 none, 1 smile, 2 open. */
private fun DrawScope.kidE(cx: Float, cy: Float, r: Float, mouth: Int) {
    drawCircle(HairE, r * 1.04f, Offset(cx, cy - r * 0.1f))
    drawCircle(SkinE, r, Offset(cx, cy + r * 0.04f))
    drawCircle(SkinE, r * 0.16f, Offset(cx - r * 1.0f, cy + r * 0.1f))
    drawCircle(SkinE, r * 0.16f, Offset(cx + r * 1.0f, cy + r * 0.1f))
    // fringe
    drawArc(
        HairE, 180f, 180f, true,
        Offset(cx - r * 0.98f, cy - r * 0.95f), Size(r * 1.96f, r * 1.0f),
    )
    drawCircle(InkE, r * 0.1f, Offset(cx - r * 0.38f, cy + r * 0.1f))
    drawCircle(InkE, r * 0.1f, Offset(cx + r * 0.38f, cy + r * 0.1f))
    drawCircle(WhiteE, r * 0.035f, Offset(cx - r * 0.35f, cy + r * 0.07f))
    drawCircle(WhiteE, r * 0.035f, Offset(cx + r * 0.41f, cy + r * 0.07f))
    drawCircle(CheekE, r * 0.16f, Offset(cx - r * 0.62f, cy + r * 0.38f))
    drawCircle(CheekE, r * 0.16f, Offset(cx + r * 0.62f, cy + r * 0.38f))
    when (mouth) {
        1 -> drawArc(
            InkE, 20f, 140f, false,
            Offset(cx - r * 0.3f, cy + r * 0.2f), Size(r * 0.6f, r * 0.5f),
            style = Stroke(r * 0.07f, cap = StrokeCap.Round),
        )
        2 -> drawOval(Color(0xFFD64550), Offset(cx - r * 0.25f, cy + r * 0.38f), Size(r * 0.5f, r * 0.4f))
    }
}

private fun DrawScope.starE(cx: Float, cy: Float, r: Float, color: Color) {
    val p = Path()
    for (i in 0 until 10) {
        val rad = if (i % 2 == 0) r else r * 0.45f
        val a = Math.PI * 2 * i / 10 - Math.PI / 2
        val x = cx + (rad * cos(a)).toFloat()
        val y = cy + (rad * sin(a)).toFloat()
        if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    p.close()
    drawPath(p, color)
}

private fun DrawScope.bubbleE(cx: Float, cy: Float, r: Float) {
    drawCircle(Color(0xCCFFFFFF), r, Offset(cx, cy))
    drawCircle(Color(0xFF8ED0F5), r, Offset(cx, cy), style = Stroke(r * 0.15f))
    drawCircle(WhiteE, r * 0.25f, Offset(cx - r * 0.35f, cy - r * 0.35f))
}

private val brushTeethE: DrawFn = {
    val s = size.minDimension
    bgE(Color(0xFFD9F0FF))
    // shirt
    drawRoundRect(
        Color(0xFFFF8FAB), Offset(s * 0.16f, s * 0.68f), Size(s * 0.52f, s * 0.36f),
        CornerRadius(s * 0.12f),
    )
    kidE(s * 0.42f, s * 0.38f, s * 0.26f, 0)
    // open mouth with teeth
    drawRoundRect(
        Color(0xFFD64550), Offset(s * 0.27f, s * 0.46f), Size(s * 0.30f, s * 0.14f),
        CornerRadius(s * 0.06f),
    )
    drawRoundRect(
        WhiteE, Offset(s * 0.29f, s * 0.46f), Size(s * 0.26f, s * 0.05f),
        CornerRadius(s * 0.02f),
    )
    drawRoundRect(
        WhiteE, Offset(s * 0.30f, s * 0.555f), Size(s * 0.24f, s * 0.04f),
        CornerRadius(s * 0.02f),
    )
    // toothbrush
    drawLine(
        Color(0xFFFFC53D), Offset(s * 0.93f, s * 0.88f), Offset(s * 0.58f, s * 0.57f),
        strokeWidth = s * 0.055f, cap = StrokeCap.Round,
    )
    drawLine(
        Color(0xFF3FA7FF), Offset(s * 0.58f, s * 0.57f), Offset(s * 0.46f, s * 0.53f),
        strokeWidth = s * 0.07f, cap = StrokeCap.Round,
    )
    drawLine(
        WhiteE, Offset(s * 0.56f, s * 0.555f), Offset(s * 0.47f, s * 0.525f),
        strokeWidth = s * 0.03f, cap = StrokeCap.Round,
    )
    // foam and sparkles
    bubbleE(s * 0.22f, s * 0.60f, s * 0.04f)
    bubbleE(s * 0.60f, s * 0.46f, s * 0.035f)
    bubbleE(s * 0.78f, s * 0.30f, s * 0.05f)
    bubbleE(s * 0.86f, s * 0.46f, s * 0.03f)
    starE(s * 0.80f, s * 0.14f, s * 0.07f, Color(0xFFFFD166))
    starE(s * 0.92f, s * 0.24f, s * 0.04f, Color(0xFFFFD166))
}

private val washHandsE: DrawFn = {
    val s = size.minDimension
    bgE(Color(0xFFE3F6FF))
    // tap
    drawRoundRect(
        Color(0xFF9AA5B1), Offset(s * 0.28f, s * 0.06f), Size(s * 0.42f, s * 0.08f),
        CornerRadius(s * 0.04f),
    )
    drawRoundRect(
        Color(0xFF9AA5B1), Offset(s * 0.28f, s * 0.06f), Size(s * 0.09f, s * 0.20f),
        CornerRadius(s * 0.04f),
    )
    drawRoundRect(
        Color(0xFF6C7A89), Offset(s * 0.26f, s * 0.24f), Size(s * 0.13f, s * 0.04f),
        CornerRadius(s * 0.02f),
    )
    drawRoundRect(
        Color(0xFFFF6B6B), Offset(s * 0.44f, s * 0.02f), Size(s * 0.12f, s * 0.05f),
        CornerRadius(s * 0.025f),
    )
    // water
    val w = Color(0xFF4CC3FF)
    for (dx in listOf(0.30f, 0.325f, 0.35f)) {
        drawLine(w, Offset(s * dx, s * 0.30f), Offset(s * dx, s * 0.50f), s * 0.014f, StrokeCap.Round)
    }
    drawCircle(w, s * 0.018f, Offset(s * 0.28f, s * 0.56f))
    drawCircle(w, s * 0.018f, Offset(s * 0.38f, s * 0.58f))
    // kid on the right
    drawRoundRect(
        Color(0xFFFFB84D), Offset(s * 0.60f, s * 0.58f), Size(s * 0.34f, s * 0.30f),
        CornerRadius(s * 0.1f),
    )
    kidE(s * 0.77f, s * 0.42f, s * 0.15f, 1)
    // basin
    drawRoundRect(
        Color(0xFFB8C4D0), Offset(s * 0.04f, s * 0.78f), Size(s * 0.92f, s * 0.2f),
        CornerRadius(s * 0.08f),
    )
    drawRoundRect(
        Color(0xFF7FD3FF), Offset(s * 0.10f, s * 0.80f), Size(s * 0.80f, s * 0.07f),
        CornerRadius(s * 0.035f),
    )
    // hands
    drawOval(SkinE, Offset(s * 0.10f, s * 0.60f), Size(s * 0.34f, s * 0.20f))
    drawOval(SkinShadeE, Offset(s * 0.12f, s * 0.70f), Size(s * 0.30f, s * 0.10f))
    drawOval(SkinE, Offset(s * 0.18f, s * 0.54f), Size(s * 0.30f, s * 0.18f))
    // soap foam
    bubbleE(s * 0.20f, s * 0.58f, s * 0.05f)
    bubbleE(s * 0.31f, s * 0.54f, s * 0.06f)
    bubbleE(s * 0.40f, s * 0.62f, s * 0.045f)
    bubbleE(s * 0.50f, s * 0.50f, s * 0.035f)
    bubbleE(s * 0.52f, s * 0.70f, s * 0.03f)
    starE(s * 0.90f, s * 0.12f, s * 0.05f, Color(0xFFFFD166))
}

private val wearClothesE: DrawFn = {
    val s = size.minDimension
    bgE(Color(0xFFFFF1D6))
    // trousers
    val pants = Color(0xFF3A4A8F)
    drawRoundRect(pants, Offset(s * 0.31f, s * 0.78f), Size(s * 0.17f, s * 0.20f), CornerRadius(s * 0.04f))
    drawRoundRect(pants, Offset(s * 0.52f, s * 0.78f), Size(s * 0.17f, s * 0.20f), CornerRadius(s * 0.04f))
    // sleeves + hands
    val shirt = Color(0xFF4D96FF)
    drawLine(shirt, Offset(s * 0.33f, s * 0.54f), Offset(s * 0.15f, s * 0.42f), s * 0.12f, StrokeCap.Round)
    drawLine(shirt, Offset(s * 0.67f, s * 0.54f), Offset(s * 0.85f, s * 0.42f), s * 0.12f, StrokeCap.Round)
    drawCircle(SkinE, s * 0.055f, Offset(s * 0.12f, s * 0.38f))
    drawCircle(SkinE, s * 0.055f, Offset(s * 0.88f, s * 0.38f))
    // shirt body
    drawRoundRect(shirt, Offset(s * 0.29f, s * 0.45f), Size(s * 0.42f, s * 0.40f), CornerRadius(s * 0.09f))
    drawRoundRect(
        Color(0xFF3A7FE6), Offset(s * 0.29f, s * 0.78f), Size(s * 0.42f, s * 0.07f),
        CornerRadius(s * 0.035f),
    )
    kidE(s * 0.50f, s * 0.27f, s * 0.17f, 1)
    // collar
    drawOval(WhiteE, Offset(s * 0.41f, s * 0.41f), Size(s * 0.18f, s * 0.09f))
    drawOval(SkinE, Offset(s * 0.44f, s * 0.415f), Size(s * 0.12f, s * 0.06f))
    // star + buttons
    starE(s * 0.50f, s * 0.65f, s * 0.08f, Color(0xFFFFD166))
    // hanging hanger in corner
    drawArc(
        Color(0xFF9AA5B1), 200f, 220f, false, Offset(s * 0.06f, s * 0.08f), Size(s * 0.12f, s * 0.12f),
        style = Stroke(s * 0.018f, cap = StrokeCap.Round),
    )
    drawLine(Color(0xFF9AA5B1), Offset(s * 0.12f, s * 0.20f), Offset(s * 0.04f, s * 0.27f), s * 0.018f, StrokeCap.Round)
    drawLine(Color(0xFF9AA5B1), Offset(s * 0.12f, s * 0.20f), Offset(s * 0.20f, s * 0.27f), s * 0.018f, StrokeCap.Round)
    drawLine(Color(0xFF9AA5B1), Offset(s * 0.04f, s * 0.27f), Offset(s * 0.20f, s * 0.27f), s * 0.018f, StrokeCap.Round)
    starE(s * 0.88f, s * 0.14f, s * 0.05f, Color(0xFFFF8FAB))
}

private val takeBathE: DrawFn = {
    val s = size.minDimension
    bgE(Color(0xFFD9F5F2))
    // shower drops from top-right
    drawRoundRect(
        Color(0xFF9AA5B1), Offset(s * 0.70f, s * 0.05f), Size(s * 0.22f, s * 0.07f),
        CornerRadius(s * 0.035f),
    )
    for (dx in listOf(0.73f, 0.80f, 0.87f)) {
        drawLine(Color(0xFF4CC3FF), Offset(s * dx, s * 0.16f), Offset(s * dx, s * 0.30f), s * 0.014f, StrokeCap.Round)
    }
    // kid
    drawRoundRect(
        Color(0xFFFFCBA4), Offset(s * 0.36f, s * 0.52f), Size(s * 0.28f, s * 0.2f),
        CornerRadius(s * 0.08f),
    )
    kidE(s * 0.50f, s * 0.38f, s * 0.19f, 1)
    // foam on head
    bubbleE(s * 0.44f, s * 0.17f, s * 0.05f)
    bubbleE(s * 0.54f, s * 0.14f, s * 0.06f)
    bubbleE(s * 0.60f, s * 0.20f, s * 0.04f)
    // tub
    drawRoundRect(
        WhiteE, Offset(s * 0.08f, s * 0.58f), Size(s * 0.84f, s * 0.32f),
        CornerRadius(s * 0.12f),
    )
    drawRoundRect(
        Color(0xFF7FD3FF), Offset(s * 0.12f, s * 0.60f), Size(s * 0.76f, s * 0.08f),
        CornerRadius(s * 0.04f),
    )
    drawRoundRect(
        Color(0xFFFF8FAB), Offset(s * 0.08f, s * 0.74f), Size(s * 0.84f, s * 0.06f),
        CornerRadius(s * 0.03f),
    )
    drawCircle(Color(0xFF9AA5B1), s * 0.04f, Offset(s * 0.22f, s * 0.94f))
    drawCircle(Color(0xFF9AA5B1), s * 0.04f, Offset(s * 0.78f, s * 0.94f))
    // foam on water
    bubbleE(s * 0.22f, s * 0.58f, s * 0.06f)
    bubbleE(s * 0.30f, s * 0.54f, s * 0.045f)
    bubbleE(s * 0.72f, s * 0.57f, s * 0.06f)
    bubbleE(s * 0.80f, s * 0.60f, s * 0.04f)
    // rubber duck
    drawOval(Color(0xFFFFD23F), Offset(s * 0.68f, s * 0.46f), Size(s * 0.20f, s * 0.12f))
    drawCircle(Color(0xFFFFD23F), s * 0.055f, Offset(s * 0.85f, s * 0.43f))
    drawOval(Color(0xFFFF8A00), Offset(s * 0.88f, s * 0.43f), Size(s * 0.06f, s * 0.035f))
    drawCircle(InkE, s * 0.01f, Offset(s * 0.86f, s * 0.415f))
    bubbleE(s * 0.16f, s * 0.32f, s * 0.035f)
    bubbleE(s * 0.10f, s * 0.44f, s * 0.025f)
}

val DRAWINGS_E: Map<String, DrawFn> = mapOf(
    "brush_teeth" to brushTeethE,
    "wash_hands" to washHandsE,
    "wear_clothes" to wearClothesE,
    "take_bath" to takeBathE,
)
