package com.company.app.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(@Value("\${app.name}") private val appName: String) {

    @Bean
    fun openApi(): OpenAPI = OpenAPI()
        .info(Info()
            .title("$appName API")
            .description("Backend REST API")
            .version("1.0.0"))
        .addSecurityItem(SecurityRequirement().addList("Bearer Auth"))
        .components(Components()
            .addSecuritySchemes("Bearer Auth",
                SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("Masukkan JWT token dari /api/auth/login")))
}
