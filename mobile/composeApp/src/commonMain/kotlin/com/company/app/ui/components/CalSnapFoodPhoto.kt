package com.company.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.abs

/**
 * Food thumbnail with gradient fallback.
 * Phase 3+: swap in Kamel/Coil for network images via imageUrl parameter.
 *
 * @param name food name — drives fallback initial letter and gradient hue
 * @param imageUrl reserved for Phase 3 network image loading (unused in Phase 0)
 * @param size thumbnail width and height
 * @param cornerRadius thumbnail corner radius
 */
@Composable
fun CalSnapFoodPhoto(
    name: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = 56.dp,
    cornerRadius: Dp = 14.dp,
) {
    val initial = remember(name) { name.firstOrNull()?.uppercaseChar()?.toString() ?: "?" }
    val hue = remember(name) { (abs(name.hashCode()) % 360).toFloat() }

    // Phase 0: gradient fallback only. imageUrl param reserved for Phase 3.
    val gradientStart = hsvToColor(hue, 0.65f, 0.72f)
    val gradientEnd   = hsvToColor((hue + 30f) % 360f, 0.70f, 0.58f)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(gradientStart, gradientEnd),
                )
            ),
    ) {
        Text(
            text = initial,
            fontSize = (size.value * 0.34f).sp,
            fontWeight = FontWeight.W600,
            color = Color.White.copy(alpha = 0.95f),
        )
    }
}

private fun hsvToColor(h: Float, s: Float, v: Float): Color {
    val c = v * s
    val x = c * (1f - kotlin.math.abs((h / 60f) % 2f - 1f))
    val m = v - c
    val (r1, g1, b1) = when {
        h < 60f  -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else     -> Triple(c, 0f, x)
    }
    return Color(r1 + m, g1 + m, b1 + m)
}
