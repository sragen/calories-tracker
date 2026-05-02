package com.company.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.company.app.ui.theme.CalSnapColors

// Icon path data from the design (24×24 viewBox, stroke style)
private sealed class IcEl
private data class IcPath(val d: String) : IcEl()
private data class IcCircle(val cx: Float, val cy: Float, val r: Float) : IcEl()
private data class IcRect(val x: Float, val y: Float, val w: Float, val h: Float, val rx: Float = 0f) : IcEl()

private val ICON_MAP: Map<String, List<IcEl>> = mapOf(
    "home"     to listOf(IcPath("M3 11l9-7 9 7v9a1 1 0 01-1 1h-5v-6h-6v6H4a1 1 0 01-1-1z")),
    "chart"    to listOf(IcPath("M3 20h18M7 16V8M12 16V4M17 16v-6")),
    "profile"  to listOf(IcPath("M4 21c0-4 4-7 8-7s8 3 8 7"), IcCircle(12f, 8f, 4f)),
    "camera"   to listOf(IcPath("M3 8h3l2-3h8l2 3h3v12H3z"), IcCircle(12f, 13f, 4f)),
    "barcode"  to listOf(IcPath("M3 6v12M6 6v12M9 6v12M12 6v12M15 6v12M18 6v12M21 6v12")),
    "search"   to listOf(IcPath("M20 20l-4-4"), IcCircle(11f, 11f, 7f)),
    "plus"     to listOf(IcPath("M12 5v14M5 12h14")),
    "minus"    to listOf(IcPath("M5 12h14")),
    "check"    to listOf(IcPath("M4 12l5 5L20 6")),
    "chev-l"   to listOf(IcPath("M15 6l-6 6 6 6")),
    "chev-r"   to listOf(IcPath("M9 6l6 6-6 6")),
    "chev-d"   to listOf(IcPath("M6 9l6 6 6-6")),
    "close"    to listOf(IcPath("M6 6l12 12M18 6L6 18")),
    "sparkle"  to listOf(IcPath("M12 3l1.5 4.5L18 9l-4.5 1.5L12 15l-1.5-4.5L6 9l4.5-1.5zM19 16l.7 2.3 2.3.7-2.3.7L19 22l-.7-2.3L16 19l2.3-.7z")),
    "edit"     to listOf(IcPath("M4 20h4l10-10-4-4L4 16zM14 6l4 4")),
    "water"    to listOf(IcPath("M12 3c-4 5-6 8-6 11a6 6 0 0012 0c0-3-2-6-6-11z")),
    "fork"     to listOf(IcPath("M5 3v6a3 3 0 003 3v9M11 3v6M8 3v4M19 3v18M19 12c-2 0-3-1-3-3V3")),
    "weight"   to listOf(IcCircle(12f, 5f, 3f), IcPath("M6.5 8a2 2 0 0 0-1.9 1.4L2.1 18.5A2 2 0 0 0 4 21h16a2 2 0 0 0 1.9-2.5L19.4 9.4A2 2 0 0 0 17.5 8z")),
    "star"     to listOf(IcPath("M12 3l2.7 5.5 6 .9-4.3 4.2 1 6L12 17l-5.4 2.6 1-6L3.3 9.4l6-.9z")),
    "arrow-r"  to listOf(IcPath("M5 12h14M13 5l7 7-7 7")),
    "flash"    to listOf(IcPath("M13 2L4 14h7l-1 8 9-12h-7z")),
    "gallery"  to listOf(IcRect(3f, 3f, 18f, 18f, rx = 2f), IcCircle(9f, 9f, 2f), IcPath("M21 15l-5-5L5 21")),
    "lock"     to listOf(IcRect(3f, 11f, 18f, 11f, rx = 2f), IcPath("M7 11V7a5 5 0 0 1 10 0v4")),
    "bell"     to listOf(IcPath("M6 8a6 6 0 0 1 12 0c0 7 3 9 3 9H3s3-2 3-9M10.3 21a1.94 1.94 0 0 0 3.4 0")),
    "streak"   to listOf(IcPath("M12 2c.6 2.5 2.4 4.2 3.6 5.7C16.6 9 17 10.3 17 12a5 5 0 1 1-10 0c0-1.4.6-2.5 1.5-3.3.2.9.8 1.5 1.5 1.7-.4-2.6.4-5.4 2-8.4z")),
    "flame"    to listOf(IcPath("M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.07-2.14-.22-4.05 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.15.43-2.29 1-3a2.5 2.5 0 0 0 2.5 2.5z")),
    "trash"    to listOf(IcPath("M3 6h18M8 6V4a2 2 0 012-2h4a2 2 0 012 2v2M19 6l-1 14a2 2 0 01-2 2H8a2 2 0 01-2-2L5 6M10 11v6M14 11v6")),
)

/**
 * Stroke icon drawn via Canvas. All icons use a 24×24 viewBox and are rendered as strokes.
 *
 * Available names: home, chart, profile, camera, barcode, search, plus, minus, check,
 * chev-l, chev-r, chev-d, close, sparkle, edit, water, fork, weight, star, arrow-r,
 * flash, gallery, lock, bell, streak, flame
 */
@Composable
fun CalSnapIcon(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp,
    color: Color = CalSnapColors.Ink,
    strokeWidth: Float = 1.8f,
) {
    val elements = remember(name) { ICON_MAP[name] }
    Canvas(modifier = modifier.size(size)) {
        if (elements == null) return@Canvas
        val s = this.size.width / 24f
        scale(s, s, pivot = Offset.Zero) {
            val stroke = Stroke(width = strokeWidth / s, cap = StrokeCap.Round, join = StrokeJoin.Round)
            elements.forEach { el -> drawElement(el, color, stroke) }
        }
    }
}

private fun DrawScope.drawElement(el: IcEl, color: Color, stroke: Stroke) {
    when (el) {
        is IcPath -> {
            val path = PathParser().parsePathString(el.d).toPath()
            drawPath(path, color = color, style = stroke)
        }
        is IcCircle -> {
            drawCircle(
                color = color,
                radius = el.r,
                center = Offset(el.cx, el.cy),
                style = stroke,
            )
        }
        is IcRect -> {
            drawRoundRect(
                color = color,
                topLeft = Offset(el.x, el.y),
                size = Size(el.w, el.h),
                cornerRadius = CornerRadius(el.rx),
                style = stroke,
            )
        }
    }
}
