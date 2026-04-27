package com.company.app.modules.meal

import com.company.app.common.exception.AppException
import com.company.app.modules.food.FoodItemRepository
import com.company.app.modules.goal.DailyGoalService
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime

@Service
class MealLogService(
    private val mealLogRepository: MealLogRepository,
    private val foodItemRepository: FoodItemRepository,
    private val dailyGoalService: DailyGoalService
) {
    fun getDiary(userId: Long, date: LocalDate): DailySummaryResponse {
        val logs = mealLogRepository.findByUserIdAndDate(userId, date)
        val goal = dailyGoalService.getByUserId(userId)

        val total = logs.fold(NutritionTotal()) { acc, log ->
            acc.copy(
                calories = acc.calories + log.caloriesSnapshot,
                protein = acc.protein + log.proteinGSnapshot,
                carbs = acc.carbs + log.carbsGSnapshot,
                fat = acc.fat + log.fatGSnapshot
            )
        }

        val remaining = (goal.targetCalories - total.calories).coerceAtLeast(0.0)
        val percent = if (goal.targetCalories > 0)
            ((total.calories / goal.targetCalories) * 100).toInt().coerceAtMost(100)
        else 0

        val mealMap = mapOf(
            "BREAKFAST" to emptyList<MealLogEntryResponse>(),
            "LUNCH"     to emptyList(),
            "DINNER"    to emptyList(),
            "SNACK"     to emptyList()
        ).toMutableMap()

        logs.forEach { log ->
            val key = log.mealType.takeIf { it in mealMap.keys } ?: "SNACK"
            mealMap[key] = mealMap[key]!! + log.toEntryResponse()
        }

        return DailySummaryResponse(
            date = date,
            summary = NutritionSummary(
                totalCalories = total.calories.round1(),
                totalProteinG = total.protein.round1(),
                totalCarbsG = total.carbs.round1(),
                totalFatG = total.fat.round1(),
                targetCalories = goal.targetCalories,
                remainingCalories = remaining.round1(),
                caloriesPercent = percent
            ),
            meals = mealMap
        )
    }

    fun addLog(userId: Long, req: MealLogRequest): MealLogEntryResponse {
        val food = foodItemRepository.findActiveById(req.foodItemId)
            ?: throw AppException.notFound("Food item not found")

        val factor = req.quantityG / 100.0
        val log = MealLog(
            userId = userId,
            foodItem = food,
            quantityG = req.quantityG,
            mealType = req.mealType,
            caloriesSnapshot = (food.caloriesPer100g * factor).round1(),
            proteinGSnapshot  = (food.proteinPer100g  * factor).round1(),
            carbsGSnapshot    = (food.carbsPer100g    * factor).round1(),
            fatGSnapshot      = (food.fatPer100g      * factor).round1(),
            aiScanPhotoUrl = req.aiScanPhotoUrl,
            loggedAt = req.loggedAt
        )
        return mealLogRepository.save(log).toEntryResponse()
    }

    fun updateLog(userId: Long, logId: Long, req: MealLogUpdateRequest): MealLogEntryResponse {
        val log = mealLogRepository.findActiveById(logId) ?: throw AppException.notFound("Log not found")
        if (log.userId != userId) throw AppException.forbidden()

        req.mealType?.let { log.mealType = it }
        req.quantityG?.let { qty ->
            val factor = qty / 100.0
            log.quantityG = qty
            log.caloriesSnapshot = (log.foodItem.caloriesPer100g * factor).round1()
            log.proteinGSnapshot  = (log.foodItem.proteinPer100g  * factor).round1()
            log.carbsGSnapshot    = (log.foodItem.carbsPer100g    * factor).round1()
            log.fatGSnapshot      = (log.foodItem.fatPer100g      * factor).round1()
        }
        log.updatedAt = LocalDateTime.now()
        return mealLogRepository.save(log).toEntryResponse()
    }

    fun deleteLog(userId: Long, logId: Long) {
        val log = mealLogRepository.findActiveById(logId) ?: throw AppException.notFound("Log not found")
        if (log.userId != userId) throw AppException.forbidden()
        log.deletedAt = LocalDateTime.now()
        mealLogRepository.save(log)
    }

    fun getSummaryRange(userId: Long, from: LocalDate, to: LocalDate): List<DailyRangeResponse> {
        val logs = mealLogRepository.findByUserIdAndDateRange(userId, from, to)
        return logs.groupBy { it.loggedAt }.map { (date, dayLogs) ->
            DailyRangeResponse(
                date = date,
                totalCalories = dayLogs.sumOf { it.caloriesSnapshot }.round1(),
                totalProteinG = dayLogs.sumOf { it.proteinGSnapshot }.round1(),
                totalCarbsG   = dayLogs.sumOf { it.carbsGSnapshot }.round1(),
                totalFatG     = dayLogs.sumOf { it.fatGSnapshot }.round1()
            )
        }.sortedBy { it.date }
    }

    fun getCurrentStreak(userId: Long): Int {
        var streak = 0
        var date = LocalDate.now()
        while (mealLogRepository.countByUserIdAndDate(userId, date) > 0) {
            streak++
            date = date.minusDays(1)
        }
        return streak
    }

    fun exportCsv(userId: Long, from: LocalDate, to: LocalDate): String {
        val logs = mealLogRepository.findByUserIdAndDateRange(userId, from, to)
        val sb = StringBuilder()
        sb.appendLine("Date,Meal Type,Food Name,Quantity (g),Calories,Protein (g),Carbs (g),Fat (g)")
        logs.forEach { log ->
            sb.appendLine(
                "${log.loggedAt},${log.mealType},\"${log.foodItem.name}\"," +
                "${log.quantityG},${log.caloriesSnapshot},${log.proteinGSnapshot}," +
                "${log.carbsGSnapshot},${log.fatGSnapshot}"
            )
        }
        return sb.toString()
    }

    private data class NutritionTotal(
        val calories: Double = 0.0, val protein: Double = 0.0,
        val carbs: Double = 0.0, val fat: Double = 0.0
    )

    private fun Double.round1() = (this * 10).toLong() / 10.0
}
