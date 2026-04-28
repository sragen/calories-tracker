package com.company.app.ui.profile

import com.company.app.shared.data.model.BodyProfileResponse
import com.company.app.shared.data.model.DailyGoalResponse
import com.company.app.shared.data.model.EntitlementResponse
import com.company.app.shared.data.repository.AuthRepository
import com.company.app.shared.data.repository.BodyProfileRepository
import com.company.app.shared.data.repository.DailyGoalRepository
import com.company.app.shared.data.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ProfileState(
    val isLoading: Boolean = true,
    val profile: BodyProfileResponse? = null,
    val goal: DailyGoalResponse? = null,
    val entitlement: EntitlementResponse? = null,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

class ProfileViewModel(
    private val bodyProfileRepo: BodyProfileRepository,
    private val dailyGoalRepo: DailyGoalRepository,
    private val authRepo: AuthRepository,
    private val subscriptionRepo: SubscriptionRepository
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
            _state.value = ProfileState(isLoading = false, profile = profile, goal = goal, entitlement = entitlement)
        }
    }

    fun logout() {
        scope.launch {
            authRepo.logout()
            _state.value = _state.value.copy(isLoggedOut = true)
        }
    }
}
