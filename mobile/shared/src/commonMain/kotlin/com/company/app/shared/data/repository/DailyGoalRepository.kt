package com.company.app.shared.data.repository

import com.company.app.shared.data.model.DailyGoalResponse
import com.company.app.shared.data.network.ApiService

class DailyGoalRepository(private val api: ApiService) {

    suspend fun get(): Result<DailyGoalResponse> = runCatching {
        api.getDailyGoal()
    }
}
