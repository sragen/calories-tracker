package com.company.app.shared.data.repository

import com.company.app.shared.data.model.FoodCategory
import com.company.app.shared.data.model.FoodItem
import com.company.app.shared.data.model.FoodPage
import com.company.app.shared.data.model.FoodSubmitRequest
import com.company.app.shared.data.network.ApiService
import com.company.app.shared.storage.RecentFoodStorage

class FoodRepository(
    private val api: ApiService,
    private val recentStorage: RecentFoodStorage
) {

    suspend fun search(
        q: String? = null,
        categoryId: Long? = null,
        page: Int = 0
    ): Result<FoodPage> = runCatching {
        api.searchFoods(q, categoryId, page)
    }

    suspend fun getById(id: Long): Result<FoodItem> = runCatching {
        api.getFoodById(id)
    }

    suspend fun getByBarcode(barcode: String): Result<FoodItem> = runCatching {
        api.getFoodByBarcode(barcode)
    }

    suspend fun getCategories(): Result<List<FoodCategory>> = runCatching {
        api.getFoodCategories()
    }

    suspend fun submit(req: FoodSubmitRequest): Result<FoodItem> = runCatching {
        api.submitFood(req)
    }

    suspend fun getRecentFoods(): List<FoodItem> {
        val ids = recentStorage.getRecentIds()
        return ids.mapNotNull { id -> api.getFoodById(id).let { runCatching { it }.getOrNull() } }
    }

    suspend fun addToRecent(food: FoodItem) {
        recentStorage.addRecent(food.id)
    }
}
