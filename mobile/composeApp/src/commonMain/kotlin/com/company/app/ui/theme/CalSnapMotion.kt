package com.company.app.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.spring

/**
 * Shared motion vocabulary. Durations and curves live here so the four key
 * moments stay in step instead of drifting apart per screen.
 */

/** cubic-bezier(0.25, 1, 0.5, 1) — decisive start, long soft landing. */
val EaseOutQuart: Easing = CubicBezierEasing(0.25f, 1f, 0.5f, 1f)

/** cubic-bezier(0.22, 1, 0.36, 1) — used for reveal/pop entrances. */
val EaseOutReveal: Easing = CubicBezierEasing(0.22f, 1f, 0.36f, 1f)

object CalSnapMotion {
    /** 01 — calorie ring fill and its synced count-up. */
    const val RingFillMs = 1300

    /** 02 — AI scan sweep across the photo, and its per-item stagger. */
    const val ScanSweepMs = 1600
    const val ScanStaggerMs = 90

    /** 03 — plan reveal: donut segments and the target number pop. */
    const val RevealMs = 700
    const val RevealStaggerMs = 120

    /** 04 — Snap FAB idle float cycle and press response. */
    const val FabFloatMs = 3600
    const val FabPressMs = 120

    /** Spring used for the ring: settles with a small, deliberate overshoot. */
    fun ringSpring() = spring<Float>(dampingRatio = 0.7f, stiffness = 180f)
}
