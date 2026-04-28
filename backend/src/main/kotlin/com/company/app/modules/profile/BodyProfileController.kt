package com.company.app.modules.profile

import com.company.app.common.auth.UserPrincipal
import com.company.app.common.exception.AppException
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/profile/body")
class BodyProfileController(private val bodyProfileService: BodyProfileService) {

    @GetMapping
    fun get(@AuthenticationPrincipal principal: UserPrincipal): BodyProfileResponse =
        bodyProfileService.getByUserId(principal.id)
            ?: throw AppException.notFound("Body profile not set up yet")

    @PostMapping
    fun upsert(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody req: BodyProfileRequest
    ): BodyProfileResponse = bodyProfileService.upsert(principal.id, req)

    @PostMapping("/bmr-preview")
    fun preview(@Valid @RequestBody req: BodyProfileRequest): BmrPreviewResponse =
        bodyProfileService.preview(req)

    @GetMapping("/exists")
    fun exists(@AuthenticationPrincipal principal: UserPrincipal): Map<String, Boolean> =
        mapOf("hasProfile" to bodyProfileService.hasProfile(principal.id))
}
