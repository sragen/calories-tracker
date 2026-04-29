package com.company.app.ui.platform

import androidx.compose.runtime.Composable

/**
 * Platform haptic feedback wrapper.
 *
 * Usage in a composable:
 *   val haptic = rememberHapticFeedback()
 *   haptic.light()
 *
 * Haptic moments (from design spec):
 *   light()     — ruler tick, goal/activity card selection, pull-to-refresh threshold
 *   medium()    — swipe-to-delete commit, shutter tap
 *   selection() — ruler major-tick scroll
 *   success()   — scan result reveal, pull-to-refresh complete, streak milestone
 *   error()     — AI scan low confidence, barcode not found
 */
interface HapticFeedback {
    fun light()
    fun medium()
    fun selection()
    fun success()
    fun error()
}

@Composable
expect fun rememberHapticFeedback(): HapticFeedback
