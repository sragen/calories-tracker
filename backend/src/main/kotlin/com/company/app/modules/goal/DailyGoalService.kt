package com.company.app.modules.goal

import com.company.app.common.exception.AppException
import com.company.app.modules.profile.BmrResult
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class DailyGoalService(private val dailyGoalRepository: DailyGoalRepository) {

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

    private fun defaultGoal(userId: Long) = DailyGoal(
        userId = userId, targetCalories = 2000.0,
        targetProteinG = 125.0, targetCarbsG = 250.0, targetFatG = 55.0
    )
}
