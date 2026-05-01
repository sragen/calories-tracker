package com.company.app.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import com.company.app.shared.data.repository.AuthRepository
import com.company.app.shared.data.repository.BodyProfileRepository
import com.company.app.shared.storage.GuestStorage
import com.company.app.ui.aiscan.AiScanResultScreen
import com.company.app.ui.theme.CalSnapTheme
import com.company.app.ui.aiscan.AiScanScreen
import com.company.app.ui.aiscan.AiScanViewModel
import com.company.app.ui.analytics.AnalyticsScreen
import com.company.app.ui.analytics.AnalyticsViewModel
import com.company.app.ui.components.CalSnapTab
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
import com.company.app.ui.subscription.PaywallScreen
import com.company.app.ui.subscription.SubscriptionState
import com.company.app.ui.subscription.SubscriptionStatusScreen
import com.company.app.ui.subscription.SubscriptionViewModel
import com.company.app.ui.welcome.WelcomeScreen
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun App() {
    CalSnapTheme {
    AppContent()
    }
}

@Composable
private fun AppContent() {
    val authRepo: AuthRepository = koinInject()
    val bodyProfileRepo: BodyProfileRepository = koinInject()
    val guestStorage: GuestStorage = koinInject()
    val scope = rememberCoroutineScope()

    // Hoisted because AiScan screen kicks off analyze() and AiScanResult reads
    // its state — Koin binds AiScanViewModel as `factory`, so injecting in each
    // screen would yield two separate instances and lose in-flight state.
    val aiScanViewModel: AiScanViewModel = koinInject()

    var currentScreen by remember { mutableStateOf<Screen>(Screen.Welcome) }
    var isGuestMode by remember { mutableStateOf(false) }
    var guestScansRemaining by remember { mutableStateOf(GuestStorage.SCAN_LIMIT) }
    var pendingMealType by remember { mutableStateOf("SNACK") }
    var scannedBarcode by remember { mutableStateOf("") }
    var pendingAiImageBytes by remember { mutableStateOf<ByteArray?>(null) }

    // Observe guest scan counter
    LaunchedEffect(Unit) {
        guestStorage.scansRemaining.collect { remaining ->
            guestScansRemaining = remaining
        }
    }

    // Silently redirect if already logged in
    LaunchedEffect(Unit) {
        val loggedIn = authRepo.isLoggedIn()
        if (loggedIn) {
            val hasProfile = bodyProfileRepo.hasProfile().getOrDefault(false)
            currentScreen = if (hasProfile) Screen.Home else Screen.Onboarding
        }
        // Not logged in → stay on Screen.Welcome (initial state)
    }

    // A12 screen-entry stagger: fade + 20dp upward slide, 280ms
    AnimatedContent(
        targetState = currentScreen,
        transitionSpec = {
            (fadeIn(tween(280)) + slideInVertically(tween(280)) { (it * 0.06f).toInt() })
                .togetherWith(fadeOut(tween(180)))
        },
        label = "screenTransition",
    ) { screen ->

    when (screen) {
        Screen.Welcome -> {
            WelcomeScreen(
                guestScansRemaining = guestScansRemaining,
                onTryFree = {
                    isGuestMode = true
                    currentScreen = Screen.AiScan
                },
                onLogin = {
                    isGuestMode = false
                    currentScreen = Screen.Login
                }
            )
        }
        Screen.Login -> {
            val viewModel: LoginViewModel = koinInject()
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    isGuestMode = false
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
                onRegisterSuccess = {
                    isGuestMode = false
                    currentScreen = Screen.Onboarding
                },
                onBack = {
                    currentScreen = if (isGuestMode) Screen.Welcome else Screen.Login
                }
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
                onLogout = { currentScreen = Screen.Welcome },
                onAddFood = { mealType ->
                    pendingMealType = mealType
                    currentScreen = Screen.SearchFood
                },
                onAiScan = { mealType ->
                    pendingMealType = mealType
                    currentScreen = Screen.AiScan
                },
                onSubscription = { currentScreen = Screen.Paywall },
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
            if (isGuestMode && guestScansRemaining <= 0) {
                // Trial exhausted — show paywall before opening camera
                currentScreen = Screen.Paywall
            } else {
                AiScanScreen(
                    onPhotoCaptured = { bytes ->
                        pendingAiImageBytes = bytes
                        if (isGuestMode) {
                            scope.launch { guestStorage.decrementScan() }
                        }
                        aiScanViewModel.analyze(bytes)
                        currentScreen = Screen.AiScanResult
                    },
                    onBack = {
                        currentScreen = if (isGuestMode) Screen.Welcome else Screen.Home
                    }
                )
            }
        }
        Screen.AiScanResult -> {
            AiScanResultScreen(
                viewModel = aiScanViewModel,
                mealType = pendingMealType,
                isGuestMode = isGuestMode,
                onConfirmed = { currentScreen = Screen.Home },
                onBack = {
                    currentScreen = if (isGuestMode) Screen.Welcome else Screen.Home
                },
                onRegisterFromGuest = {
                    isGuestMode = false
                    currentScreen = Screen.Register
                }
            )
        }
        Screen.Paywall -> {
            val viewModel: SubscriptionViewModel = koinInject()
            val state by viewModel.state.collectAsState()
            if (state is SubscriptionState.Entitled) {
                isGuestMode = false
                currentScreen = Screen.SubscriptionStatus
            } else {
                PaywallScreen(
                    viewModel = viewModel,
                    isGuestMode = isGuestMode,
                    onEntitled = {
                        isGuestMode = false
                        currentScreen = Screen.SubscriptionStatus
                    },
                    onRegister = {
                        isGuestMode = false
                        currentScreen = Screen.Register
                    },
                    onBack = {
                        currentScreen = if (isGuestMode) Screen.Welcome else Screen.Home
                    }
                )
            }
        }
        Screen.SubscriptionStatus -> {
            val viewModel: SubscriptionViewModel = koinInject()
            SubscriptionStatusScreen(
                viewModel = viewModel,
                onBack = { currentScreen = Screen.Home }
            )
        }
        Screen.Analytics -> {
            val viewModel: AnalyticsViewModel = koinInject()
            AnalyticsScreen(
                viewModel = viewModel,
                onTabSelected = { tab ->
                    when (tab) {
                        CalSnapTab.HOME -> currentScreen = Screen.Home
                        CalSnapTab.LOG -> { pendingMealType = "SNACK"; currentScreen = Screen.SearchFood }
                        CalSnapTab.PROFILE -> currentScreen = Screen.Profile
                        else -> {}
                    }
                },
                onSnapTap = { pendingMealType = "SNACK"; currentScreen = Screen.AiScan },
                onUpgrade = { currentScreen = Screen.Paywall },
                onBack = { currentScreen = Screen.Home }
            )
        }
        Screen.Profile -> {
            val viewModel: ProfileViewModel = koinInject()
            ProfileScreen(
                viewModel = viewModel,
                onTabSelected = { tab ->
                    when (tab) {
                        CalSnapTab.HOME -> currentScreen = Screen.Home
                        CalSnapTab.STATS -> currentScreen = Screen.Analytics
                        CalSnapTab.LOG -> { pendingMealType = "SNACK"; currentScreen = Screen.SearchFood }
                        else -> {}
                    }
                },
                onSnapTap = { pendingMealType = "SNACK"; currentScreen = Screen.AiScan },
                onSubscription = { currentScreen = Screen.Paywall },
                onLogout = { currentScreen = Screen.Welcome }
            )
        }
        else -> {}
    }
    } // end AnimatedContent
}
