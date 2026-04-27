package com.company.app.shared.data.repository

import com.company.app.shared.data.model.DailyRangeSummary
import com.company.app.shared.data.model.DailySummary
import com.company.app.shared.data.model.MealLogEntry
import com.company.app.shared.data.model.MealLogRequest
import com.company.app.shared.data.model.MealLogUpdateRequest
import com.company.app.shared.data.network.ApiService

class MealLogRepository(private val api: ApiService) {

    suspend fun getDiary(date: String): Result<DailySummary> = runCatching {
        api.getDiary(date)
    }

    suspend fun add(req: MealLogRequest): Result<MealLogEntry> = runCatching {
        api.addMealLog(req)
    }

    suspend fun update(id: Long, req: MealLogUpdateRequest): Result<MealLogEntry> = runCatching {
        api.updateMealLog(id, req)
    }

    suspend fun delete(id: Long): Result<Unit> = runCatching {
        api.deleteMealLog(id)
        Unit
    }

    suspend fun getSummaryRange(from: String, to: String): Result<List<DailyRangeSummary>> =
        runCatching { api.getMealSummaryRange(from, to) }

    suspend fun getStreak(): Result<Int> = runCatching { api.getStreak() }
}
