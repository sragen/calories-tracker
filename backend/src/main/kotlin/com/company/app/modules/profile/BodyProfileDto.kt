package com.company.app.modules.profile

import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Past
import java.time.LocalDate
import java.time.LocalDateTime

data class BodyProfileRequest(
    @field:NotNull @field:DecimalMin("50.0") @field:DecimalMax("300.0")
    val heightCm: Double,

    @field:NotNull @field:DecimalMin("10.0") @field:DecimalMax("500.0")
    val weightKg: Double,

    @field:NotNull @field:Past
    val birthDate: LocalDate,

    @field:NotBlank
    val gender: String,           // MALE | FEMALE

    @field:NotBlank
    val activityLevel: String,    // SEDENTARY | LIGHTLY_ACTIVE | MODERATELY_ACTIVE | VERY_ACTIVE | EXTRA_ACTIVE

    @field:NotBlank
    val goal: String,             // LOSE | MAINTAIN | GAIN

    val targetWeightKg: Double? = null
)

data class BodyProfileResponse(
    val heightCm: Double,
    val weightKg: Double,
    val birthDate: LocalDate,
    val gender: String,
    val activityLevel: String,
    val goal: String,
    val targetWeightKg: Double?,
    val bmrKcal: Double?,
    val tdeeKcal: Double?,
    val recommendedCalories: Double?,
    val updatedAt: LocalDateTime
)

data class BmrPreviewResponse(
    val bmrKcal: Double,
    val tdeeKcal: Double,
    val recommendedCalories: Double,
    val recommendedProteinG: Double,
    val recommendedCarbsG: Double,
    val recommendedFatG: Double
)

fun BodyProfile.toResponse() = BodyProfileResponse(
    heightCm = heightCm,
    weightKg = weightKg,
    birthDate = birthDate,
    gender = gender,
    activityLevel = activityLevel,
    goal = goal,
    targetWeightKg = targetWeightKg,
    bmrKcal = bmrKcal,
    tdeeKcal = tdeeKcal,
    recommendedCalories = recommendedCalories,
    updatedAt = updatedAt
)
