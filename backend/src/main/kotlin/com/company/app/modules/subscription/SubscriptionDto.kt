package com.company.app.modules.subscription

import java.time.LocalDateTime

data class SubscriptionPlanResponse(
    val id: Long,
    val name: String,
    val description: String?,
    val priceIdr: Long,
    val durationDays: Int,
    val features: List<String>
)

data class UserSubscriptionResponse(
    val id: Long,
    val plan: SubscriptionPlanResponse,
    val status: String,
    val startedAt: LocalDateTime?,
    val expiresAt: LocalDateTime?,
    val paymentId: String?,
    val snapToken: String?,
    val createdAt: LocalDateTime
)

data class PremiumStatusResponse(
    val isPremium: Boolean,
    val subscription: UserSubscriptionResponse?
)

data class PurchaseRequest(
    val planId: Long
)

data class SnapTokenResponse(
    val snapToken: String,
    val subscriptionId: Long,
    val redirectUrl: String
)

fun SubscriptionPlan.toResponse() = SubscriptionPlanResponse(
    id = id,
    name = name,
    description = description,
    priceIdr = priceIdr,
    durationDays = durationDays,
    features = features?.removeSurrounding("[", "]")
        ?.split(",")
        ?.map { it.trim().removeSurrounding("\"") }
        ?: emptyList()
)

fun UserSubscription.toResponse() = UserSubscriptionResponse(
    id = id,
    plan = plan.toResponse(),
    status = status,
    startedAt = startedAt,
    expiresAt = expiresAt,
    paymentId = paymentId,
    snapToken = snapToken,
    createdAt = createdAt
)
