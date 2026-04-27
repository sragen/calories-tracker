package com.company.app.modules.subscription.controller

import com.company.app.common.exception.AppException
import com.company.app.common.rbac.RequiresPermission
import com.company.app.common.rbac.RequiresPermission.Action
import com.company.app.modules.subscription.dto.AdminSubscriptionResponse
import com.company.app.modules.subscription.repository.SubscriptionPlanRepository
import com.company.app.modules.subscription.repository.SubscriptionRepository
import com.company.app.modules.user.UserRepository
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.bind.annotation.*
import org.springframework.web.bind.annotation.RequestParam

@RestController
@RequestMapping("/api/admin/subscriptions")
@Tag(name = "Admin - Subscriptions")
@SecurityRequirement(name = "bearerAuth")
class AdminSubscriptionController(
    private val subscriptionRepository: SubscriptionRepository,
    private val planRepository: SubscriptionPlanRepository,
    private val userRepository: UserRepository
) {
    @GetMapping
    @RequiresPermission(module = "SUBSCRIPTIONS", action = Action.READ)
    fun list(
        pageable: Pageable,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) platform: String?
    ): Page<AdminSubscriptionResponse> =
        subscriptionRepository.findAllActiveFiltered(pageable, status, platform).map { sub ->
            val user = userRepository.findActiveById(sub.userId)
            val plan = planRepository.findById(sub.planId).orElse(null)
            AdminSubscriptionResponse(
                subscriptionId = sub.id,
                userId = sub.userId,
                userName = user?.name ?: "Unknown",
                userEmail = user?.email,
                planName = plan?.name ?: "Unknown",
                status = sub.status,
                platform = sub.platform,
                currentPeriodEnd = sub.currentPeriodEnd,
                createdAt = sub.createdAt
            )
        }

    @GetMapping("/{userId}")
    @RequiresPermission(module = "SUBSCRIPTIONS", action = Action.READ)
    fun getByUser(@PathVariable userId: Long): AdminSubscriptionResponse {
        val sub = subscriptionRepository.findActiveByUserId(userId)
            ?: throw AppException.notFound("No active subscription for user $userId")
        val user = userRepository.findActiveById(sub.userId)
        val plan = planRepository.findById(sub.planId).orElse(null)
        return AdminSubscriptionResponse(
            subscriptionId = sub.id,
            userId = sub.userId,
            userName = user?.name ?: "Unknown",
            userEmail = user?.email,
            planName = plan?.name ?: "Unknown",
            status = sub.status,
            platform = sub.platform,
            currentPeriodEnd = sub.currentPeriodEnd,
            createdAt = sub.createdAt
        )
    }
}
