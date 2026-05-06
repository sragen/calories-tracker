package com.company.app.modules.goal

import com.company.app.common.exception.AppException
import com.company.app.modules.profile.BmrCalculator
import com.company.app.modules.profile.BmrResult
import com.company.app.modules.profile.BodyProfileRepository
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DailyGoalService(
    private val dailyGoalRepository: DailyGoalRepository,
    private val bodyProfileRepository: BodyProfileRepository
) {

    fun getByUserId(userId: Long): DailyGoalResponse =
        (dailyGoalRepository.findByUserId(userId) ?: defaultGoal(userId)).toResponse()

    fun update(userId: Long, req: DailyGoalRequest): DailyGoalResponse {
        val goal = dailyGoalRepository.findByUserId(userId) ?: DailyGoal(
            userId = userId,
            targetCalories = req.targetCalories
        )
        goal.targetCalories = req.targetCalories
        goal.targetProteinG = req.targetProteinG
        goal.targetCarbsG = req.targetCarbsG
        goal.targetFatG = req.targetFatG
        goal.autoCalculated = false
        goal.updatedAt = LocalDateTime.now()
        return dailyGoalRepository.save(goal).toResponse()
    }

    fun reset(userId: Long): DailyGoalResponse =
        (dailyGoalRepository.findByUserId(userId) ?: throw AppException.notFound("No body profile found")).also {
            it.autoCalculated = true
            it.updatedAt = LocalDateTime.now()
            dailyGoalRepository.save(it)
        }.toResponse()

    fun syncFromBmr(userId: Long, bmr: BmrResult) {
        val goal = dailyGoalRepository.findByUserId(userId) ?: DailyGoal(
            userId = userId,
            targetCalories = bmr.recommendedCalories
        )
        if (goal.autoCalculated) {
            goal.targetCalories = bmr.recommendedCalories
            goal.targetProteinG = bmr.recommendedProteinG
            goal.targetCarbsG = bmr.recommendedCarbsG
            goal.targetFatG = bmr.recommendedFatG
            goal.updatedAt = LocalDateTime.now()
            dailyGoalRepository.save(goal)
        }
    }

    fun getPresets(userId: Long): GoalPresetsResponse {
        val profile = bodyProfileRepository.findByUserId(userId)
            ?: throw AppException.badRequest("Body profile belum diisi. Lengkapi data tubuh terlebih dahulu.")

        val presetTypes = listOf(
            "LOSE"     to "Cut",
            "MAINTAIN" to "Maintain",
            "GAIN"     to "Bulk"
        )

        val presets = presetTypes.map { (goal, label) ->
            val bmr = BmrCalculator.calculate(
                weightKg = profile.weightKg,
                heightCm = profile.heightCm,
                birthDate = profile.birthDate,
                gender = profile.gender,
                activityLevel = profile.activityLevel,
                goal = goal
            )
            GoalPreset(
                type = goal,
                label = label,
                adjustmentPercent = BmrCalculator.goalAdjustmentPercents[goal] ?: 0,
                tdeeKcal = bmr.tdeeKcal,
                targetCalories = bmr.recommendedCalories,
                targetProteinG = bmr.recommendedProteinG,
                targetCarbsG = bmr.recommendedCarbsG,
                targetFatG = bmr.recommendedFatG
            )
        }

        val tdeeKcal = presets.first { it.type == "MAINTAIN" }.tdeeKcal
        return GoalPresetsResponse(
            tdeeKcal = tdeeKcal,
            presets = presets,
            current = getByUserId(userId)
        )
    }

    private fun defaultGoal(userId: Long) = DailyGoal(
        userId = userId, targetCalories = 2000.0,
        targetProteinG = 125.0, targetCarbsG = 250.0, targetFatG = 55.0
    )
}
