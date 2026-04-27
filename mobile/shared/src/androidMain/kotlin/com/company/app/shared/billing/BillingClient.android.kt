package com.company.app.shared.billing

import com.android.billingclient.api.BillingClient as AndroidBillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual fun createBillingClient(): BillingClient = GooglePlayBillingClient()

class GooglePlayBillingClient : BillingClient {

    private var client: AndroidBillingClient? = null
    private var pendingCont: CancellableContinuation<PurchaseResult>? = null

    private val purchasesListener = PurchasesUpdatedListener { result, purchases ->
        val cont = pendingCont ?: return@PurchasesUpdatedListener
        pendingCont = null
        if (!cont.isActive) return@PurchasesUpdatedListener
        when (result.responseCode) {
            AndroidBillingClient.BillingResponseCode.OK -> {
                val p = purchases?.firstOrNull()
                if (p != null) cont.resume(p.toSuccess())
                else cont.resume(PurchaseResult.Error("No purchase returned"))
            }
            AndroidBillingClient.BillingResponseCode.USER_CANCELED ->
                cont.resume(PurchaseResult.UserCancelled)
            else ->
                cont.resume(PurchaseResult.Error(result.debugMessage))
        }
    }

    override suspend fun connect(): Boolean {
        val activity = ActivityProvider.get() ?: return false
        return suspendCancellableCoroutine { cont ->
            val c = AndroidBillingClient.newBuilder(activity)
                .enablePendingPurchases(
                    PendingPurchasesParams.newBuilder().enableOneTimeProducts().build()
                )
                .setListener(purchasesListener)
                .build()
            client = c
            c.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(r: BillingResult) {
                    if (cont.isActive)
                        cont.resume(r.responseCode == AndroidBillingClient.BillingResponseCode.OK)
                }
                override fun onBillingServiceDisconnected() {}
            })
        }
    }

    override suspend fun queryProduct(productId: String): Result<ProductDetails> {
        val c = client ?: return Result.failure(IllegalStateException("Call connect() first"))
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(AndroidBillingClient.ProductType.SUBS)
                    .build()
            ))
            .build()
        return suspendCancellableCoroutine { cont ->
            c.queryProductDetailsAsync(params) { result, details ->
                if (!cont.isActive) return@queryProductDetailsAsync
                if (result.responseCode == AndroidBillingClient.BillingResponseCode.OK && details.isNotEmpty()) {
                    val d = details.first()
                    val offerDetails = d.subscriptionOfferDetails?.firstOrNull()
                    val price = offerDetails
                        ?.pricingPhases?.pricingPhaseList?.lastOrNull()
                        ?.formattedPrice ?: ""
                    val trialDays = offerDetails?.pricingPhases?.pricingPhaseList
                        ?.firstOrNull { it.priceAmountMicros == 0L }
                        ?.let { phase -> parsePeriodToDays(phase.billingPeriod) }
                    cont.resume(Result.success(ProductDetails(
                        productId = d.productId,
                        name = d.name,
                        priceFormatted = price,
                        trialDays = trialDays
                    )))
                } else {
                    cont.resume(Result.failure(Exception("Product not found: ${result.debugMessage}")))
                }
            }
        }
    }

    override suspend fun launchPurchaseFlow(productId: String): PurchaseResult {
        val c = client ?: return PurchaseResult.Error("Call connect() first")
        val activity = ActivityProvider.get() ?: return PurchaseResult.Error("No active Activity")

        // Fetch product details synchronously first
        val detailsResult = queryProduct(productId)
        if (detailsResult.isFailure) return PurchaseResult.Error(detailsResult.exceptionOrNull()?.message ?: "Product not found")

        // Need raw ProductDetails from Android API for launchBillingFlow
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(AndroidBillingClient.ProductType.SUBS)
                    .build()
            ))
            .build()

        return suspendCancellableCoroutine { cont ->
            cont.invokeOnCancellation { pendingCont = null }
            c.queryProductDetailsAsync(params) { result, details ->
                if (!cont.isActive) return@queryProductDetailsAsync
                if (result.responseCode != AndroidBillingClient.BillingResponseCode.OK || details.isEmpty()) {
                    cont.resume(PurchaseResult.Error("Failed to load product: ${result.debugMessage}"))
                    return@queryProductDetailsAsync
                }
                val productDetails = details.first()
                val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
                if (offerToken == null) {
                    cont.resume(PurchaseResult.Error("No subscription offer available"))
                    return@queryProductDetailsAsync
                }
                val flowParams = BillingFlowParams.newBuilder()
                    .setProductDetailsParamsList(listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .setOfferToken(offerToken)
                            .build()
                    ))
                    .build()
                // Register cont before launching — listener fires on main thread
                pendingCont = cont
                val launchResult = c.launchBillingFlow(activity, flowParams)
                if (launchResult.responseCode != AndroidBillingClient.BillingResponseCode.OK) {
                    pendingCont = null
                    if (cont.isActive) cont.resume(PurchaseResult.Error(launchResult.debugMessage))
                }
            }
        }
    }

    override suspend fun queryExistingPurchase(productId: String): PurchaseResult.Success? {
        val c = client ?: return null
        return suspendCancellableCoroutine { cont ->
            c.queryPurchasesAsync(
                QueryPurchasesParams.newBuilder()
                    .setProductType(AndroidBillingClient.ProductType.SUBS)
                    .build()
            ) { _, purchases ->
                val purchase = purchases.firstOrNull { it.products.contains(productId) }
                if (cont.isActive) cont.resume(purchase?.toSuccess())
            }
        }
    }

    override fun disconnect() {
        pendingCont = null
        client?.endConnection()
        client = null
    }

    private fun Purchase.toSuccess() = PurchaseResult.Success(
        platform = "GOOGLE_PLAY",
        purchaseToken = purchaseToken,
        orderId = orderId ?: purchaseToken.take(50)
    )

    private fun parsePeriodToDays(isoPeriod: String): Int? = when {
        isoPeriod.contains("D") -> isoPeriod.removePrefix("P").removeSuffix("D").toIntOrNull()
        isoPeriod.contains("W") -> isoPeriod.removePrefix("P").removeSuffix("W").toIntOrNull()?.times(7)
        isoPeriod.contains("M") -> isoPeriod.removePrefix("P").removeSuffix("M").toIntOrNull()?.times(30)
        else -> null
    }
}
