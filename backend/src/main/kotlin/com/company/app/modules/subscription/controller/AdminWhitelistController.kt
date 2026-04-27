package com.company.app.modules.subscription.controller

import com.company.app.common.auth.UserPrincipal
import com.company.app.common.rbac.RequiresPermission
import com.company.app.common.rbac.RequiresPermission.Action
import com.company.app.modules.subscription.dto.AddToWhitelistRequest
import com.company.app.modules.subscription.dto.WhitelistEntryResponse
import com.company.app.modules.subscription.service.WhitelistService
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/whitelist")
@Tag(name = "Admin - Whitelist")
@SecurityRequirement(name = "bearerAuth")
class AdminWhitelistController(private val whitelistService: WhitelistService) {

    @GetMapping
    @RequiresPermission(module = "WHITELIST", action = Action.READ)
    fun list(pageable: Pageable): Page<WhitelistEntryResponse> =
        whitelistService.findAll(pageable)

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @RequiresPermission(module = "WHITELIST", action = Action.WRITE)
    fun add(
        @Valid @RequestBody req: AddToWhitelistRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): WhitelistEntryResponse =
        whitelistService.add(req.userId, req.note, addedBy = principal.id)

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @RequiresPermission(module = "WHITELIST", action = Action.DELETE)
    fun remove(@PathVariable userId: Long) =
        whitelistService.remove(userId)
}
