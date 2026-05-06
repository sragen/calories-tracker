package com.company.app.modules.goal

import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class DailyGoalResponse(
    val targetCalories: Double,
    val targetProteinG: Double,
    val targetCarbsG: Double,
    val targetFatG: Double,
    val autoCalculated: Boolean,
    val updatedAt: LocalDateTime
)

data class DailyGoalRequest(
    @field:NotNull @field:DecimalMin("500.0") val targetCalories: Double,
    @field:DecimalMin("0.0") val targetProteinG: Double = 0.0,
    @field:DecimalMin("0.0") val targetCarbsG: Double = 0.0,
    @field:DecimalMin("0.0") val targetFatG: Double = 0.0
)

data class GoalPreset(
    val type: String,
    val label: String,
    val adjustmentPercent: Int,
    val tdeeKcal: Double,
    val targetCalories: Double,
    val targetProteinG: Double,
    val targetCarbsG: Double,
    val targetFatG: Double
)

data class GoalPresetsResponse(
    val tdeeKcal: Double,
    val presets: List<GoalPreset>,
    val current: DailyGoalResponse
)

fun DailyGoal.toResponse() = DailyGoalResponse(
    targetCalories = targetCalories,
    targetProteinG = targetProteinG,
    targetCarbsG = targetCarbsG,
    targetFatG = targetFatG,
    autoCalculated = autoCalculated,
    updatedAt = updatedAt
)
