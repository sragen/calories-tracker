package com.company.app.modules.subscription.service

import com.company.app.modules.subscription.entity.PaymentEvent
import com.company.app.modules.subscription.entity.Subscription
import com.company.app.modules.subscription.repository.PaymentEventRepository
import com.company.app.modules.subscription.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class SubscriptionStateMachine(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentEventRepository: PaymentEventRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Transactional
    fun onRenewed(token: String, platform: String, newPeriodEnd: LocalDateTime, rawPayload: String? = null) {
        val sub = findByToken(token, platform) ?: return
        sub.status = "ACTIVE"
        sub.currentPeriodEnd = newPeriodEnd
        sub.gracePeriodEndsAt = null
        sub.updatedAt = LocalDateTime.now()
        subscriptionRepository.save(sub)
        logEvent(sub, "RENEWED", platform, rawPayload)
    }

    @Transactional
    fun onPaymentFailed(token: String, platform: String, gracePeriodEndsAt: LocalDateTime, rawPayload: String? = null) {
        val sub = findByToken(token, platform) ?: return
        sub.status = "PAST_DUE"
        sub.gracePeriodEndsAt = gracePeriodEndsAt
        sub.updatedAt = LocalDateTime.now()
        subscriptionRepository.save(sub)
        logEvent(sub, "PAYMENT_FAILED", platform, rawPayload)
    }

    @Transactional
    fun onRecovered(token: String, platform: String, newPeriodEnd: LocalDateTime, rawPayload: String? = null) {
        val sub = findByToken(token, platform) ?: return
        sub.status = "ACTIVE"
        sub.currentPeriodEnd = newPeriodEnd
        sub.gracePeriodEndsAt = null
        sub.updatedAt = LocalDateTime.now()
        subscriptionRepository.save(sub)
        logEvent(sub, "PAYMENT_RECOVERED", platform, rawPayload)
    }

    @Transactional
    fun onExpired(token: String, platform: String, rawPayload: String? = null) {
        val sub = findByToken(token, platform) ?: return
        sub.status = "EXPIRED"
        sub.updatedAt = LocalDateTime.now()
        subscriptionRepository.save(sub)
        logEvent(sub, "EXPIRED", platform, rawPayload)
    }

    @Transactional
    fun onCancelled(token: String, platform: String, rawPayload: String? = null) {
        val sub = findByToken(token, platform) ?: return
        sub.status = "CANCELLED"
        sub.cancelledAt = LocalDateTime.now()
        sub.updatedAt = LocalDateTime.now()
        subscriptionRepository.save(sub)
        logEvent(sub, "CANCELLED", platform, rawPayload)
    }

    private fun findByToken(token: String, platform: String): Subscription? {
        val sub = if (platform == "GOOGLE_PLAY") {
            subscriptionRepository.findByTokenAndPlatform(token, platform)
        } else {
            subscriptionRepository.findByOriginalTransactionIdAndPlatform(token, platform)
        }
        if (sub == null) log.warn("Webhook: no subscription found for token=$token platform=$platform")
        return sub
    }

    private fun logEvent(sub: Subscription, type: String, platform: String, rawPayload: String?) {
        paymentEventRepository.save(
            PaymentEvent(
                subscriptionId = sub.id,
                userId = sub.userId,
                platform = platform,
                eventType = type,
                platformOrderId = sub.platformOrderId,
                rawPayload = rawPayload,
                processedAt = LocalDateTime.now()
            )
        )
    }
}
