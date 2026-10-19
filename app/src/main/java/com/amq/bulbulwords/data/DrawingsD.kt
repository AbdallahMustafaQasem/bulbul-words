package com.amq.bulbulwords.data

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope

// ---- helpers (all coordinates are fractions of the square side) ----

private fun DrawScope.rr(x: Float, y: Float, w: Float, h: Float, r: Float, c: Color) {
    val s = size.minDimension
    drawRoundRect(c, Offset(x * s, y * s), Size(w * s, h * s), CornerRadius(r * s, r * s))
}

private fun DrawScope.ci(cx: Float, cy: Float, r: Float, c: Color) {
    val s = size.minDimension
    drawCircle(c, r * s, Offset(cx * s, cy * s))
}

private fun DrawScope.ov(x: Float, y: Float, w: Float, h: Float, c: Color) {
    val s = size.minDimension
    drawOval(c, Offset(x * s, y * s), Size(w * s, h * s))
}

private fun DrawScope.ln(x1: Float, y1: Float, x2: Float, y2: Float, w: Float, c: Color) {
    val s = size.minDimension
    drawLine(c, Offset(x1 * s, y1 * s), Offset(x2 * s, y2 * s), strokeWidth = w * s, cap = StrokeCap.Round)
}

private fun DrawScope.poly(c: Color, vararg p: Float) {
    val s = size.minDimension
    val path = Path()
    path.moveTo(p[0] * s, p[1] * s)
    var i = 2
    while (i < p.size) {
        path.lineTo(p[i] * s, p[i + 1] * s)
        i += 2
    }
    path.close()
    drawPath(path, c)
}

private fun d(f: DrawFn): DrawFn = f

private val SKIN = Color(0xFFFFCBA4)
private val SKIN_D = Color(0xFFF0A878)
private val WOOD = Color(0xFFC98A4B)
private val WOOD_D = Color(0xFF9C6532)
private val WOOD_L = Color(0xFFE2AA6E)

private fun DrawScope.person(shirt: Color, shirtD: Color) {
    // shoulders / body
    rr(0.2f, 0.58f, 0.6f, 0.36f, 0.18f, shirt)
    rr(0.2f, 0.78f, 0.6f, 0.16f, 0.08f, shirtD)
    // neck
    rr(0.43f, 0.5f, 0.14f, 0.14f, 0.04f, SKIN_D)
    // head
    ci(0.5f, 0.34f, 0.2f, SKIN)
    ci(0.3f, 0.36f, 0.04f, SKIN)
    ci(0.7f, 0.36f, 0.04f, SKIN)
}

private fun DrawScope.face() {
    ci(0.43f, 0.34f, 0.025f, Color(0xFF3B2A20))
    ci(0.57f, 0.34f, 0.025f, Color(0xFF3B2A20))
    ci(0.38f, 0.41f, 0.03f, Color(0x55FF7B7B))
    ci(0.62f, 0.41f, 0.03f, Color(0x55FF7B7B))
    ln(0.45f, 0.43f, 0.55f, 0.43f, 0.015f, Color(0xFFB5503C))
}

val DRAWINGS_D: Map<String, DrawFn> = mapOf(

    "uncle" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFDCEBFF))
        person(Color(0xFF3F7FD6), Color(0xFF2F63AB))
        // collar
        poly(Color.White, 0.42f, 0.6f, 0.58f, 0.6f, 0.5f, 0.72f)
        // hair
        ov(0.3f, 0.12f, 0.4f, 0.2f, Color(0xFF3B2A20))
        face()
        // mustache
        rr(0.4f, 0.385f, 0.2f, 0.04f, 0.02f, Color(0xFF3B2A20))
        ln(0.45f, 0.445f, 0.55f, 0.445f, 0.015f, Color(0xFFB5503C))
    },

    "aunt" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFFFE3EE))
        // scarf behind
        ci(0.5f, 0.36f, 0.27f, Color(0xFFE0457B))
        person(Color(0xFFB04FC9), Color(0xFF8B3AA1))
        // scarf over head
        ci(0.5f, 0.34f, 0.24f, Color(0xFFE0457B))
        ci(0.5f, 0.38f, 0.17f, SKIN)
        ov(0.28f, 0.54f, 0.44f, 0.14f, Color(0xFFC63468))
        ci(0.45f, 0.37f, 0.022f, Color(0xFF3B2A20))
        ci(0.55f, 0.37f, 0.022f, Color(0xFF3B2A20))
        ci(0.4f, 0.43f, 0.028f, Color(0x55FF7B7B))
        ci(0.6f, 0.43f, 0.028f, Color(0x55FF7B7B))
        ln(0.46f, 0.46f, 0.54f, 0.46f, 0.014f, Color(0xFFB5503C))
        // scarf dots
        ci(0.36f, 0.2f, 0.018f, Color(0xFFFFD1E1))
        ci(0.5f, 0.15f, 0.018f, Color(0xFFFFD1E1))
        ci(0.64f, 0.2f, 0.018f, Color(0xFFFFD1E1))
    },

    "kitchen" to d {
        rr(0f, 0f, 1f, 1f, 0.06f, Color(0xFFFFF1C9))
        // tiles floor
        rr(0f, 0.82f, 1f, 0.18f, 0.04f, Color(0xFFE8C38A))
        // window
        rr(0.58f, 0.1f, 0.3f, 0.28f, 0.03f, Color(0xFF8ED1FC))
        ln(0.73f, 0.1f, 0.73f, 0.38f, 0.015f, Color.White)
        ln(0.58f, 0.24f, 0.88f, 0.24f, 0.015f, Color.White)
        // upper cabinet
        rr(0.08f, 0.1f, 0.4f, 0.24f, 0.03f, Color(0xFFE8734A))
        ln(0.28f, 0.1f, 0.28f, 0.34f, 0.01f, Color(0xFFC85A36))
        ci(0.25f, 0.22f, 0.014f, Color(0xFFFFE08A))
        ci(0.31f, 0.22f, 0.014f, Color(0xFFFFE08A))
        // counter
        rr(0.05f, 0.5f, 0.9f, 0.34f, 0.03f, Color(0xFFE8734A))
        rr(0.03f, 0.46f, 0.94f, 0.07f, 0.03f, Color(0xFF7A8CA3))
        ln(0.37f, 0.56f, 0.37f, 0.84f, 0.01f, Color(0xFFC85A36))
        ln(0.65f, 0.56f, 0.65f, 0.84f, 0.01f, Color(0xFFC85A36))
        ci(0.34f, 0.66f, 0.014f, Color(0xFFFFE08A))
        ci(0.4f, 0.66f, 0.014f, Color(0xFFFFE08A))
        // pot
        rr(0.7f, 0.32f, 0.2f, 0.14f, 0.03f, Color(0xFF5C6B7A))
        rr(0.68f, 0.3f, 0.24f, 0.04f, 0.02f, Color(0xFF8396A8))
        ci(0.8f, 0.28f, 0.02f, Color(0xFF3B4650))
        // steam
        ln(0.76f, 0.24f, 0.78f, 0.16f, 0.012f, Color(0x99FFFFFF))
        ln(0.84f, 0.24f, 0.86f, 0.16f, 0.012f, Color(0x99FFFFFF))
        // fruit bowl
        ov(0.12f, 0.4f, 0.2f, 0.07f, Color(0xFF4FB477))
        ci(0.18f, 0.39f, 0.03f, Color(0xFFFF5A5A))
        ci(0.25f, 0.39f, 0.03f, Color(0xFFFFB627))
    },

    "bedroom" to d {
        rr(0f, 0f, 1f, 1f, 0.06f, Color(0xFFD8E4FF))
        rr(0f, 0.8f, 1f, 0.2f, 0.04f, Color(0xFFC9A27A))
        // window with moon
        rr(0.62f, 0.1f, 0.28f, 0.34f, 0.04f, Color(0xFF24306E))
        ci(0.72f, 0.22f, 0.05f, Color(0xFFFFE680))
        ci(0.74f, 0.2f, 0.045f, Color(0xFF24306E))
        ci(0.82f, 0.34f, 0.012f, Color.White)
        ci(0.68f, 0.35f, 0.012f, Color.White)
        // bed frame
        rr(0.08f, 0.38f, 0.1f, 0.5f, 0.03f, WOOD_D)
        rr(0.08f, 0.62f, 0.8f, 0.24f, 0.04f, WOOD)
        rr(0.84f, 0.56f, 0.06f, 0.32f, 0.02f, WOOD_D)
        // mattress + blanket
        rr(0.16f, 0.5f, 0.68f, 0.18f, 0.06f, Color.White)
        rr(0.34f, 0.52f, 0.5f, 0.2f, 0.06f, Color(0xFFFF7BAC))
        rr(0.34f, 0.52f, 0.5f, 0.05f, 0.025f, Color(0xFFFFA6C7))
        // pillow
        rr(0.18f, 0.43f, 0.2f, 0.12f, 0.05f, Color(0xFFFFF3C4))
    },

    "bathroom" to d {
        rr(0f, 0f, 1f, 1f, 0.06f, Color(0xFFD4F4F2))
        // tile lines
        for (i in 1..4) {
            ln(0f, i * 0.16f, 1f, i * 0.16f, 0.006f, Color(0xFFB4E4E0))
            ln(i * 0.2f, 0f, i * 0.2f, 0.7f, 0.006f, Color(0xFFB4E4E0))
        }
        rr(0f, 0.86f, 1f, 0.14f, 0.04f, Color(0xFFA9D7D3))
        // tub body
        rr(0.08f, 0.5f, 0.84f, 0.32f, 0.12f, Color.White)
        rr(0.08f, 0.7f, 0.84f, 0.12f, 0.06f, Color(0xFFD9E2EA))
        rr(0.05f, 0.46f, 0.9f, 0.08f, 0.04f, Color(0xFFF2F6FA))
        // feet
        rr(0.16f, 0.82f, 0.08f, 0.06f, 0.02f, Color(0xFF9AA7B4))
        rr(0.76f, 0.82f, 0.08f, 0.06f, 0.02f, Color(0xFF9AA7B4))
        // bubbles
        ov(0.2f, 0.4f, 0.3f, 0.12f, Color(0xFFBDE6FF))
        ci(0.25f, 0.4f, 0.05f, Color.White)
        ci(0.34f, 0.37f, 0.06f, Color(0xFFEAF7FF))
        ci(0.44f, 0.41f, 0.045f, Color.White)
        // duck
        ci(0.7f, 0.46f, 0.07f, Color(0xFFFFD84A))
        ci(0.76f, 0.38f, 0.045f, Color(0xFFFFD84A))
        poly(Color(0xFFFF9A3C), 0.8f, 0.37f, 0.86f, 0.39f, 0.8f, 0.41f)
        ci(0.77f, 0.365f, 0.01f, Color(0xFF3B2A20))
        // tap
        rr(0.2f, 0.1f, 0.05f, 0.2f, 0.02f, Color(0xFF9AA7B4))
        rr(0.2f, 0.1f, 0.2f, 0.05f, 0.025f, Color(0xFFB6C2CE))
        rr(0.36f, 0.14f, 0.05f, 0.06f, 0.02f, Color(0xFF9AA7B4))
        ci(0.225f, 0.08f, 0.03f, Color(0xFFE8734A))
        // drops
        ci(0.385f, 0.26f, 0.014f, Color(0xFF5BB8F0))
        ci(0.385f, 0.33f, 0.014f, Color(0xFF5BB8F0))
    },

    "table" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFFFF0D6))
        // far legs
        rr(0.24f, 0.4f, 0.06f, 0.4f, 0.02f, WOOD_D)
        rr(0.7f, 0.4f, 0.06f, 0.4f, 0.02f, WOOD_D)
        // front legs
        rr(0.13f, 0.4f, 0.08f, 0.5f, 0.025f, WOOD)
        rr(0.79f, 0.4f, 0.08f, 0.5f, 0.025f, WOOD)
        // apron + top
        rr(0.14f, 0.4f, 0.72f, 0.08f, 0.02f, WOOD_D)
        rr(0.06f, 0.28f, 0.88f, 0.16f, 0.05f, WOOD_L)
        rr(0.06f, 0.28f, 0.88f, 0.06f, 0.03f, Color(0xFFF2C894))
        // vase with flower
        rr(0.44f, 0.12f, 0.12f, 0.16f, 0.04f, Color(0xFF5BB8F0))
        ln(0.5f, 0.12f, 0.5f, 0.06f, 0.012f, Color(0xFF4FB477))
        ci(0.5f, 0.05f, 0.035f, Color(0xFFFF5A8A))
    },

    "fridge" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFE6F3FF))
        // body
        rr(0.26f, 0.06f, 0.48f, 0.88f, 0.06f, Color(0xFF8FC7F2))
        // freezer door
        rr(0.28f, 0.08f, 0.44f, 0.26f, 0.05f, Color(0xFFBFE0FA))
        // fridge door
        rr(0.28f, 0.37f, 0.44f, 0.55f, 0.05f, Color(0xFFD4EBFC))
        // shine
        rr(0.31f, 0.42f, 0.04f, 0.3f, 0.02f, Color(0xAAFFFFFF))
        // handles
        rr(0.64f, 0.16f, 0.03f, 0.14f, 0.015f, Color(0xFF5E7FA0))
        rr(0.64f, 0.42f, 0.03f, 0.22f, 0.015f, Color(0xFF5E7FA0))
        // magnets
        ci(0.45f, 0.5f, 0.03f, Color(0xFFFF5A5A))
        ci(0.52f, 0.58f, 0.03f, Color(0xFFFFC93C))
        // feet
        rr(0.3f, 0.93f, 0.07f, 0.04f, 0.015f, Color(0xFF5E7FA0))
        rr(0.63f, 0.93f, 0.07f, 0.04f, 0.015f, Color(0xFF5E7FA0))
    },

    "wardrobe" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFEAF5E0))
        // top cornice
        rr(0.12f, 0.06f, 0.76f, 0.08f, 0.03f, WOOD_D)
        // body
        rr(0.14f, 0.12f, 0.72f, 0.76f, 0.03f, WOOD)
        // doors
        rr(0.18f, 0.17f, 0.31f, 0.66f, 0.025f, WOOD_L)
        rr(0.51f, 0.17f, 0.31f, 0.66f, 0.025f, WOOD_L)
        // panels
        rr(0.22f, 0.22f, 0.23f, 0.24f, 0.02f, Color(0xFFD7A064))
        rr(0.55f, 0.22f, 0.23f, 0.24f, 0.02f, Color(0xFFD7A064))
        rr(0.22f, 0.52f, 0.23f, 0.25f, 0.02f, Color(0xFFD7A064))
        rr(0.55f, 0.52f, 0.23f, 0.25f, 0.02f, Color(0xFFD7A064))
        // knobs
        ci(0.45f, 0.5f, 0.022f, Color(0xFFFFE08A))
        ci(0.55f, 0.5f, 0.022f, Color(0xFFFFE08A))
        // feet
        rr(0.18f, 0.88f, 0.08f, 0.07f, 0.02f, WOOD_D)
        rr(0.74f, 0.88f, 0.08f, 0.07f, 0.02f, WOOD_D)
    },

    "carpet" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFFFF3DC))
        // fringe
        for (i in 0..7) {
            val y = 0.26f + i * 0.066f
            ln(0.04f, y, 0.12f, y, 0.016f, Color(0xFFF7E3B0))
            ln(0.88f, y, 0.96f, y, 0.016f, Color(0xFFF7E3B0))
        }
        // rug
        rr(0.1f, 0.2f, 0.8f, 0.6f, 0.05f, Color(0xFFD64550))
        rr(0.15f, 0.26f, 0.7f, 0.48f, 0.04f, Color(0xFFFFE9B8))
        rr(0.19f, 0.3f, 0.62f, 0.4f, 0.03f, Color(0xFF3FA7A0))
        // pattern
        poly(Color(0xFFFFC93C), 0.5f, 0.34f, 0.64f, 0.5f, 0.5f, 0.66f, 0.36f, 0.5f)
        poly(Color(0xFFD64550), 0.5f, 0.41f, 0.57f, 0.5f, 0.5f, 0.59f, 0.43f, 0.5f)
        ci(0.27f, 0.38f, 0.02f, Color(0xFFFFE9B8))
        ci(0.73f, 0.38f, 0.02f, Color(0xFFFFE9B8))
        ci(0.27f, 0.62f, 0.02f, Color(0xFFFFE9B8))
        ci(0.73f, 0.62f, 0.02f, Color(0xFFFFE9B8))
    },

    "pillow" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFE9F4FF))
        // shadow
        ov(0.12f, 0.74f, 0.76f, 0.1f, Color(0x22000000))
        // body
        rr(0.1f, 0.2f, 0.8f, 0.58f, 0.24f, Color(0xFFFF8FB7))
        rr(0.16f, 0.26f, 0.68f, 0.46f, 0.2f, Color(0xFFFFB3CE))
        // shine
        rr(0.22f, 0.3f, 0.3f, 0.06f, 0.03f, Color(0x88FFFFFF))
        // stitching
        ln(0.5f, 0.3f, 0.5f, 0.68f, 0.008f, Color(0xFFE5628F))
        ci(0.5f, 0.49f, 0.03f, Color(0xFFE5628F))
        // corner tufts
        ci(0.13f, 0.23f, 0.03f, Color(0xFFE5628F))
        ci(0.87f, 0.23f, 0.03f, Color(0xFFE5628F))
        ci(0.13f, 0.75f, 0.03f, Color(0xFFE5628F))
        ci(0.87f, 0.75f, 0.03f, Color(0xFFE5628F))
    },

    "blanket" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFFFF2E0))
        // folded stack
        rr(0.1f, 0.58f, 0.8f, 0.2f, 0.06f, Color(0xFF3FA7A0))
        rr(0.14f, 0.4f, 0.72f, 0.2f, 0.06f, Color(0xFFFF8A5B))
        rr(0.18f, 0.22f, 0.64f, 0.2f, 0.06f, Color(0xFF7B6CF0))
        // stripes
        rr(0.1f, 0.64f, 0.8f, 0.03f, 0.01f, Color(0xFFFFE08A))
        rr(0.14f, 0.46f, 0.72f, 0.03f, 0.01f, Color(0xFFFFE08A))
        rr(0.18f, 0.28f, 0.64f, 0.03f, 0.01f, Color(0xFFFFE08A))
        // folded edges shading
        rr(0.1f, 0.72f, 0.8f, 0.06f, 0.03f, Color(0x22000000))
        rr(0.14f, 0.54f, 0.72f, 0.06f, 0.03f, Color(0x22000000))
        rr(0.18f, 0.36f, 0.64f, 0.06f, 0.03f, Color(0x22000000))
        // fringe
        for (i in 0..5) {
            val y = 0.24f + i * 0.1f
            if (i < 5) {
                ln(0.86f, y + 0.02f, 0.92f, y + 0.02f, 0.012f, Color(0xFFF7E3B0))
            }
        }
    },

    "oven" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFFFEBD6))
        // body
        rr(0.12f, 0.14f, 0.76f, 0.76f, 0.06f, Color(0xFFD8DEE6))
        // cook top
        rr(0.12f, 0.14f, 0.76f, 0.26f, 0.06f, Color(0xFF4A5560))
        ov(0.18f, 0.18f, 0.28f, 0.1f, Color(0xFFFF7043))
        ov(0.22f, 0.2f, 0.2f, 0.06f, Color(0xFF2E353C))
        ov(0.54f, 0.18f, 0.28f, 0.1f, Color(0xFFFF7043))
        ov(0.58f, 0.2f, 0.2f, 0.06f, Color(0xFF2E353C))
        ov(0.18f, 0.29f, 0.28f, 0.08f, Color(0xFF6B7783))
        ov(0.54f, 0.29f, 0.28f, 0.08f, Color(0xFF6B7783))
        // knob panel
        rr(0.16f, 0.42f, 0.68f, 0.08f, 0.03f, Color(0xFFB4BDC8))
        ci(0.3f, 0.46f, 0.026f, Color(0xFF3B4650))
        ci(0.5f, 0.46f, 0.026f, Color(0xFF3B4650))
        ci(0.7f, 0.46f, 0.026f, Color(0xFF3B4650))
        // door
        rr(0.16f, 0.54f, 0.68f, 0.32f, 0.04f, Color(0xFF9AA5B1))
        rr(0.22f, 0.6f, 0.56f, 0.2f, 0.03f, Color(0xFF2E353C))
        rr(0.26f, 0.63f, 0.2f, 0.04f, 0.02f, Color(0x66FFFFFF))
        rr(0.2f, 0.545f, 0.6f, 0.03f, 0.015f, Color(0xFF6B7783))
    },

    "washer" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFE8F6FF))
        // body
        rr(0.16f, 0.06f, 0.68f, 0.88f, 0.06f, Color(0xFFF4F7FA))
        rr(0.16f, 0.06f, 0.68f, 0.88f, 0.06f, Color(0x11000000))
        rr(0.16f, 0.06f, 0.66f, 0.86f, 0.06f, Color(0xFFF4F7FA))
        // control panel
        rr(0.16f, 0.06f, 0.66f, 0.18f, 0.06f, Color(0xFFD2DBE4))
        ci(0.3f, 0.15f, 0.04f, Color(0xFF5E7FA0))
        rr(0.42f, 0.12f, 0.16f, 0.06f, 0.02f, Color(0xFF3FA7A0))
        ci(0.68f, 0.15f, 0.025f, Color(0xFFFF7043))
        ci(0.75f, 0.15f, 0.025f, Color(0xFFFFC93C))
        // door
        ci(0.49f, 0.58f, 0.27f, Color(0xFFB4BDC8))
        ci(0.49f, 0.58f, 0.22f, Color(0xFF5BB8F0))
        ci(0.49f, 0.62f, 0.17f, Color(0xFF8ED1FC))
        ov(0.34f, 0.66f, 0.3f, 0.08f, Color(0xFFD6F0FF))
        ci(0.4f, 0.5f, 0.03f, Color(0x88FFFFFF))
        ci(0.46f, 0.7f, 0.02f, Color.White)
        ci(0.54f, 0.64f, 0.016f, Color.White)
    },

    "shelf" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFFFF0D6))
        // sides
        rr(0.12f, 0.08f, 0.05f, 0.84f, 0.015f, WOOD_D)
        rr(0.83f, 0.08f, 0.05f, 0.84f, 0.015f, WOOD_D)
        // back
        rr(0.17f, 0.08f, 0.66f, 0.84f, 0.0f, Color(0xFFF2D5AA))
        // top shelf: books
        rr(0.2f, 0.16f, 0.07f, 0.2f, 0.01f, Color(0xFFE8504A))
        rr(0.28f, 0.2f, 0.07f, 0.16f, 0.01f, Color(0xFF3F7FD6))
        rr(0.36f, 0.14f, 0.07f, 0.22f, 0.01f, Color(0xFF4FB477))
        rr(0.44f, 0.18f, 0.07f, 0.18f, 0.01f, Color(0xFFFFC93C))
        // plant
        rr(0.62f, 0.26f, 0.12f, 0.1f, 0.025f, Color(0xFFE8734A))
        ci(0.68f, 0.22f, 0.05f, Color(0xFF4FB477))
        ci(0.64f, 0.19f, 0.03f, Color(0xFF6CCB8E))
        // boards
        rr(0.1f, 0.36f, 0.8f, 0.05f, 0.015f, WOOD)
        // middle shelf: box + ball
        rr(0.22f, 0.5f, 0.2f, 0.14f, 0.02f, Color(0xFF9B5DE5))
        rr(0.22f, 0.5f, 0.2f, 0.04f, 0.02f, Color(0xFFB98CF0))
        ci(0.62f, 0.58f, 0.06f, Color(0xFFFF7BAC))
        rr(0.1f, 0.64f, 0.8f, 0.05f, 0.015f, WOOD)
        // bottom shelf: basket
        rr(0.3f, 0.78f, 0.4f, 0.1f, 0.03f, Color(0xFFD7A064))
        ln(0.4f, 0.78f, 0.4f, 0.88f, 0.008f, WOOD_D)
        ln(0.5f, 0.78f, 0.5f, 0.88f, 0.008f, WOOD_D)
        ln(0.6f, 0.78f, 0.6f, 0.88f, 0.008f, WOOD_D)
        rr(0.1f, 0.88f, 0.8f, 0.06f, 0.015f, WOOD)
    },

    "curtain" to d {
        rr(0f, 0f, 1f, 1f, 0.12f, Color(0xFFFFF3E6))
        // window view
        rr(0.2f, 0.18f, 0.6f, 0.66f, 0.02f, Color(0xFF8ED1FC))
        ci(0.62f, 0.34f, 0.07f, Color(0xFFFFE680))
        ov(0.28f, 0.42f, 0.3f, 0.12f, Color.White)
        ov(0.38f, 0.38f, 0.26f, 0.12f, Color.White)
        ln(0.5f, 0.18f, 0.5f, 0.84f, 0.012f, Color.White)
        // left curtain
        val s = size.minDimension
        val l = Path()
        l.moveTo(0.1f * s, 0.14f * s)
        l.lineTo(0.5f * s, 0.14f * s)
        l.quadraticTo(0.34f * s, 0.5f * s, 0.4f * s, 0.9f * s)
        l.lineTo(0.1f * s, 0.9f * s)
        l.close()
        drawPath(l, Color(0xFFE5628F))
        val r = Path()
        r.moveTo(0.9f * s, 0.14f * s)
        r.lineTo(0.5f * s, 0.14f * s)
        r.quadraticTo(0.66f * s, 0.5f * s, 0.6f * s, 0.9f * s)
        r.lineTo(0.9f * s, 0.9f * s)
        r.close()
        drawPath(r, Color(0xFFE5628F))
        // folds
        ln(0.2f, 0.16f, 0.2f, 0.9f, 0.025f, Color(0xFFC94677))
        ln(0.32f, 0.16f, 0.33f, 0.9f, 0.025f, Color(0xFFC94677))
        ln(0.8f, 0.16f, 0.8f, 0.9f, 0.025f, Color(0xFFC94677))
        ln(0.68f, 0.16f, 0.67f, 0.9f, 0.025f, Color(0xFFC94677))
        // rod
        rr(0.06f, 0.1f, 0.88f, 0.045f, 0.02f, WOOD_D)
        ci(0.07f, 0.1225f, 0.03f, Color(0xFFFFC93C))
        ci(0.93f, 0.1225f, 0.03f, Color(0xFFFFC93C))
    },
)
