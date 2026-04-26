package com.company.app.shared.data.network

import com.company.app.shared.data.model.AuthResponse
import com.company.app.shared.data.model.LoginRequest
import com.company.app.shared.data.model.RemoteConfig
import com.company.app.shared.data.model.UserResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ApiService(
    private val baseUrl: String,
    private val tokenProvider: () -> String?
) {
    private val client = HttpClient(platformHttpClient().engine) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true; isLenient = true })
        }
        install(Logging) { level = LogLevel.INFO }
    }

    suspend fun login(email: String, password: String): AuthResponse =
        client.post("$baseUrl/api/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()

    suspend fun getConfigs(): List<RemoteConfig> =
        client.get("$baseUrl/api/config").body()

    suspend fun getMe(): UserResponse =
        client.get("$baseUrl/api/auth/me") {
            tokenProvider()?.let { bearerAuth(it) }
        }.body()
}
