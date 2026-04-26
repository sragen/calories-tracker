package com.company.app.ui

import androidx.compose.runtime.*
import com.company.app.ui.home.HomeScreen
import com.company.app.ui.home.HomeViewModel
import com.company.app.ui.login.LoginScreen
import com.company.app.ui.login.LoginViewModel
import com.company.app.ui.navigation.Screen
import org.koin.compose.koinInject

@Composable
fun App(startScreen: Screen = Screen.Login) {
    var currentScreen by remember { mutableStateOf(startScreen) }

    when (currentScreen) {
        Screen.Login -> {
            val viewModel: LoginViewModel = koinInject()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = { currentScreen = Screen.Home }
            )
        }
        Screen.Home -> {
            val viewModel: HomeViewModel = koinInject()
            HomeScreen(
                viewModel = viewModel,
                onLogout = { currentScreen = Screen.Login }
            )
        }
        else -> {}
    }
}
