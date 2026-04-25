package com.company.app.common.auth

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.time.LocalDateTime

data class RegisterRequest(
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank @field:Size(min = 8, message = "Password minimum 8 characters")
    val password: String,
    @field:NotBlank val name: String,
    val phone: String? = null
)

data class LoginRequest(
    @field:NotBlank val email: String,
    @field:NotBlank val password: String
)

data class RefreshRequest(
    @field:NotBlank val refreshToken: String
)

data class UpdateProfileRequest(
    val name: String? = null,
    val phone: String? = null,
    val avatarUrl: String? = null,
    val fcmToken: String? = null
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val expiresIn: Long = 86400
)

data class UserResponse(
    val id: Long,
    val email: String?,
    val phone: String?,
    val name: String,
    val role: String,
    val status: String,
    val avatarUrl: String?,
    val createdAt: LocalDateTime
)
