package com.company.app.ui.home

import com.company.app.shared.data.model.AppConfigs
import com.company.app.shared.data.repository.AuthRepository
import com.company.app.shared.data.repository.ConfigRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class HomeState(
    val isLoading: Boolean = true,
    val configs: AppConfigs? = null,
    val error: String? = null,
    val isLoggedOut: Boolean = false
)

class HomeViewModel(
    private val configRepo: ConfigRepository,
    private val authRepo: AuthRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init { loadConfigs() }

    fun loadConfigs() {
        _state.value = HomeState(isLoading = true)
        scope.launch {
            configRepo.fetchConfigs()
                .onSuccess { _state.value = HomeState(isLoading = false, configs = it) }
                .onFailure { _state.value = HomeState(isLoading = false, error = it.message) }
        }
    }

    fun logout() {
        scope.launch {
            authRepo.logout()
            _state.value = HomeState(isLoggedOut = true)
        }
    }
}
