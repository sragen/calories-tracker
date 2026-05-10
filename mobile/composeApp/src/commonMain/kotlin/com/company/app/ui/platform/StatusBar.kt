package com.company.app.ui.platform

import androidx.compose.runtime.Composable

enum class StatusBarStyle { Dark, Light }

/**
 * Sets the status bar icon color while this composable is in the composition.
 * Reverts to the previous style on dispose.
 *
 * Use [StatusBarStyle.Dark] (dark icons) on light backgrounds — the global default.
 * Use [StatusBarStyle.Light] (light icons) on dark backgrounds (camera, paywall hero).
 */
@Composable
expect fun SetStatusBarStyle(style: StatusBarStyle)
