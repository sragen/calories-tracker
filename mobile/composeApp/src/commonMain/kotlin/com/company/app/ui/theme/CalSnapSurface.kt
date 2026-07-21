package com.company.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Depth for the dark theme.
 *
 * Drop shadows do not read against a near-black background, so elevation is
 * expressed as a ladder of progressively lighter surfaces, each separated from
 * what is behind it by a hairline border:
 *
 *   E0 Background → E1 Surface → E2 Card → E3 Card + accent border + glow
 *
 * Use [calSnapCard] for ordinary containers and [calSnapAccentLift] for the one
 * element on a screen that should feel closest to the viewer.
 */

/** E2 — a standard raised container: lighter fill plus a hairline edge. */
fun Modifier.calSnapCard(
    radius: Dp = CalSnapRadius.card,
    fill: Color = CalSnapColors.Card,
    borderColor: Color = CalSnapColors.Border,
): Modifier = this
    .clip(RoundedCornerShape(radius))
    .background(fill)
    .border(1.dp, borderColor, RoundedCornerShape(radius))

/**
 * E3 — the "accent lift". Same fill as [calSnapCard] but ringed in accent, so
 * it reads as the nearest layer. Reserve this for a single hero element per
 * screen; used everywhere it stops meaning anything.
 */
fun Modifier.calSnapAccentLift(
    radius: Dp = CalSnapRadius.card,
    fill: Color = CalSnapColors.Card,
): Modifier = this
    .clip(RoundedCornerShape(radius))
    .background(fill)
    .border(1.dp, CalSnapColors.Accent.copy(alpha = 0.35f), RoundedCornerShape(radius))

/**
 * A soft radial bloom, drawn *behind* a hero element (the calorie ring, a big
 * number) to lift it off the background. This is what replaces the glow a CSS
 * `box-shadow` would provide — Compose Multiplatform 1.7.3 has no blur filter
 * that works reliably on both platforms, so the halo is painted directly as a
 * gradient instead.
 */
@Composable
fun CalSnapGlow(
    modifier: Modifier = Modifier,
    color: Color = CalSnapColors.Accent,
    alpha: Float = 0.16f,
    radiusFraction: Float = 0.68f,
) {
    Box(
        modifier = modifier.background(
            Brush.radialGradient(
                colorStops = arrayOf(
                    0f to color.copy(alpha = alpha),
                    radiusFraction to color.copy(alpha = alpha * 0.35f),
                    1f to Color.Transparent,
                ),
            ),
        ),
    )
}

/** Top-down wash used behind hero areas so the screen is not a flat slab. */
fun Modifier.calSnapHeroWash(
    color: Color = CalSnapColors.Accent,
    alpha: Float = 0.07f,
): Modifier = this.background(
    Brush.radialGradient(
        colorStops = arrayOf(
            0f to color.copy(alpha = alpha),
            1f to Color.Transparent,
        ),
        center = Offset(Float.POSITIVE_INFINITY / 2, 0f),
    ),
)
