package com.company.app.ui.myfood

import com.company.app.shared.data.model.QuickLogRequest
import com.company.app.shared.data.model.SaveUserFoodRequest
import com.company.app.shared.data.model.UserFoodResponse
import com.company.app.shared.data.repository.UserFoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

enum class MyFoodSort(val param: String, val label: String) {
    RECENTLY_USED("RECENTLY_USED", "Recently used"),
    MOST_USED("MOST_USED", "Most used"),
    A_Z("ALPHABETICAL", "A–Z"),
}

data class MyFoodsState(
    val foods: List<UserFoodResponse> = emptyList(),
    val query: String = "",
    val sort: MyFoodSort = MyFoodSort.RECENTLY_USED,
    val isLoading: Boolean = false,
    val selectedFood: UserFoodResponse? = null,
    val quickAddMealType: String = "LUNCH",
    val quickAddMultiplier: Float = 1f,
    val isLogging: Boolean = false,
    val logSuccess: Boolean = false,
    val isCreating: Boolean = false,
    val editingFood: UserFoodResponse? = null,
    val isSaving: Boolean = false,
    val error: String? = null,
)

class MyFoodsViewModel(private val repo: UserFoodRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var searchJob: Job? = null

    private val _state = MutableStateFlow(MyFoodsState())
    val state: StateFlow<MyFoodsState> = _state

    init { load() }

    fun load() {
        val s = _state.value
        _state.value = s.copy(isLoading = true, error = null)
        scope.launch {
            repo.list(sort = s.sort.param, search = s.query.takeIf { it.isNotBlank() })
                .onSuccess { _state.value = _state.value.copy(isLoading = false, foods = it) }
                .onFailure { _state.value = _state.value.copy(isLoading = false, error = it.message) }
        }
    }

    fun onSearch(q: String) {
        _state.value = _state.value.copy(query = q)
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300)
            load()
        }
    }

    fun onSort(sort: MyFoodSort) {
        if (_state.value.sort == sort) return
        _state.value = _state.value.copy(sort = sort)
        load()
    }

    fun selectForQuickAdd(food: UserFoodResponse) {
        _state.value = _state.value.copy(
            selectedFood = food,
            quickAddMealType = "LUNCH",
            quickAddMultiplier = 1f,
            logSuccess = false,
        )
    }

    fun dismissQuickAdd() {
        _state.value = _state.value.copy(selectedFood = null, logSuccess = false)
    }

    fun setMealType(type: String) {
        _state.value = _state.value.copy(quickAddMealType = type)
    }

    fun setMultiplier(m: Float) {
        _state.value = _state.value.copy(quickAddMultiplier = m)
    }

    fun quickLog() {
        val s = _state.value
        val food = s.selectedFood ?: return
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        _state.value = s.copy(isLogging = true, error = null)
        scope.launch {
            repo.quickLog(
                id = food.id,
                request = QuickLogRequest(
                    mealType = s.quickAddMealType,
                    portionG = food.servingSizeG * s.quickAddMultiplier,
                    loggedAt = today,
                )
            ).onSuccess {
                _state.value = _state.value.copy(isLogging = false, logSuccess = true, selectedFood = null)
                load()
            }.onFailure {
                _state.value = _state.value.copy(isLogging = false, error = it.message)
            }
        }
    }

    fun deleteFood(id: Long) {
        scope.launch {
            repo.delete(id).onSuccess {
                _state.value = _state.value.copy(
                    foods = _state.value.foods.filter { it.id != id }
                )
            }.onFailure {
                _state.value = _state.value.copy(error = it.message)
            }
        }
    }

    fun openCreate() {
        _state.value = _state.value.copy(isCreating = true, editingFood = null)
    }

    fun openEdit(food: UserFoodResponse) {
        _state.value = _state.value.copy(isCreating = true, editingFood = food)
    }

    fun dismissCreate() {
        _state.value = _state.value.copy(isCreating = false, editingFood = null)
    }

    fun saveFood(name: String, calories: Double, protein: Double, carbs: Double, fat: Double, serving: Double) {
        val s = _state.value
        _state.value = s.copy(isSaving = true, error = null)
        scope.launch {
            val req = SaveUserFoodRequest(
                foodName = name,
                caloriesPer100g = calories,
                proteinPer100g = protein,
                carbsPer100g = carbs,
                fatPer100g = fat,
                servingSizeG = serving,
                source = "MANUAL",
            )
            val result = if (s.editingFood != null) {
                repo.update(s.editingFood.id, req)
            } else {
                repo.save(req)
            }
            result.onSuccess {
                _state.value = _state.value.copy(isSaving = false, isCreating = false, editingFood = null)
                load()
            }.onFailure {
                _state.value = _state.value.copy(isSaving = false, error = it.message)
            }
        }
    }

    fun clearLogSuccess() {
        _state.value = _state.value.copy(logSuccess = false)
    }
}
