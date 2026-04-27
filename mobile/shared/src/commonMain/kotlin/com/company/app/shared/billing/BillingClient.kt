package com.company.app.shared.billing

data class ProductDetails(
    val productId: String,
    val name: String,
    val priceFormatted: String,
    val trialDays: Int?
)

sealed class PurchaseResult {
    data class Success(
        val platform: String,
        val purchaseToken: String,
        val orderId: String
    ) : PurchaseResult()
    object UserCancelled : PurchaseResult()
    data class Error(val message: String) : PurchaseResult()
}

interface BillingClient {
    suspend fun connect(): Boolean
    suspend fun queryProduct(productId: String): Result<ProductDetails>
    suspend fun launchPurchaseFlow(productId: String): PurchaseResult
    suspend fun queryExistingPurchase(productId: String): PurchaseResult.Success?
    fun disconnect()
}

expect fun createBillingClient(): BillingClient
