package com.company.app.modules.subscription.dto

data class SubscriptionPlanResponse(
    val id: Long,
    val name: String,
    val priceIdr: Long,
    val intervalDays: Int,
    val trialDays: Int,
    val productIdAndroid: String?,
    val productIdIos: String?,
)
