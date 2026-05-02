package com.company.app.ui.home

import com.company.app.shared.data.model.DailySummary
import com.company.app.shared.data.model.MealLogEntry
import com.company.app.shared.data.repository.AuthRepository
import com.company.app.shared.data.repository.MealLogRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

data class HomeState(
    val isLoading: Boolean = true,
    val diary: DailySummary? = null,
    val streak: Int = 0,
    val userName: String? = null,
    val error: String? = null,
    val isLoggedOut: Boolean = false,
    val deletingId: Long? = null
)

class HomeViewModel(
    private val mealLogRepo: MealLogRepository,
    private val authRepo: AuthRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        loadDiary()
        loadUser()
    }

    fun loadDiary() {
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        _state.value = _state.value.copy(isLoading = true, error = null, deletingId = null)
        scope.launch {
            val diaryResult = mealLogRepo.getDiary(today)
            val streak = mealLogRepo.getStreak().getOrDefault(0)
            diaryResult
                .onSuccess { _state.value = _state.value.copy(isLoading = false, diary = it, streak = streak) }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message, streak = streak) }
        }
    }

    private fun loadUser() {
        scope.launch {
            authRepo.getMe().onSuccess { user ->
                _state.value = _state.value.copy(userName = user.name)
            }
        }
    }

    fun deleteLog(entry: MealLogEntry) {
        _state.value = _state.value.copy(deletingId = entry.id)
        scope.launch {
            mealLogRepo.delete(entry.id)
                .onSuccess { loadDiary() }
                .onFailure { _state.value = _state.value.copy(deletingId = null, error = it.message) }
        }
    }

    fun logout() {
        scope.launch {
            authRepo.logout()
            _state.value = HomeState(isLoggedOut = true)
        }
    }
}
