package com.company.app.common.rbac

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RolePermissionRepository : JpaRepository<RolePermission, RolePermissionId> {
    fun findByRoleAndModule(role: String, module: String): RolePermission?
    fun findByRole(role: String): List<RolePermission>
}
