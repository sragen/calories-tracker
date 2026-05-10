package com.company.app.ui.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import platform.UIKit.UIStatusBarStyle
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewController

// Single source of truth for the current iOS status bar style.
// The host UIViewController (see MainViewController.kt) reads from here
// inside its preferredStatusBarStyle() override.
internal object StatusBarController {
    var rootVc: UIViewController? = null
    var style: UIStatusBarStyle = UIStatusBarStyleDarkContent
}

@Composable
actual fun SetStatusBarStyle(style: StatusBarStyle) {
    DisposableEffect(style) {
        val previous = StatusBarController.style
        StatusBarController.style = when (style) {
            StatusBarStyle.Dark -> UIStatusBarStyleDarkContent
            StatusBarStyle.Light -> UIStatusBarStyleLightContent
        }
        StatusBarController.rootVc?.setNeedsStatusBarAppearanceUpdate()
        onDispose {
            StatusBarController.style = previous
            StatusBarController.rootVc?.setNeedsStatusBarAppearanceUpdate()
        }
    }
}
