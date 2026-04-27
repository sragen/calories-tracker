package com.company.app.ui.analytics

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.company.app.shared.data.model.DailyRangeSummary
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
    val error: String? = null
)

class AnalyticsViewModel(
    private val mealLogRepo: MealLogRepository,
    private val subRepo: SubscriptionRepository
) {
    var state by mutableStateOf(AnalyticsState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    init { load() }

    private fun load() {
        scope.launch {
            val premiumResult = subRepo.getStatus()
            val isPremium = premiumResult.getOrNull()?.isPremium == true
            state = state.copy(isPremium = isPremium)

            if (!isPremium) {
                state = state.copy(isLoading = false)
                return@launch
            }

            val tz = TimeZone.currentSystemDefault()
            val today = Clock.System.todayIn(tz)
            val from = today.minus(DatePeriod(days = 6))

            mealLogRepo.getSummaryRange(from.toString(), today.toString()).fold(
                onSuccess = { data ->
                    val n = data.size.coerceAtLeast(1)
                    state = state.copy(
                        isLoading = false,
                        weeklyData = data,
                        avgCalories = data.sumOf { it.totalCalories } / n,
                        avgProtein  = data.sumOf { it.totalProteinG } / n,
                        avgCarbs    = data.sumOf { it.totalCarbsG } / n,
                        avgFat      = data.sumOf { it.totalFatG } / n
                    )
                },
                onFailure = { e ->
                    state = state.copy(isLoading = false, error = e.message)
                }
            )
        }
    }

    fun refresh() {
        state = AnalyticsState()
        load()
    }
}
