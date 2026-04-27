package com.company.app.shared.data.repository

import com.company.app.shared.billing.PurchaseResult
import com.company.app.shared.billing.createBillingClient
import com.company.app.shared.data.model.EntitlementResponse
import com.company.app.shared.data.model.RestorePurchaseRequest
import com.company.app.shared.data.model.VerifyPurchaseRequest
import com.company.app.shared.data.network.ApiService

class BillingRepository(
    private val api: ApiService,
    private val planId: Long,
    private val productId: String
) {
    private val billingClient by lazy { createBillingClient() }

    suspend fun getEntitlement(): Result<EntitlementResponse> = runCatching {
        api.getEntitlement()
    }

    suspend fun purchase(): Result<EntitlementResponse> = runCatching {
        billingClient.connect()
        when (val result = billingClient.launchPurchaseFlow(productId)) {
            is PurchaseResult.Success -> api.verifyPurchase(
                VerifyPurchaseRequest(
                    platform = result.platform,
                    planId = planId,
                    purchaseToken = result.purchaseToken,
                    orderId = result.orderId
                )
            )
            is PurchaseResult.UserCancelled -> throw CancellationException("Purchase cancelled by user")
            is PurchaseResult.Error -> throw Exception(result.message)
        }
    }

    suspend fun restore(): Result<EntitlementResponse> = runCatching {
        billingClient.connect()
        val existing = billingClient.queryExistingPurchase(productId)
            ?: throw Exception("No existing purchase found — nothing to restore")
        api.verifyPurchase(
            VerifyPurchaseRequest(
                platform = existing.platform,
                planId = planId,
                purchaseToken = existing.purchaseToken,
                orderId = existing.orderId
            )
        )
    }

    suspend fun restoreIos(originalTransactionId: String): Result<EntitlementResponse> = runCatching {
        api.restorePurchase(
            RestorePurchaseRequest(
                platform = "APP_STORE",
                originalTransactionId = originalTransactionId
            )
        )
    }

    fun release() = billingClient.disconnect()
}

private class CancellationException(message: String) : Exception(message)
