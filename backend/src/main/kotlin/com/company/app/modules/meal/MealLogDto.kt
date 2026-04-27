package com.company.app.modules.meal

import com.company.app.modules.food.FoodItemResponse
import com.company.app.modules.food.toResponse
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate
import java.time.LocalDateTime

data class MealLogRequest(
    @field:NotNull val foodItemId: Long,
    @field:NotNull @field:DecimalMin("1.0") val quantityG: Double,
    @field:NotBlank val mealType: String,    // BREAKFAST | LUNCH | DINNER | SNACK
    val loggedAt: LocalDate = LocalDate.now(),
    val aiScanPhotoUrl: String? = null
)

data class MealLogUpdateRequest(
    @field:DecimalMin("1.0") val quantityG: Double? = null,
    val mealType: String? = null
)

data class MealLogEntryResponse(
    val id: Long,
    val foodItem: FoodItemResponse,
    val quantityG: Double,
    val mealType: String,
    val calories: Double,
    val proteinG: Double,
    val carbsG: Double,
    val fatG: Double,
    val aiScanPhotoUrl: String?,
    val loggedAt: LocalDate,
    val createdAt: LocalDateTime
)

data class DailySummaryResponse(
    val date: LocalDate,
    val summary: NutritionSummary,
    val meals: Map<String, List<MealLogEntryResponse>>
)

data class NutritionSummary(
    val totalCalories: Double,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double,
    val targetCalories: Double,
    val remainingCalories: Double,
    val caloriesPercent: Int
)

data class DailyRangeResponse(
    val date: LocalDate,
    val totalCalories: Double,
    val totalProteinG: Double,
    val totalCarbsG: Double,
    val totalFatG: Double
)

fun MealLog.toEntryResponse() = MealLogEntryResponse(
    id = id,
    foodItem = foodItem.toResponse(),
    quantityG = quantityG,
    mealType = mealType,
    calories = caloriesSnapshot,
    proteinG = proteinGSnapshot,
    carbsG = carbsGSnapshot,
    fatG = fatGSnapshot,
    aiScanPhotoUrl = aiScanPhotoUrl,
    loggedAt = loggedAt,
    createdAt = createdAt
)
