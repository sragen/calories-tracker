package com.company.app.modules.subscription.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class VerifyPurchaseRequest(
    @field:NotBlank val platform: String,
    @field:NotNull val planId: Long,
    @field:NotBlank val purchaseToken: String,
    @field:NotBlank val orderId: String
)

data class RestorePurchaseRequest(
    @field:NotBlank val platform: String,
    @field:NotBlank val originalTransactionId: String
)
