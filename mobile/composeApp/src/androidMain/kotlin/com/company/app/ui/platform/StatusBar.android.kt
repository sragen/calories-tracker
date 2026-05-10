package com.company.app.ui.platform

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
actual fun SetStatusBarStyle(style: StatusBarStyle) {
    val view = LocalView.current
    if (view.isInEditMode) return
    DisposableEffect(style) {
        val window = (view.context as Activity).window
        val controller = WindowCompat.getInsetsController(window, view)
        val previousStatus = controller.isAppearanceLightStatusBars
        val previousNav = controller.isAppearanceLightNavigationBars
        val light = style == StatusBarStyle.Dark  // dark icons => "light bars"
        controller.isAppearanceLightStatusBars = light
        controller.isAppearanceLightNavigationBars = light
        onDispose {
            controller.isAppearanceLightStatusBars = previousStatus
            controller.isAppearanceLightNavigationBars = previousNav
        }
    }
}
