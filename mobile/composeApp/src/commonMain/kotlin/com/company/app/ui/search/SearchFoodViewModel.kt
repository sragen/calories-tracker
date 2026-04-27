package com.company.app.ui.search

import com.company.app.shared.data.model.FoodCategory
import com.company.app.shared.data.model.FoodItem
import com.company.app.shared.data.model.MealLogRequest
import com.company.app.shared.data.repository.FoodRepository
import com.company.app.shared.data.repository.MealLogRepository
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

data class SearchFoodState(
    val query: String = "",
    val results: List<FoodItem> = emptyList(),
    val recentFoods: List<FoodItem> = emptyList(),
    val categories: List<FoodCategory> = emptyList(),
    val selectedCategoryId: Long? = null,
    val isSearching: Boolean = false,
    val isLoadingMore: Boolean = false,
    val hasMorePages: Boolean = false,
    val currentPage: Int = 0,
    val selectedFood: FoodItem? = null,
    val mealType: String = "SNACK",
    val quantityG: String = "",
    val isLogging: Boolean = false,
    val logSuccess: Boolean = false,
    val error: String? = null
)

class SearchFoodViewModel(
    private val foodRepo: FoodRepository,
    private val mealLogRepo: MealLogRepository
) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private var searchJob: Job? = null

    private val _state = MutableStateFlow(SearchFoodState())
    val state: StateFlow<SearchFoodState> = _state

    init {
        loadCategories()
        loadRecentFoods()
        search()
    }

    private fun loadCategories() {
        scope.launch {
            foodRepo.getCategories()
                .onSuccess { _state.value = _state.value.copy(categories = it) }
        }
    }

    private fun loadRecentFoods() {
        scope.launch {
            val recent = foodRepo.getRecentFoods()
            _state.value = _state.value.copy(recentFoods = recent)
        }
    }

    fun onQueryChanged(q: String) {
        _state.value = _state.value.copy(query = q, currentPage = 0)
        searchJob?.cancel()
        searchJob = scope.launch {
            delay(300)
            search()
        }
    }

    fun onCategorySelected(categoryId: Long?) {
        _state.value = _state.value.copy(selectedCategoryId = categoryId, currentPage = 0)
        search()
    }

    fun search() {
        val s = _state.value
        _state.value = s.copy(isSearching = true, error = null)
        scope.launch {
            foodRepo.search(
                q = s.query.takeIf { it.isNotBlank() },
                categoryId = s.selectedCategoryId,
                page = 0
            ).onSuccess { page ->
                _state.value = _state.value.copy(
                    isSearching = false,
                    results = page.content,
                    currentPage = 0,
                    hasMorePages = page.page.number + 1 < page.page.totalPages
                )
            }.onFailure {
                _state.value = _state.value.copy(isSearching = false, error = it.message)
            }
        }
    }

    fun loadMore() {
        val s = _state.value
        if (!s.hasMorePages || s.isLoadingMore) return
        val nextPage = s.currentPage + 1
        _state.value = s.copy(isLoadingMore = true)
        scope.launch {
            foodRepo.search(
                q = s.query.takeIf { it.isNotBlank() },
                categoryId = s.selectedCategoryId,
                page = nextPage
            ).onSuccess { page ->
                _state.value = _state.value.copy(
                    isLoadingMore = false,
                    results = _state.value.results + page.content,
                    currentPage = nextPage,
                    hasMorePages = page.page.number + 1 < page.page.totalPages
                )
            }.onFailure {
                _state.value = _state.value.copy(isLoadingMore = false, error = it.message)
            }
        }
    }

    fun selectFood(food: FoodItem, mealType: String) {
        _state.value = _state.value.copy(
            selectedFood = food,
            mealType = mealType,
            quantityG = food.defaultServingG.toInt().toString()
        )
    }

    fun dismissFoodDetail() {
        _state.value = _state.value.copy(selectedFood = null, quantityG = "")
    }

    fun onQuantityChanged(v: String) {
        _state.value = _state.value.copy(quantityG = v)
    }

    fun onMealTypeChanged(v: String) {
        _state.value = _state.value.copy(mealType = v)
    }

    fun logFood() {
        val s = _state.value
        val food = s.selectedFood ?: return
        val qty = s.quantityG.toDoubleOrNull()
        if (qty == null || qty <= 0) {
            _state.value = s.copy(error = "Invalid quantity")
            return
        }
        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        _state.value = s.copy(isLogging = true, error = null)
        scope.launch {
            mealLogRepo.add(
                MealLogRequest(
                    foodItemId = food.id,
                    quantityG = qty,
                    mealType = s.mealType,
                    loggedAt = today
                )
            ).onSuccess {
                foodRepo.addToRecent(food)
                _state.value = _state.value.copy(isLogging = false, logSuccess = true, selectedFood = null)
            }.onFailure {
                _state.value = _state.value.copy(isLogging = false, error = it.message)
            }
        }
    }
}
