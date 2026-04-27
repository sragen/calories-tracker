package com.company.app

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.company.app.shared.data.network.ApiService
import com.company.app.shared.data.repository.AiScanRepository
import com.company.app.shared.data.repository.AuthRepository
import com.company.app.shared.data.repository.BodyProfileRepository
import com.company.app.shared.data.repository.ConfigRepository
import com.company.app.shared.data.repository.DailyGoalRepository
import com.company.app.shared.data.repository.FoodRepository
import com.company.app.shared.data.repository.MealLogRepository
import com.company.app.shared.data.repository.SubscriptionRepository
import com.company.app.shared.storage.RecentFoodStorage
import com.company.app.shared.storage.TokenStorage
import com.company.app.ui.aiscan.AiScanViewModel
import com.company.app.ui.analytics.AnalyticsViewModel
import com.company.app.ui.home.HomeViewModel
import com.company.app.ui.login.LoginViewModel
import com.company.app.ui.onboarding.OnboardingViewModel
import com.company.app.ui.profile.ProfileViewModel
import com.company.app.ui.register.RegisterViewModel
import com.company.app.ui.search.SearchFoodViewModel
import com.company.app.ui.submit.SubmitFoodViewModel
import com.company.app.ui.subscription.SubscriptionViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

private const val BASE_URL = "http://10.0.2.2:8080"

val appModule = module {
    single {
        PreferenceDataStoreFactory.create {
            androidContext().preferencesDataStoreFile("app_prefs")
        }
    }

    single { TokenStorage(get()) }
    single { RecentFoodStorage(get()) }

    single {
        val tokenStorage: TokenStorage = get()
        ApiService(
            baseUrl = BASE_URL,
            tokenProvider = { runBlocking { tokenStorage.accessToken.firstOrNull() } }
        )
    }

    // Repositories
    single { AuthRepository(get(), get()) }
    single { ConfigRepository(get()) }
    single { FoodRepository(get(), get()) }
    single { BodyProfileRepository(get()) }
    single { DailyGoalRepository(get()) }
    single { MealLogRepository(get()) }
    single { SubscriptionRepository(get()) }
    single { AiScanRepository(get()) }

    // ViewModels
    factory { LoginViewModel(get()) }
    factory { RegisterViewModel(get()) }
    factory { OnboardingViewModel(get()) }
    factory { HomeViewModel(get(), get()) }
    factory { SearchFoodViewModel(get(), get()) }
    factory { SubmitFoodViewModel(get()) }
    factory { ProfileViewModel(get(), get(), get(), get()) }
    factory { AiScanViewModel(get()) }
    factory { SubscriptionViewModel(get()) }
    factory { AnalyticsViewModel(get(), get()) }
}
