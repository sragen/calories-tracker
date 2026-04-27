package com.company.app.modules.subscription

import com.company.app.common.auth.UserPrincipal
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subscriptions")
class SubscriptionController(private val subscriptionService: SubscriptionService) {

    @GetMapping("/plans")
    fun plans() = subscriptionService.getPlans()

    @GetMapping("/me")
    fun myStatus(@AuthenticationPrincipal principal: UserPrincipal) =
        subscriptionService.getStatus(principal.id)

    @PostMapping("/purchase")
    fun purchase(
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestBody request: PurchaseRequest
    ) = subscriptionService.createPurchase(principal.id, request)
}

@RestController
@RequestMapping("/api/admin/subscriptions")
class AdminSubscriptionController(private val subscriptionService: SubscriptionService) {

    @GetMapping
    fun all() = subscriptionService.getAllSubscriptions()
}
