package com.company.app.modules.subscription.service

import com.company.app.common.exception.AppException
import com.company.app.modules.subscription.client.AppleReceiptClient
import com.company.app.modules.subscription.client.GooglePlayReceiptClient
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class ReceiptValidationService(
    private val googlePlayClient: GooglePlayReceiptClient,
    private val appleClient: AppleReceiptClient
) {
    data class ValidationResult(
        val orderId: String,
        val originalTransactionId: String?,
        val status: String,
        val currentPeriodStart: LocalDateTime,
        val currentPeriodEnd: LocalDateTime,
        val trialEndsAt: LocalDateTime?
    )

    fun validate(platform: String, purchaseToken: String, productId: String): ValidationResult =
        when (platform) {
            "GOOGLE_PLAY" -> {
                val r = googlePlayClient.validate(purchaseToken, productId)
                ValidationResult(
                    orderId = r.orderId,
                    originalTransactionId = null,
                    status = r.status,
                    currentPeriodStart = r.currentPeriodStart,
                    currentPeriodEnd = r.currentPeriodEnd,
                    trialEndsAt = r.trialEndsAt
                )
            }
            "APP_STORE" -> {
                val r = appleClient.validate(purchaseToken)
                ValidationResult(
                    orderId = r.transactionId,
                    originalTransactionId = r.originalTransactionId,
                    status = r.status,
                    currentPeriodStart = r.currentPeriodStart,
                    currentPeriodEnd = r.currentPeriodEnd,
                    trialEndsAt = r.trialEndsAt
                )
            }
            else -> throw AppException.badRequest("Unsupported platform: $platform")
        }
}
