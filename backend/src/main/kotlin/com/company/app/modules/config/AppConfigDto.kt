package com.company.app.modules.config

import jakarta.validation.constraints.NotBlank

data class ConfigResponse(
    val id: Long,
    val key: String,
    val value: String,
    val type: String,
    val label: String?,
    val description: String?,
    val isActive: Boolean
)

data class PublicConfigResponse(
    val key: String,
    val value: String,
    val type: String
)

data class UpdateConfigRequest(
    @field:NotBlank val value: String,
    val isActive: Boolean? = null
)
