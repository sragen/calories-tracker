package com.company.app.modules.subscription.dto

data class EntitlementResponse(
    val entitled: Boolean,
    val source: String? = null,
    val status: String? = null,
    val expiresAt: String? = null,
    val gracePeriodEndsAt: String? = null
)
