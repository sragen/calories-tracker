package com.company.app

import androidx.compose.ui.window.ComposeUIViewController
import com.company.app.ui.App
import org.koin.core.context.startKoin

fun MainViewController() = ComposeUIViewController { App() }

fun startKoinIos() {
    startKoin { modules(iosAppModule) }
}
