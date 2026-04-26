package com.company.app.modules.config

import com.company.app.common.auth.UserPrincipal
import com.company.app.common.rbac.RequiresPermission
import com.company.app.common.rbac.RequiresPermission.Action
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/admin/configs")
@Tag(name = "Admin - Configs")
@SecurityRequirement(name = "bearerAuth")
class AdminConfigController(private val service: AppConfigService) {

    @GetMapping
    @RequiresPermission(module = "CONFIG", action = Action.READ)
    fun getAll(): List<ConfigResponse> = service.getAll()

    @GetMapping("/{key}")
    @RequiresPermission(module = "CONFIG", action = Action.READ)
    fun getByKey(@PathVariable key: String): ConfigResponse = service.getByKey(key)

    @PutMapping("/{key}")
    @RequiresPermission(module = "CONFIG", action = Action.WRITE)
    fun update(
        @PathVariable key: String,
        @Valid @RequestBody req: UpdateConfigRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ConfigResponse = service.update(key, req, principal.id)

    @PostMapping("/{key}/toggle")
    @RequiresPermission(module = "CONFIG", action = Action.WRITE)
    fun toggle(
        @PathVariable key: String,
        @AuthenticationPrincipal principal: UserPrincipal
    ): ConfigResponse = service.toggle(key, principal.id)
}
