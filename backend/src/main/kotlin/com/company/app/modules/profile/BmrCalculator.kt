package com.company.app.modules.profile

import java.time.LocalDate
import java.time.Period
import kotlin.math.roundToInt

object BmrCalculator {

    private val activityMultipliers = mapOf(
        "SEDENTARY"          to 1.2,
        "LIGHTLY_ACTIVE"     to 1.375,
        "MODERATELY_ACTIVE"  to 1.55,
        "VERY_ACTIVE"        to 1.725,
        "EXTRA_ACTIVE"       to 1.9
    )

    private val goalAdjustments = mapOf(
        "LOSE"     to -500.0,
        "MAINTAIN" to 0.0,
        "GAIN"     to 300.0
    )

    private val macroRatios = mapOf(
        "LOSE"     to Triple(0.30, 0.40, 0.30),  // protein, carbs, fat
        "MAINTAIN" to Triple(0.25, 0.50, 0.25),
        "GAIN"     to Triple(0.25, 0.55, 0.20)
    )

    fun calculate(
        weightKg: Double,
        heightCm: Double,
        birthDate: LocalDate,
        gender: String,
        activityLevel: String,
        goal: String
    ): BmrResult {
        val age = Period.between(birthDate, LocalDate.now()).years
        val bmr = if (gender == "MALE")
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) + 5
        else
            (10 * weightKg) + (6.25 * heightCm) - (5 * age) - 161

        val multiplier = activityMultipliers[activityLevel] ?: 1.2
        val tdee = bmr * multiplier
        val recommended = (tdee + (goalAdjustments[goal] ?: 0.0)).coerceAtLeast(1200.0)

        val (proteinRatio, carbsRatio, fatRatio) = macroRatios[goal] ?: Triple(0.25, 0.50, 0.25)
        val proteinG = (recommended * proteinRatio / 4).round1()
        val carbsG   = (recommended * carbsRatio / 4).round1()
        val fatG     = (recommended * fatRatio / 9).round1()

        return BmrResult(
            bmrKcal = bmr.round1(),
            tdeeKcal = tdee.round1(),
            recommendedCalories = recommended.round1(),
            recommendedProteinG = proteinG,
            recommendedCarbsG = carbsG,
            recommendedFatG = fatG
        )
    }

    private fun Double.round1() = (this * 10).roundToInt() / 10.0
}

data class BmrResult(
    val bmrKcal: Double,
    val tdeeKcal: Double,
    val recommendedCalories: Double,
    val recommendedProteinG: Double,
    val recommendedCarbsG: Double,
    val recommendedFatG: Double
)
