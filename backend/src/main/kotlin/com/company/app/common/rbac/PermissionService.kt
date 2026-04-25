package com.company.app.common.rbac

import org.springframework.stereotype.Service

data class PermissionResponse(
    val role: String,
    val module: String,
    val canRead: Boolean,
    val canWrite: Boolean,
    val canDelete: Boolean
)

@Service
class PermissionService(private val repo: RolePermissionRepository) {

    fun getPermissionsForRole(role: String): List<PermissionResponse> =
        repo.findByRole(role).map {
            PermissionResponse(it.role, it.module, it.canRead, it.canWrite, it.canDelete)
        }

    fun getAllPermissions(): List<PermissionResponse> =
        repo.findAll().map {
            PermissionResponse(it.role, it.module, it.canRead, it.canWrite, it.canDelete)
        }
}
