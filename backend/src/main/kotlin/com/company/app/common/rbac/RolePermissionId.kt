package com.company.app.common.rbac

import java.io.Serializable

data class RolePermissionId(
    val role: String = "",
    val module: String = ""
) : Serializable
