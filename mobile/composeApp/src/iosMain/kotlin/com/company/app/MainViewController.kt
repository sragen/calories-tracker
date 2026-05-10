package com.company.app

import androidx.compose.ui.window.ComposeUIViewController
import com.company.app.ui.App
import com.company.app.ui.platform.StatusBarController
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.core.context.startKoin
import platform.UIKit.UIStatusBarStyle
import platform.UIKit.UIViewAutoresizingFlexibleHeight
import platform.UIKit.UIViewAutoresizingFlexibleWidth
import platform.UIKit.UIViewController

// Host controller that overrides preferredStatusBarStyle.
// Reads current style from StatusBarController; SetStatusBarStyle composables update it.
private class CalSnapHostViewController : UIViewController(nibName = null, bundle = null) {
    override fun preferredStatusBarStyle(): UIStatusBarStyle = StatusBarController.style
}

@OptIn(ExperimentalForeignApi::class)
fun MainViewController(): UIViewController {
    val host = CalSnapHostViewController()
    val compose = ComposeUIViewController { App() }
    host.view.addSubview(compose.view)
    compose.view.setFrame(host.view.bounds)
    compose.view.setAutoresizingMask(
        UIViewAutoresizingFlexibleWidth or UIViewAutoresizingFlexibleHeight,
    )
    StatusBarController.rootVc = host
    return host
}

fun startKoinIos() {
    startKoin { modules(iosAppModule) }
}
