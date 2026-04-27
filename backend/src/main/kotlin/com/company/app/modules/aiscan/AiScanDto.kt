package com.company.app.modules.aiscan

data class AiDetectedFood(
    val name: String,
    val portionG: Double,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val matchedFoodId: Long?,       // null if no match in local DB
    val totalCalories: Double = caloriesPer100g * portionG / 100.0
)

data class AiScanResponse(
    val scanLogId: Long,
    val imageUrl: String?,
    val detectedFoods: List<AiDetectedFood>
)

data class AiScanConfirmRequest(
    val scanLogId: Long,
    val selectedFoods: List<ConfirmFoodItem>,
    val mealType: String,
    val loggedAt: String          // YYYY-MM-DD
)

data class ConfirmFoodItem(
    val name: String,
    val portionG: Double,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val matchedFoodId: Long?
)
