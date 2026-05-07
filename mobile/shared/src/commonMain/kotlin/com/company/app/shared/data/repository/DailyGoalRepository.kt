package com.company.app.shared.data.repository

import com.company.app.shared.data.model.DailyGoalRequest
import com.company.app.shared.data.model.DailyGoalResponse
import com.company.app.shared.data.model.GoalPresetsResponse
import com.company.app.shared.data.network.ApiService

class DailyGoalRepository(private val api: ApiService) {

    suspend fun get(): Result<DailyGoalResponse> = runCatching {
        api.getDailyGoal()
    }

    suspend fun update(request: DailyGoalRequest): Result<DailyGoalResponse> = runCatching {
        api.updateDailyGoal(request)
    }

    suspend fun getPresets(): Result<GoalPresetsResponse> = runCatching {
        api.getGoalPresets()
    }

    suspend fun reset(): Result<DailyGoalResponse> = runCatching {
        api.resetDailyGoal()
    }
}
