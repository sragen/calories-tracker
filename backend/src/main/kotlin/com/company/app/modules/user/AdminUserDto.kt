package com.company.app.modules.user

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateStaffRequest(
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank @field:Size(min = 8, message = "Password minimum 8 characters")
    val password: String,
    @field:NotBlank val name: String,
    val phone: String? = null,
    val role: String = "STAFF"
)

data class UpdateUserRequest(
    val name: String? = null,
    val phone: String? = null,
    val role: String? = null,
    val avatarUrl: String? = null
)

data class UpdateStatusRequest(
    @field:NotBlank val status: String
)
