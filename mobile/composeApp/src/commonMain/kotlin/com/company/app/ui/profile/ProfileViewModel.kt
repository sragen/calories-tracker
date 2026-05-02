package com.company.app.ui.profile

import com.company.app.shared.data.model.BodyProfileResponse
import com.company.app.shared.data.model.DailyGoalResponse
import com.company.app.shared.data.model.EntitlementResponse
import com.company.app.shared.data.repository.AuthRepository
import com.company.app.shared.data.repository.BodyProfileRepository
import com.company.app.shared.data.repository.DailyGoalRepository
import com.company.app.shared.data.repository.MealLogRepository
import com.company.app.shared.data.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.todayIn

data class ProfileState(
    val isLoading: Boolean = true,
    val profile: BodyProfileResponse? = null,
    val goal: DailyGoalResponse? = null,
    val entitlement: EntitlementResponse? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val activeDays: Int = 0,
    val avgKcal: Int = 0,
    val streak: Int = 0,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
)

class ProfileViewModel(
    private val bodyProfileRepo: BodyProfileRepository,
    private val dailyGoalRepo: DailyGoalRepository,
    private val authRepo: AuthRepository,
    private val subscriptionRepo: SubscriptionRepository,
    private val mealLogRepo: MealLogRepository,
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(ProfileState())
    val state: StateFlow<ProfileState> = _state

    init { load() }

    fun load() {
        _state.value = ProfileState(isLoading = true)
        scope.launch {
            val profile = bodyProfileRepo.get().getOrNull()
            val goal = dailyGoalRepo.get().getOrNull()
            val entitlement = subscriptionRepo.getEntitlement().getOrNull()
            val user = authRepo.getMe().getOrNull()
            val streak = mealLogRepo.getStreak().getOrDefault(0)

            val tz = TimeZone.currentSystemDefault()
            val today = Clock.System.todayIn(tz)
            val from = today.minus(DatePeriod(days = 6))
            val weekly = mealLogRepo.getSummaryRange(from.toString(), today.toString()).getOrNull().orEmpty()
            val activeDays = weekly.count { it.totalCalories > 0 }
            val avgKcal = if (activeDays > 0) (weekly.sumOf { it.totalCalories } / activeDays).toInt() else 0

            _state.value = ProfileState(
                isLoading = false,
                profile = profile,
                goal = goal,
                entitlement = entitlement,
                userName = user?.name,
                userEmail = user?.email,
                activeDays = activeDays,
                avgKcal = avgKcal,
                streak = streak,
            )
        }
    }

    fun logout() {
        scope.launch {
            authRepo.logout()
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }
}
