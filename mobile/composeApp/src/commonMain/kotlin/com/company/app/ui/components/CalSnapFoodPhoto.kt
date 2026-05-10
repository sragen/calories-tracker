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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.SubcomposeAsyncImage
import kotlin.math.abs

/**
 * Food thumbnail. Loads [imageUrl] via Coil 3 when present; otherwise renders a
 * deterministic gradient with the food's first letter (also used as
 * placeholder/error state for the network image).
 *
 * @param name drives fallback initial letter and gradient hue
 * @param imageUrl http(s) URL of the food photo, or null for gradient-only
 */
@Composable
fun CalSnapFoodPhoto(
    name: String,
    modifier: Modifier = Modifier,
    imageUrl: String? = null,
    size: Dp = 56.dp,
    cornerRadius: Dp = 14.dp,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val box = modifier.size(size).clip(shape)

    if (imageUrl.isNullOrBlank()) {
        GradientFallback(name, box)
        return
    }
    SubcomposeAsyncImage(
        model = imageUrl,
        contentDescription = name,
        contentScale = ContentScale.Crop,
        modifier = box,
        loading = { GradientFallback(name, Modifier.size(size).clip(shape)) },
        error = { GradientFallback(name, Modifier.size(size).clip(shape)) },
    )
}

@Composable
private fun GradientFallback(name: String, modifier: Modifier) {
    val initial = remember(name) { name.firstOrNull()?.uppercaseChar()?.toString() ?: "?" }
    val hue = remember(name) { (abs(name.hashCode()) % 360).toFloat() }
    val gradientStart = hsvToColor(hue, 0.65f, 0.72f)
    val gradientEnd   = hsvToColor((hue + 30f) % 360f, 0.70f, 0.58f)
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.background(Brush.linearGradient(listOf(gradientStart, gradientEnd))),
    ) {
        Text(
            text = initial,
            fontSize = 18.sp,
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
