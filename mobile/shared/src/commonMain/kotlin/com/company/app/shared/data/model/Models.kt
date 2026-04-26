package com.company.app.shared.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 86400
)

@Serializable
data class UserResponse(
    val id: Long,
    val email: String? = null,
    val phone: String? = null,
    val name: String,
    val role: String,
    val status: String,
    val avatarUrl: String? = null,
    val createdAt: String
)

@Serializable
data class RemoteConfig(
    val key: String,
    val value: String,
    val type: String
)

@Serializable
data class AppConfigs(
    val maintenanceMode: Boolean = false,
    val forceUpdate: Boolean = false,
    val minAppVersion: String = "1.0.0",
    val pushNotification: Boolean = true,
    val promoBannerUrl: String = "",
    val maxRetryLogin: Int = 5
) {
    companion object {
        fun from(configs: List<RemoteConfig>): AppConfigs {
            val map = configs.associate { it.key to it.value }
            return AppConfigs(
                maintenanceMode = map["maintenance_mode"]?.toBooleanStrictOrNull() ?: false,
                forceUpdate = map["force_update"]?.toBooleanStrictOrNull() ?: false,
                minAppVersion = map["min_app_version"] ?: "1.0.0",
                pushNotification = map["push_notification"]?.toBooleanStrictOrNull() ?: true,
                promoBannerUrl = map["promo_banner_url"] ?: "",
                maxRetryLogin = map["max_retry_login"]?.toIntOrNull() ?: 5
            )
        }
    }
}

@Serializable
data class ApiError(
    val status: Int = 0,
    val error: String = "",
    val message: String = "Unknown error"
)
