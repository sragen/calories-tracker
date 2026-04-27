package com.company.app.modules.subscription.service

import com.company.app.modules.subscription.dto.EntitlementResponse
import com.company.app.modules.subscription.repository.PremiumWhitelistRepository
import com.company.app.modules.subscription.repository.SubscriptionRepository
import com.company.app.modules.user.UserRepository
import org.springframework.stereotype.Service

@Service
class EntitlementService(
    private val userRepository: UserRepository,
    private val whitelistRepository: PremiumWhitelistRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    fun checkEntitlement(userId: Long): EntitlementResponse {
        val user = userRepository.findActiveById(userId)
            ?: return EntitlementResponse(entitled = false)

        if (user.role == "SUPER_ADMIN") {
            return EntitlementResponse(entitled = true, source = "ROLE")
        }

        if (whitelistRepository.existsByUserId(userId)) {
            return EntitlementResponse(entitled = true, source = "WHITELIST")
        }

        val sub = subscriptionRepository.findActiveByUserId(userId)
        if (sub != null) {
            return EntitlementResponse(
                entitled = true,
                source = "SUBSCRIPTION",
                status = sub.status,
                expiresAt = sub.currentPeriodEnd?.toString(),
                gracePeriodEndsAt = sub.gracePeriodEndsAt?.toString()
            )
        }

        return EntitlementResponse(entitled = false)
    }
}
