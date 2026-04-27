package com.company.app.modules.subscription.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class AddToWhitelistRequest(
    @field:NotNull val userId: Long,
    val note: String? = null
)

data class WhitelistEntryResponse(
    val id: Long,
    val userId: Long,
    val userName: String,
    val userEmail: String?,
    val note: String?,
    val addedByName: String,
    val createdAt: LocalDateTime
)
