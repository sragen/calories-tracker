package com.company.app.ui

import androidx.compose.runtime.*
import com.company.app.shared.data.repository.AuthRepository
import com.company.app.shared.data.repository.BodyProfileRepository
import com.company.app.ui.aiscan.AiScanResultScreen
import com.company.app.ui.aiscan.AiScanScreen
import com.company.app.ui.aiscan.AiScanViewModel
import com.company.app.ui.analytics.AnalyticsScreen
import com.company.app.ui.analytics.AnalyticsViewModel
import com.company.app.ui.home.HomeScreen
import com.company.app.ui.home.HomeViewModel
import com.company.app.ui.login.LoginScreen
import com.company.app.ui.login.LoginViewModel
import com.company.app.ui.navigation.Screen
import com.company.app.ui.onboarding.OnboardingScreen
import com.company.app.ui.onboarding.OnboardingViewModel
import com.company.app.ui.profile.ProfileScreen
import com.company.app.ui.profile.ProfileViewModel
import com.company.app.ui.register.RegisterScreen
import com.company.app.ui.register.RegisterViewModel
import com.company.app.ui.scan.BarcodeScannerScreen
import com.company.app.ui.search.SearchFoodScreen
import com.company.app.ui.search.SearchFoodViewModel
import com.company.app.ui.submit.SubmitFoodScreen
import com.company.app.ui.submit.SubmitFoodViewModel
import com.company.app.ui.subscription.MidtransPaymentScreen
import com.company.app.ui.subscription.SubscriptionScreen
import com.company.app.ui.subscription.SubscriptionViewModel
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    val authRepo: AuthRepository = koinInject()
    val bodyProfileRepo: BodyProfileRepository = koinInject()
    val scope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    var pendingMealType by remember { mutableStateOf("SNACK") }
    var scannedBarcode by remember { mutableStateOf("") }
    var pendingAiImageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var snapToken by remember { mutableStateOf("") }
    var isCheckingAuth by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        scope.launch {
            val loggedIn = authRepo.isLoggedIn()
            currentScreen = if (loggedIn) {
                val hasProfile = bodyProfileRepo.hasProfile().getOrDefault(false)
                if (hasProfile) Screen.Home else Screen.Onboarding
            } else {
                Screen.Login
            }
            isCheckingAuth = false
        }
    }

    if (isCheckingAuth) return

    when (currentScreen) {
        Screen.Login -> {
            val viewModel: LoginViewModel = koinInject()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    scope.launch {
                        val hasProfile = bodyProfileRepo.hasProfile().getOrDefault(false)
                        currentScreen = if (hasProfile) Screen.Home else Screen.Onboarding
                    }
                },
                onNavigateToRegister = { currentScreen = Screen.Register }
            )
        }
        Screen.Register -> {
            val viewModel: RegisterViewModel = koinInject()
            RegisterScreen(
                viewModel = viewModel,
                onRegisterSuccess = { currentScreen = Screen.Onboarding },
                onBack = { currentScreen = Screen.Login }
            )
        }
        Screen.Onboarding -> {
            val viewModel: OnboardingViewModel = koinInject()
            OnboardingScreen(
                viewModel = viewModel,
                onComplete = { currentScreen = Screen.Home }
            )
        }
        Screen.Home -> {
            val viewModel: HomeViewModel = koinInject()
            HomeScreen(
                viewModel = viewModel,
                onLogout = { currentScreen = Screen.Login },
                onAddFood = { mealType ->
                    pendingMealType = mealType
                    currentScreen = Screen.SearchFood
                },
                onAiScan = { mealType ->
                    pendingMealType = mealType
                    currentScreen = Screen.AiScan
                },
                onSubscription = { currentScreen = Screen.Subscription },
                onAnalytics = { currentScreen = Screen.Analytics },
                onProfile = { currentScreen = Screen.Profile }
            )
        }
        Screen.SearchFood -> {
            val viewModel: SearchFoodViewModel = koinInject()
            SearchFoodScreen(
                viewModel = viewModel,
                initialMealType = pendingMealType,
                onBack = { currentScreen = Screen.Home },
                onLogSuccess = { currentScreen = Screen.Home },
                onOpenBarcodeScanner = { currentScreen = Screen.BarcodeScanner }
            )
        }
        Screen.BarcodeScanner -> {
            BarcodeScannerScreen(
                onBarcodeDetected = { barcode ->
                    scannedBarcode = barcode
                    currentScreen = Screen.SearchFood
                },
                onBack = { currentScreen = Screen.SearchFood }
            )
        }
        Screen.SubmitFood -> {
            val viewModel: SubmitFoodViewModel = koinInject()
            SubmitFoodScreen(
                viewModel = viewModel,
                initialBarcode = scannedBarcode,
                onSuccess = { currentScreen = Screen.Home },
                onBack = { currentScreen = Screen.SearchFood }
            )
        }
        Screen.AiScan -> {
            val viewModel: AiScanViewModel = koinInject()
            AiScanScreen(
                onPhotoCaptured = { bytes ->
                    pendingAiImageBytes = bytes
                    viewModel.analyze(bytes)
                    currentScreen = Screen.AiScanResult
                },
                onBack = { currentScreen = Screen.Home }
            )
        }
        Screen.AiScanResult -> {
            val viewModel: AiScanViewModel = koinInject()
            AiScanResultScreen(
                viewModel = viewModel,
                mealType = pendingMealType,
                onConfirmed = { currentScreen = Screen.Home },
                onBack = { currentScreen = Screen.Home }
            )
        }
        Screen.Subscription -> {
            val viewModel: SubscriptionViewModel = koinInject()
            SubscriptionScreen(
                viewModel = viewModel,
                onSnapToken = { token ->
                    snapToken = token
                    currentScreen = Screen.MidtransPayment
                },
                onBack = { currentScreen = Screen.Home }
            )
        }
        Screen.Analytics -> {
            val viewModel: AnalyticsViewModel = koinInject()
            AnalyticsScreen(
                viewModel = viewModel,
                onUpgrade = { currentScreen = Screen.Subscription },
                onBack = { currentScreen = Screen.Home }
            )
        }
        Screen.MidtransPayment -> {
            MidtransPaymentScreen(
                snapToken = snapToken,
                onFinished = { currentScreen = Screen.Home },
                onError = { currentScreen = Screen.Subscription }
            )
        }
        Screen.Profile -> {
            val viewModel: ProfileViewModel = koinInject()
            ProfileScreen(
                viewModel = viewModel,
                onLogout = { currentScreen = Screen.Login }
            )
        }
        else -> {}
    }
}
