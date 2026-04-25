package com.company.app.common.rbac

import jakarta.persistence.*

@Entity
@Table(name = "role_permissions")
@IdClass(RolePermissionId::class)
class RolePermission(
    @Id
    @Column(nullable = false)
    val role: String,

    @Id
    @Column(nullable = false)
    val module: String,

    @Column(name = "can_read", nullable = false)
    val canRead: Boolean = false,

    @Column(name = "can_write", nullable = false)
    val canWrite: Boolean = false,

    @Column(name = "can_delete", nullable = false)
    val canDelete: Boolean = false
)
