package com.company.app.modules.userfood

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class SaveUserFoodRequest(
    @field:NotBlank val foodName: String,
    @field:DecimalMin("0.0") val caloriesPer100g: Double,
    @field:DecimalMin("0.0") val proteinPer100g: Double = 0.0,
    @field:DecimalMin("0.0") val carbsPer100g: Double = 0.0,
    @field:DecimalMin("0.0") val fatPer100g: Double = 0.0,
    @field:DecimalMin("1.0") val servingSizeG: Double = 100.0,
    val imageUrl: String? = null,
    val source: String = "AI_SCAN",
    val originalFoodId: Long? = null
)

data class QuickLogRequest(
    @field:NotBlank val mealType: String,           // BREAKFAST | LUNCH | DINNER | SNACK
    @field:DecimalMin("1.0") val portionG: Double? = null,  // null → use saved servingSizeG
    @field:NotBlank val loggedAt: String            // YYYY-MM-DD
)

data class UserFoodResponse(
    val id: Long,
    val foodName: String,
    val caloriesPer100g: Double,
    val proteinPer100g: Double,
    val carbsPer100g: Double,
    val fatPer100g: Double,
    val servingSizeG: Double,
    val totalCalories: Double,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double,
    val imageUrl: String?,
    val source: String,
    val useCount: Int,
    val lastUsedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class QuickLogResponse(
    val mealLogId: Long,
    val logged: Boolean,
    // Snapshot values returned so mobile can update Home ring + show toast without re-fetch
    val foodName: String,
    val mealType: String,
    val portionG: Double,
    val caloriesLogged: Double,
    val proteinGLogged: Double,
    val carbsGLogged: Double,
    val fatGLogged: Double
)

fun UserFoodLibrary.toResponse() = UserFoodResponse(
    id = id,
    foodName = foodName,
    caloriesPer100g = caloriesPer100g,
    proteinPer100g = proteinPer100g,
    carbsPer100g = carbsPer100g,
    fatPer100g = fatPer100g,
    servingSizeG = servingSizeG,
    totalCalories = (caloriesPer100g * servingSizeG / 100.0).round1(),
    totalProteinG = (proteinPer100g * servingSizeG / 100.0).round1(),
    totalCarbsG = (carbsPer100g * servingSizeG / 100.0).round1(),
    totalFatG = (fatPer100g * servingSizeG / 100.0).round1(),
    imageUrl = imageUrl,
    source = source,
    useCount = useCount,
    lastUsedAt = lastUsedAt,
    createdAt = createdAt
)

private fun Double.round1() = Math.round(this * 10) / 10.0
