package com.company.app.shared.data.repository

import com.company.app.shared.data.model.QuickLogRequest
import com.company.app.shared.data.model.QuickLogResponse
import com.company.app.shared.data.model.SaveUserFoodRequest
import com.company.app.shared.data.model.UserFoodResponse
import com.company.app.shared.data.network.ApiService

class UserFoodRepository(private val api: ApiService) {

    suspend fun list(sort: String = "RECENTLY_USED", search: String? = null): Result<List<UserFoodResponse>> =
        runCatching { api.getUserFoods(sort = sort, search = search).content }

    suspend fun save(req: SaveUserFoodRequest): Result<UserFoodResponse> =
        runCatching { api.saveUserFood(req) }

    suspend fun update(id: Long, req: SaveUserFoodRequest): Result<UserFoodResponse> =
        runCatching { api.updateUserFood(id, req) }

    suspend fun quickLog(id: Long, request: QuickLogRequest): Result<QuickLogResponse> =
        runCatching { api.quickLogUserFood(id, request) }

    suspend fun delete(id: Long): Result<Unit> =
        runCatching { api.deleteUserFood(id) }
}
