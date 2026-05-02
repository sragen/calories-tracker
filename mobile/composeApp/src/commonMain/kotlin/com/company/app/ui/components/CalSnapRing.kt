package com.company.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.company.app.ui.theme.CalSnapColors

/**
 * Calorie progress ring used on the Home dashboard.
 *
 * @param progress 0f..1f consumed/goal ratio
 * @param size outer diameter (220dp for Home, 120dp for empty state, 80dp for scan result mini)
 * @param strokeWidth ring stroke thickness
 * @param color arc stroke color
 * @param track background circle color
 * @param content composable rendered in the center slot (kcal number + label)
 */
@Composable
fun CalSnapRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 220.dp,
    strokeWidth: Dp = 14.dp,
    color: Color = CalSnapColors.Ink,
    track: Color = CalSnapColors.Divider,
    animate: Boolean = true,
    content: @Composable () -> Unit = {},
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(size),
    ) {
        val target = progress.coerceIn(0f, 1f)
        val animated by animateFloatAsState(
            targetValue = target,
            animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            label = "ringSweep",
        )
        val clampedProgress = if (animate) animated else target
        Canvas(modifier = Modifier.size(size)) {
            val stroke = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            val inset = strokeWidth.toPx() / 2f
            val arcSize = Size(this.size.width - strokeWidth.toPx(), this.size.height - strokeWidth.toPx())
            val topLeft = Offset(inset, inset)

            // Track (full circle)
            drawArc(
                color = track,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke,
                size = arcSize,
                topLeft = topLeft,
            )

            // Progress arc
            if (clampedProgress > 0f) {
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * clampedProgress,
                    useCenter = false,
                    style = stroke,
                    size = arcSize,
                    topLeft = topLeft,
                )
            }
        }
        content()
    }
}
