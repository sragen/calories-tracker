package com.company.app.modules.subscription.controller

import com.company.app.common.auth.UserPrincipal
import com.company.app.modules.subscription.dto.EntitlementResponse
import com.company.app.modules.subscription.dto.RestorePurchaseRequest
import com.company.app.modules.subscription.dto.SubscriptionStatusResponse
import com.company.app.modules.subscription.dto.VerifyPurchaseRequest
import com.company.app.modules.subscription.service.EntitlementService
import com.company.app.modules.subscription.service.SubscriptionService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/subscription")
@Tag(name = "Subscription")
@SecurityRequirement(name = "bearerAuth")
class SubscriptionController(
    private val entitlementService: EntitlementService,
    private val subscriptionService: SubscriptionService
) {
    @GetMapping("/entitlement")
    fun getEntitlement(@AuthenticationPrincipal principal: UserPrincipal): EntitlementResponse =
        entitlementService.checkEntitlement(principal.id)

    @GetMapping("/status")
    fun getStatus(@AuthenticationPrincipal principal: UserPrincipal): SubscriptionStatusResponse =
        subscriptionService.getStatus(principal.id)

    @PostMapping("/verify")
    @ResponseStatus(HttpStatus.OK)
    fun verify(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: VerifyPurchaseRequest
    ): EntitlementResponse {
        subscriptionService.verifyAndCreate(
            userId = principal.id,
            planId = req.planId,
            platform = req.platform,
            purchaseToken = req.purchaseToken,
            orderId = req.orderId
        )
        return entitlementService.checkEntitlement(principal.id)
    }

    @PostMapping("/restore")
    fun restore(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: RestorePurchaseRequest
    ): EntitlementResponse {
        subscriptionService.restoreByOriginalTransactionId(
            userId = principal.id,
            platform = req.platform,
            originalTransactionId = req.originalTransactionId
        )
        return entitlementService.checkEntitlement(principal.id)
    }
}
