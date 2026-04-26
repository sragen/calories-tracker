package com.company.app.shared.data.repository

import com.company.app.shared.data.model.UserResponse
import com.company.app.shared.data.network.ApiService
import com.company.app.shared.storage.TokenStorage
import kotlinx.coroutines.flow.firstOrNull

class AuthRepository(
    private val api: ApiService,
    private val storage: TokenStorage
) {
    suspend fun login(email: String, password: String): Result<UserResponse> = runCatching {
        val auth = api.login(email, password)
        storage.saveTokens(auth.accessToken, auth.refreshToken)
        api.getMe()
    }

    suspend fun logout() = storage.clearTokens()

    suspend fun isLoggedIn(): Boolean = storage.accessToken.firstOrNull() != null

    suspend fun getToken(): String? = storage.accessToken.firstOrNull()
}
