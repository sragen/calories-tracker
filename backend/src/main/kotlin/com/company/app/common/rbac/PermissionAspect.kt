package com.company.app.common.rbac

import com.company.app.common.exception.AppException
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Before
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Aspect
@Component
class PermissionAspect(private val repo: RolePermissionRepository) {

    @Before("@annotation(requires)")
    fun checkPermission(requires: RequiresPermission) {
        val auth = SecurityContextHolder.getContext().authentication
            ?: throw AppException.unauthorized()

        val role = auth.authorities
            .map { it.authority }
            .firstOrNull { it.startsWith("ROLE_") }
            ?.removePrefix("ROLE_")
            ?: throw AppException.forbidden()

        val permission = repo.findByRoleAndModule(role, requires.module)
            ?: throw AppException.forbidden("No permission defined for role=$role module=${requires.module}")

        val allowed = when (requires.action) {
            RequiresPermission.Action.READ   -> permission.canRead
            RequiresPermission.Action.WRITE  -> permission.canWrite
            RequiresPermission.Action.DELETE -> permission.canDelete
        }

        if (!allowed) throw AppException.forbidden(
            "Role $role does not have ${requires.action} on ${requires.module}"
        )
    }
}
