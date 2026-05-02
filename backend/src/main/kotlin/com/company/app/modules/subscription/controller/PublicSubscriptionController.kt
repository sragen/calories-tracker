package com.company.app.modules.subscription.controller

import com.company.app.modules.subscription.dto.SubscriptionPlanResponse
import com.company.app.modules.subscription.service.SubscriptionService
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/subscription")
@Tag(name = "Subscription (public)")
class PublicSubscriptionController(
    private val subscriptionService: SubscriptionService,
) {
    @GetMapping("/plans")
    fun listActivePlans(): List<SubscriptionPlanResponse> = subscriptionService.getActivePlans()
}
