package com.company.app.ui.platform

import androidx.compose.runtime.Composable

// iOS status bar style is controlled globally by Info.plist (UIStatusBarStyle =
// UIStatusBarStyleDarkContent, UIViewControllerBasedStatusBarAppearance = false).
//
// Per-screen runtime override would require subclassing UIViewController and overriding
// preferredStatusBarStyle, but Kotlin/Native UIKit bindings don't expose
// addChildViewController:/didMoveToParentViewController: in a way that lets us host the
// internal Compose VC without breaking its lifecycle (it crashes on viewWillAppear when
// returning from a presented modal like the camera picker).
//
// Workaround for screens with dark backgrounds (AI Scan camera, Paywall hero): add a
// subtle darkening gradient strip behind the status bar inside the Compose layout so the
// dark icons stay readable.
@Composable
actual fun SetStatusBarStyle(style: StatusBarStyle) {
    // No-op on iOS for now.
}
