package com.company.app.modules.profile

import com.company.app.modules.goal.DailyGoalService
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class BodyProfileService(
    private val bodyProfileRepository: BodyProfileRepository,
    private val dailyGoalService: DailyGoalService
) {
    fun getByUserId(userId: Long): BodyProfileResponse? =
        bodyProfileRepository.findByUserId(userId)?.toResponse()

    fun hasProfile(userId: Long): Boolean =
        bodyProfileRepository.findByUserId(userId) != null

    fun upsert(userId: Long, req: BodyProfileRequest): BodyProfileResponse {
        val bmr = BmrCalculator.calculate(
            weightKg = req.weightKg,
            heightCm = req.heightCm,
            birthDate = req.birthDate,
            gender = req.gender,
            activityLevel = req.activityLevel,
            goal = req.goal
        )

        val profile = bodyProfileRepository.findByUserId(userId) ?: BodyProfile(userId = userId, heightCm = req.heightCm, weightKg = req.weightKg, birthDate = req.birthDate, gender = req.gender)

        profile.heightCm = req.heightCm
        profile.weightKg = req.weightKg
        profile.birthDate = req.birthDate
        profile.gender = req.gender
        profile.activityLevel = req.activityLevel
        profile.goal = req.goal
        profile.targetWeightKg = req.targetWeightKg
        profile.bmrKcal = bmr.bmrKcal
        profile.tdeeKcal = bmr.tdeeKcal
        profile.recommendedCalories = bmr.recommendedCalories
        profile.updatedAt = LocalDateTime.now()

        val saved = bodyProfileRepository.save(profile)

        dailyGoalService.syncFromBmr(userId, bmr)

        return saved.toResponse()
    }

    fun preview(req: BodyProfileRequest): BmrPreviewResponse {
        val bmr = BmrCalculator.calculate(
            weightKg = req.weightKg,
            heightCm = req.heightCm,
            birthDate = req.birthDate,
            gender = req.gender,
            activityLevel = req.activityLevel,
            goal = req.goal
        )
        return BmrPreviewResponse(
            bmrKcal = bmr.bmrKcal,
            tdeeKcal = bmr.tdeeKcal,
            recommendedCalories = bmr.recommendedCalories,
            recommendedProteinG = bmr.recommendedProteinG,
            recommendedCarbsG = bmr.recommendedCarbsG,
            recommendedFatG = bmr.recommendedFatG
        )
    }
}
