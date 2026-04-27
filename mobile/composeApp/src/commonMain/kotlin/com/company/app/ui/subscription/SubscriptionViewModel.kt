package com.company.app.ui.subscription

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.company.app.shared.data.model.PremiumStatusResponse
import com.company.app.shared.data.model.SnapTokenResponse
import com.company.app.shared.data.model.SubscriptionPlan
import com.company.app.shared.data.repository.SubscriptionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class SubscriptionState(
    val isLoading: Boolean = true,
    val plans: List<SubscriptionPlan> = emptyList(),
    val status: PremiumStatusResponse? = null,
    val isPurchasing: Boolean = false,
    val snapToken: SnapTokenResponse? = null,
    val error: String? = null
)

class SubscriptionViewModel(private val repo: SubscriptionRepository) {

    var state by mutableStateOf(SubscriptionState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    init { load() }

    private fun load() {
        scope.launch {
            val plansResult = repo.getPlans()
            val statusResult = repo.getStatus()
            state = state.copy(
                isLoading = false,
                plans = plansResult.getOrDefault(emptyList()),
                status = statusResult.getOrNull()
            )
        }
    }

    fun purchase(planId: Long) {
        state = state.copy(isPurchasing = true, error = null, snapToken = null)
        scope.launch {
            repo.purchase(planId).fold(
                onSuccess = { token -> state = state.copy(isPurchasing = false, snapToken = token) },
                onFailure = { e -> state = state.copy(isPurchasing = false, error = e.message) }
            )
        }
    }

    fun clearSnapToken() { state = state.copy(snapToken = null) }
    fun clearError() { state = state.copy(error = null) }
}
