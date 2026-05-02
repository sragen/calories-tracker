package com.company.app.shared.data.repository

import com.company.app.shared.data.model.UserResponse
import com.company.app.shared.data.network.ApiService
import com.company.app.shared.storage.TokenStorage
import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class AuthRepository(
    private val api: ApiService,
    private val storage: TokenStorage
) {
    suspend fun login(email: String, password: String): Result<UserResponse> =
        try {
            val auth = api.login(email, password)
            storage.saveTokens(auth.accessToken, auth.refreshToken)
            Result.success(api.getMe())
        } catch (e: ResponseException) {
            Result.failure(Exception(e.extractMessage()))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun register(email: String, password: String, name: String): Result<UserResponse> =
        try {
            val auth = api.register(email, password, name)
            storage.saveTokens(auth.accessToken, auth.refreshToken)
            Result.success(api.getMe())
        } catch (e: ResponseException) {
            Result.failure(Exception(e.extractMessage()))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun getMe(): Result<UserResponse> =
        try {
            Result.success(api.getMe())
        } catch (e: ResponseException) {
            Result.failure(Exception(e.extractMessage()))
        } catch (e: Exception) {
            Result.failure(e)
        }

    suspend fun logout() = storage.clearTokens()

    suspend fun isLoggedIn(): Boolean = storage.accessToken.firstOrNull() != null

    suspend fun getToken(): String? = storage.accessToken.firstOrNull()
}

private suspend fun ResponseException.extractMessage(): String {
    val body = response.bodyAsText()
    return try {
        Json.parseToJsonElement(body)
            .jsonObject["message"]
            ?.jsonPrimitive?.content
            ?: "Request failed (${response.status.value})"
    } catch (_: Exception) {
        "Request failed (${response.status.value})"
    }
}
