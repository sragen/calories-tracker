package com.company.app.modules.subscription.dto

import java.time.LocalDateTime

data class SubscriptionStatusResponse(
    val subscriptionId: Long,
    val planName: String,
    val priceIdr: Long,
    val status: String,
    val platform: String,
    val trialEndsAt: LocalDateTime?,
    val currentPeriodStart: LocalDateTime?,
    val currentPeriodEnd: LocalDateTime?,
    val cancelledAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class AdminSubscriptionResponse(
    val subscriptionId: Long,
    val userId: Long,
    val userName: String,
    val userEmail: String?,
    val planName: String,
    val status: String,
    val platform: String,
    val currentPeriodEnd: LocalDateTime?,
    val createdAt: LocalDateTime
)
