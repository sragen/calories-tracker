package com.company.app

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.company.app.shared.data.network.ApiService
import com.company.app.shared.data.repository.AuthRepository
import com.company.app.shared.data.repository.ConfigRepository
import com.company.app.shared.storage.TokenStorage
import com.company.app.ui.home.HomeViewModel
import com.company.app.ui.login.LoginViewModel
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

    single {
        val tokenStorage: TokenStorage = get()
        ApiService(
            baseUrl = BASE_URL,
            tokenProvider = { runBlocking { tokenStorage.accessToken.firstOrNull() } }
        )
    }

    single { AuthRepository(get(), get()) }
    single { ConfigRepository(get()) }

    factory { LoginViewModel(get()) }
    factory { HomeViewModel(get(), get()) }
}
