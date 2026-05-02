package com.company.app.ui.analytics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.company.app.shared.data.model.DailyRangeSummary
import com.company.app.shared.data.repository.DailyGoalRepository
import com.company.app.shared.data.repository.MealLogRepository
import com.company.app.shared.data.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

data class AnalyticsState(
    val isLoading: Boolean = true,
    val isPremium: Boolean = false,
    val weeklyData: List<DailyRangeSummary> = emptyList(),
    val avgCalories: Double = 0.0,
    val avgProtein: Double = 0.0,
    val avgCarbs: Double = 0.0,
    val avgFat: Double = 0.0,
    val targetCalories: Double = 0.0,
    val error: String? = null
)

class AnalyticsViewModel(
    private val mealLogRepo: MealLogRepository,
    private val subRepo: SubscriptionRepository,
    private val goalRepo: DailyGoalRepository,
) {
    var state by mutableStateOf(AnalyticsState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    init { load() }

    private fun load() {
        scope.launch {
            val premiumResult = subRepo.getEntitlement()
            val isPremium = premiumResult.getOrNull()?.entitled == true
            state = state.copy(isPremium = isPremium)

            if (!isPremium) {
                state = state.copy(isLoading = false)
                return@launch
            }

            val tz = TimeZone.currentSystemDefault()
            val today = Clock.System.todayIn(tz)
            val from = today.minus(DatePeriod(days = 6))

            val goal = goalRepo.get().getOrNull()
            mealLogRepo.getSummaryRange(from.toString(), today.toString()).fold(
                onSuccess = { data ->
                    val active = data.count { it.totalCalories > 0 }.coerceAtLeast(1)
                    state = state.copy(
                        isLoading = false,
                        weeklyData = data,
                        avgCalories = data.sumOf { it.totalCalories } / active,
                        avgProtein  = data.sumOf { it.totalProteinG } / active,
                        avgCarbs    = data.sumOf { it.totalCarbsG } / active,
                        avgFat      = data.sumOf { it.totalFatG } / active,
                        targetCalories = goal?.targetCalories ?: 0.0,
                    )
                },
                onFailure = { e ->
                    state = state.copy(isLoading = false, error = e.message, targetCalories = goal?.targetCalories ?: 0.0)
                }
            )
        }
    }

    fun refresh() {
        state = AnalyticsState()
        load()
    }
}
