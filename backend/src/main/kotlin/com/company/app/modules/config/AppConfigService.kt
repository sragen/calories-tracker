package com.company.app.modules.config

import com.company.app.common.exception.AppException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AppConfigService(private val repo: AppConfigRepository) {

    fun getPublicConfigs(): List<PublicConfigResponse> =
        repo.findByIsActiveTrue().map {
            PublicConfigResponse(it.key, it.value, it.type)
        }

    fun getAll(): List<ConfigResponse> =
        repo.findAll().map { it.toResponse() }

    fun getByKey(key: String): ConfigResponse =
        repo.findByKey(key)?.toResponse()
            ?: throw AppException.notFound("Config key '$key' not found")

    @Transactional
    fun update(key: String, req: UpdateConfigRequest, updatedBy: Long): ConfigResponse {
        val config = repo.findByKey(key)
            ?: throw AppException.notFound("Config key '$key' not found")

        config.value = req.value
        req.isActive?.let { config.isActive = it }
        config.updatedAt = LocalDateTime.now()
        config.updatedBy = updatedBy

        return repo.save(config).toResponse()
    }

    @Transactional
    fun toggle(key: String, updatedBy: Long): ConfigResponse {
        val config = repo.findByKey(key)
            ?: throw AppException.notFound("Config key '$key' not found")

        if (config.type != "BOOLEAN") throw AppException.badRequest(
            "Toggle only supported for BOOLEAN configs, got ${config.type}"
        )

        config.value = if (config.value == "true") "false" else "true"
        config.updatedAt = LocalDateTime.now()
        config.updatedBy = updatedBy

        return repo.save(config).toResponse()
    }

    private fun AppConfig.toResponse() = ConfigResponse(
        id = id,
        key = key,
        value = value,
        type = type,
        label = label,
        description = description,
        isActive = isActive
    )
}
