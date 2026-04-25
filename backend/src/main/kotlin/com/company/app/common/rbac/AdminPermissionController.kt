package com.company.app.common.rbac

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/admin/permissions")
@Tag(name = "Admin - Permissions")
@SecurityRequirement(name = "bearerAuth")
class AdminPermissionController(private val service: PermissionService) {

    @GetMapping
    fun getAll(): List<PermissionResponse> = service.getAllPermissions()

    @GetMapping("/role/{role}")
    fun getByRole(@PathVariable role: String): List<PermissionResponse> =
        service.getPermissionsForRole(role)
}
