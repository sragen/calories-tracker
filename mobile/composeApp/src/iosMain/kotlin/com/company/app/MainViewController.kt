package com.company.app

import androidx.compose.ui.window.ComposeUIViewController
import com.company.app.ui.App
import kotlin.experimental.ExperimentalNativeApi
import kotlin.native.setUnhandledExceptionHook
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController { App() }

@OptIn(ExperimentalNativeApi::class)
fun startKoinIos() {
    // Print the Kotlin stack trace before aborting — otherwise Xcode only shows
    // propagateExceptionFinalResort, whose source isn't shipped with the klib,
    // hiding the real thrower.
    setUnhandledExceptionHook { throwable ->
        println("=== UNHANDLED KOTLIN EXCEPTION ===")
        println("${throwable::class.qualifiedName}: ${throwable.message}")
        throwable.printStackTrace()
        println("=== END UNHANDLED KOTLIN EXCEPTION ===")
    }
    startKoin { modules(iosAppModule) }
}
