package com.company.app.modules.subscription

import com.company.app.common.exception.AppException
import com.company.app.modules.payment.MidtransService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SubscriptionService(
    private val planRepo: SubscriptionPlanRepository,
    private val subRepo: UserSubscriptionRepository,
    private val midtransService: MidtransService
) {
    fun getPlans(): List<SubscriptionPlanResponse> =
        planRepo.findByIsActiveTrue().map { it.toResponse() }

    fun getStatus(userId: Long): PremiumStatusResponse {
        val active = subRepo.findActiveByUserId(userId).firstOrNull()
        return PremiumStatusResponse(
            isPremium = active != null,
            subscription = active?.toResponse()
        )
    }

    fun isPremium(userId: Long): Boolean =
        subRepo.findActiveByUserId(userId).isNotEmpty()

    @Transactional
    fun createPurchase(userId: Long, request: PurchaseRequest): SnapTokenResponse {
        val plan = planRepo.findById(request.planId).orElseThrow {
            AppException.notFound("Subscription plan not found")
        }
        val sub = UserSubscription(userId = userId, plan = plan, status = "PENDING")
        subRepo.save(sub)

        val snapResult = midtransService.createSnapToken(
            orderId = "SUB-${sub.id}",
            amount = plan.priceIdr,
            userId = userId,
            description = "Calories Tracker - ${plan.name}"
        )
        sub.snapToken = snapResult.token
        subRepo.save(sub)

        return SnapTokenResponse(
            snapToken = snapResult.token,
            subscriptionId = sub.id,
            redirectUrl = snapResult.redirectUrl
        )
    }

    @Transactional
    fun handlePaymentNotification(payload: Map<String, Any>) {
        val orderId = payload["order_id"] as? String ?: return
        if (!orderId.startsWith("SUB-")) return

        val subId = orderId.removePrefix("SUB-").toLongOrNull() ?: return
        val sub = subRepo.findById(subId).orElse(null) ?: return

        val transactionStatus = payload["transaction_status"] as? String ?: return
        val paymentType = payload["payment_type"] as? String

        when (transactionStatus) {
            "capture", "settlement" -> {
                sub.status = "ACTIVE"
                sub.startedAt = LocalDateTime.now()
                sub.expiresAt = LocalDateTime.now().plusDays(sub.plan.durationDays.toLong())
                sub.paymentId = payload["transaction_id"] as? String
                sub.paymentMethod = paymentType
                sub.updatedAt = LocalDateTime.now()
            }
            "cancel", "deny", "expire" -> {
                sub.status = "CANCELLED"
                sub.updatedAt = LocalDateTime.now()
            }
        }
        subRepo.save(sub)
    }

    fun getAllSubscriptions(): List<UserSubscriptionResponse> =
        subRepo.findAll().map { it.toResponse() }
}
