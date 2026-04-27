package com.company.app.shared.billing

actual fun createBillingClient(): BillingClient = StoreKitBillingClient()

// StoreKit 2 requires Swift interop. To implement:
// 1. Create a Swift class in iosApp/ with @objc annotations exposing purchase/restore methods
// 2. Call it from here via Kotlin/Native cinterop
// 3. Or use a cross-platform billing library (e.g. RevenueCat KMP SDK)
class StoreKitBillingClient : BillingClient {
    override suspend fun connect(): Boolean = true

    override suspend fun queryProduct(productId: String): Result<ProductDetails> =
        Result.failure(NotImplementedError("StoreKit 2 integration required — see docs/PAYMENT_PHASES.md Phase 4"))

    override suspend fun launchPurchaseFlow(productId: String): PurchaseResult =
        PurchaseResult.Error("StoreKit 2 integration required — see docs/PAYMENT_PHASES.md Phase 4")

    override suspend fun queryExistingPurchase(productId: String): PurchaseResult.Success? = null

    override fun disconnect() {}
}
