package com.company.app.modules.subscription.service

import com.company.app.common.exception.AppException
import com.company.app.modules.subscription.dto.SubscriptionPlanResponse
import com.company.app.modules.subscription.dto.SubscriptionStatusResponse
import com.company.app.modules.subscription.entity.PaymentEvent
import com.company.app.modules.subscription.entity.Subscription
import com.company.app.modules.subscription.entity.SubscriptionPlan
import com.company.app.modules.subscription.repository.PaymentEventRepository
import com.company.app.modules.subscription.repository.SubscriptionPlanRepository
import com.company.app.modules.subscription.repository.SubscriptionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val planRepository: SubscriptionPlanRepository,
    private val paymentEventRepository: PaymentEventRepository,
    private val receiptValidationService: ReceiptValidationService
) {
    fun getActivePlans(): List<SubscriptionPlanResponse> =
        planRepository.findByIsActiveTrue()
            .sortedBy { it.intervalDays }
            .map { it.toResponse() }

    private fun SubscriptionPlan.toResponse() = SubscriptionPlanResponse(
        id = id,
        name = name,
        priceIdr = priceIdr,
        intervalDays = intervalDays,
        trialDays = trialDays,
        productIdAndroid = platformProductIdAndroid,
        productIdIos = platformProductIdIos,
    )

    fun getStatus(userId: Long): SubscriptionStatusResponse {
        val sub = subscriptionRepository.findActiveByUserId(userId)
            ?: throw AppException.notFound("No active subscription found")
        val plan = planRepository.findById(sub.planId).orElseThrow { AppException.notFound("Plan not found") }
        return sub.toStatusResponse(plan.name, plan.priceIdr)
    }

    @Transactional
    fun verifyAndCreate(
        userId: Long,
        planId: Long,
        platform: String,
        purchaseToken: String,
        orderId: String
    ): Subscription {
        // Idempotency: return existing subscription if this orderId was already processed
        subscriptionRepository.findByPlatformOrderId(orderId)?.let { return it }

        val plan = planRepository.findById(planId).orElseThrow { AppException.notFound("Plan not found") }
        val productId = plan.platformProductId(platform)

        val validation = try {
            receiptValidationService.validate(platform, purchaseToken, productId)
        } catch (e: AppException) {
            throw e
        } catch (e: Exception) {
            throw AppException.badRequest("Receipt validation failed: ${e.message}")
        }

        // Expire any existing active subscriptions for this user
        subscriptionRepository.expirePreviousForUser(userId, LocalDateTime.now())

        val subscription = subscriptionRepository.save(
            Subscription(
                userId = userId,
                planId = planId,
                status = validation.status,
                platform = platform,
                platformPurchaseToken = purchaseToken,
                platformOrderId = validation.orderId,
                platformOriginalTransactionId = validation.originalTransactionId,
                trialEndsAt = validation.trialEndsAt,
                currentPeriodStart = validation.currentPeriodStart,
                currentPeriodEnd = validation.currentPeriodEnd
            )
        )

        paymentEventRepository.save(
            PaymentEvent(
                subscriptionId = subscription.id,
                userId = userId,
                platform = platform,
                eventType = "PURCHASED",
                platformOrderId = validation.orderId,
                processedAt = LocalDateTime.now()
            )
        )

        return subscription
    }

    @Transactional
    fun restoreByOriginalTransactionId(
        userId: Long,
        platform: String,
        originalTransactionId: String
    ): Subscription {
        val existing = subscriptionRepository
            .findByOriginalTransactionIdAndPlatform(originalTransactionId, platform)

        if (existing != null) {
            // Re-associate with current user if needed (e.g., after reinstall)
            return existing
        }

        throw AppException.notFound("No subscription found for the provided transaction ID")
    }

    private fun Subscription.toStatusResponse(planName: String, priceIdr: Long) =
        SubscriptionStatusResponse(
            subscriptionId = id,
            planName = planName,
            priceIdr = priceIdr,
            status = status,
            platform = platform,
            trialEndsAt = trialEndsAt,
            currentPeriodStart = currentPeriodStart,
            currentPeriodEnd = currentPeriodEnd,
            cancelledAt = cancelledAt,
            createdAt = createdAt
        )
}
