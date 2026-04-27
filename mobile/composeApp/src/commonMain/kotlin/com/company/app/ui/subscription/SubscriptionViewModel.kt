package com.company.app.ui.subscription

import com.company.app.shared.data.model.EntitlementResponse
import com.company.app.shared.data.repository.BillingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class SubscriptionState {
    object Loading : SubscriptionState()
    data class Entitled(val source: String, val status: String?, val expiresAt: String?) : SubscriptionState()
    object Paywall : SubscriptionState()
    object Purchasing : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
}

class SubscriptionViewModel(private val billingRepo: BillingRepository) {
    // SupervisorJob so one failed child doesn't cancel siblings; cancel via close()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)

    private val _state = MutableStateFlow<SubscriptionState>(SubscriptionState.Loading)
    val state: StateFlow<SubscriptionState> = _state

    init { checkEntitlement() }

    fun checkEntitlement() {
        _state.value = SubscriptionState.Loading
        scope.launch {
            billingRepo.getEntitlement().fold(
                onSuccess = { _state.value = it.toState() },
                onFailure = { _state.value = SubscriptionState.Error(it.message ?: "Failed to check entitlement") }
            )
        }
    }

    fun purchase() {
        _state.value = SubscriptionState.Purchasing
        scope.launch {
            billingRepo.purchase().fold(
                onSuccess = { _state.value = it.toState() },
                onFailure = { e ->
                    val msg = e.message ?: "Purchase failed"
                    _state.value = if (msg.contains("cancelled", ignoreCase = true))
                        SubscriptionState.Paywall
                    else
                        SubscriptionState.Error(msg)
                }
            )
        }
    }

    fun restore() {
        _state.value = SubscriptionState.Purchasing
        scope.launch {
            billingRepo.restore().fold(
                onSuccess = { _state.value = it.toState() },
                onFailure = { _state.value = SubscriptionState.Error(it.message ?: "Nothing to restore") }
            )
        }
    }

    // Call when the screen owning this ViewModel is permanently destroyed
    fun close() {
        billingRepo.release()
        scope.cancel()
    }

    private fun EntitlementResponse.toState(): SubscriptionState =
        if (entitled) {
            SubscriptionState.Entitled(
                source = source ?: "SUBSCRIPTION",
                status = status,
                expiresAt = expiresAt
            )
        } else {
            SubscriptionState.Paywall
        }
}
