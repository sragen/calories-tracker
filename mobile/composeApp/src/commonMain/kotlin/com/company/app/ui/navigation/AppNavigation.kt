package com.company.app.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object SearchFood : Screen("search_food")
    object BarcodeScanner : Screen("barcode_scanner")
    object SubmitFood : Screen("submit_food")
    object AiScan : Screen("ai_scan")
    object AiScanResult : Screen("ai_scan_result")
    object Paywall : Screen("paywall")
    object SubscriptionStatus : Screen("subscription_status")
    object Analytics : Screen("analytics")
    object Diary : Screen("diary")
    object Profile : Screen("profile")
}
