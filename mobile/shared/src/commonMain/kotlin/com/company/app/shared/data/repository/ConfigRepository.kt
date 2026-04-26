package com.company.app.shared.data.repository

import com.company.app.shared.data.model.AppConfigs
import com.company.app.shared.data.network.ApiService

class ConfigRepository(private val api: ApiService) {
    suspend fun fetchConfigs(): Result<AppConfigs> = runCatching {
        AppConfigs.from(api.getConfigs())
    }
}
