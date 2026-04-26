package com.company.app.modules.config

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/config")
@Tag(name = "Config (Public)")
class ConfigController(private val service: AppConfigService) {

    @GetMapping
    @Operation(summary = "Fetch all active configs — called by mobile app at startup")
    fun getPublicConfigs(): List<PublicConfigResponse> = service.getPublicConfigs()
}
