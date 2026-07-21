package com.company.app.ui.aiscan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.company.app.shared.data.model.AiDetectedFood
import com.company.app.shared.data.model.AiScanResponse
import com.company.app.shared.data.model.DailyGoalResponse
import com.company.app.shared.data.model.FoodItem
import com.company.app.shared.data.model.toDetected
import com.company.app.shared.data.repository.AiScanRepository
import com.company.app.shared.data.repository.DailyGoalRepository
import com.company.app.shared.data.repository.FoodRepository
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Which correction pane is showing inside the bottom sheet. */
enum class CorrectionMode { REFINE, SWAP }

/**
 * One editable row in the scan result. [id] is a stable local key so toggle /
 * refine / swap keep working even after a food's name or matchedFoodId changes.
 */
data class ScanItem(
    val id: Int,
    val food: AiDetectedFood,
    val selected: Boolean,
)

data class AiScanState(
    val isAnalyzing: Boolean = false,
    val scanResult: AiScanResponse? = null,
    val items: List<ScanItem> = emptyList(),
    val isConfirming: Boolean = false,
    val error: String? = null,
    val confirmed: Boolean = false,
    val imageBytes: ByteArray? = null,
    val goal: DailyGoalResponse? = null,
    // Correction sheet
    val correctionTargetId: Int? = null,
    val correctionMode: CorrectionMode = CorrectionMode.REFINE,
    val swapQuery: String = "",
    val swapResults: List<FoodItem> = emptyList(),
    val isSearchingSwap: Boolean = false,
) {
    val selectedFoods: List<AiDetectedFood> get() = items.filter { it.selected }.map { it.food }
    val correctionTarget: ScanItem? get() = items.firstOrNull { it.id == correctionTargetId }
}

class AiScanViewModel(
    private val aiScanRepo: AiScanRepository,
    private val goalRepo: DailyGoalRepository,
    private val foodRepo: FoodRepository,
) {

    var state by mutableStateOf(AiScanState())
        private set

    // SupervisorJob + handler so a throw inside an onSuccess lambda doesn't cancel
    // the scope and abort the app on iOS (propagateExceptionFinalResort).
    private val scope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main +
            CoroutineExceptionHandler { _, e ->
                state = state.copy(error = e.message, isSearchingSwap = false, isConfirming = false)
            }
    )
    private var searchJob: Job? = null

    init { loadGoal() }

    private fun loadGoal() {
        scope.launch {
            goalRepo.get().onSuccess { state = state.copy(goal = it) }
        }
    }

    fun analyze(imageBytes: ByteArray, mimeType: String = "image/jpeg") {
        state = AiScanState(isAnalyzing = true, imageBytes = imageBytes, goal = state.goal)
        scope.launch {
            aiScanRepo.analyze(imageBytes, mimeType).fold(
                onSuccess = { result ->
                    state = state.copy(
                        isAnalyzing = false,
                        scanResult = result,
                        items = result.detectedFoods.mapIndexed { i, food ->
                            ScanItem(id = i, food = food, selected = true)
                        },
                    )
                },
                onFailure = { e ->
                    state = state.copy(isAnalyzing = false, error = e.message)
                }
            )
        }
    }

    fun toggleFood(id: Int) {
        state = state.copy(
            items = state.items.map { if (it.id == id) it.copy(selected = !it.selected) else it }
        )
    }

    fun updatePortion(id: Int, portionG: Double) {
        state = state.copy(
            items = state.items.map {
                if (it.id == id) it.copy(
                    food = it.food.copy(
                        portionG = portionG,
                        totalCalories = it.food.caloriesPer100g * portionG / 100.0,
                    )
                ) else it
            }
        )
    }

    // ── Correction sheet ─────────────────────────────────────────────────────

    fun openCorrection(id: Int, mode: CorrectionMode = CorrectionMode.REFINE) {
        searchJob?.cancel()
        state = state.copy(
            correctionTargetId = id,
            correctionMode = mode,
            swapQuery = "",
            swapResults = emptyList(),
            isSearchingSwap = false,
        )
    }

    fun setCorrectionMode(mode: CorrectionMode) {
        state = state.copy(correctionMode = mode)
    }

    fun closeCorrection() {
        searchJob?.cancel()
        state = state.copy(
            correctionTargetId = null,
            swapQuery = "",
            swapResults = emptyList(),
            isSearchingSwap = false,
        )
    }

    fun onSwapQueryChange(query: String) {
        state = state.copy(swapQuery = query)
        searchJob?.cancel()
        if (query.isBlank()) {
            state = state.copy(swapResults = emptyList(), isSearchingSwap = false)
            return
        }
        searchJob = scope.launch {
            delay(300) // debounce
            state = state.copy(isSearchingSwap = true)
            foodRepo.search(q = query).fold(
                onSuccess = { page ->
                    state = state.copy(swapResults = page.content, isSearchingSwap = false)
                },
                onFailure = { e ->
                    state = state.copy(isSearchingSwap = false, error = e.message)
                },
            )
        }
    }

    fun swapFood(id: Int, replacement: FoodItem) {
        searchJob?.cancel()
        state = state.copy(
            items = state.items.map {
                if (it.id == id) it.copy(food = replacement.toDetected(it.food.portionG)) else it
            },
            correctionTargetId = null,
            swapQuery = "",
            swapResults = emptyList(),
            isSearchingSwap = false,
        )
    }

    fun confirm(mealType: String, loggedAt: String) {
        val result = state.scanResult ?: return
        val selected = state.selectedFoods
        if (selected.isEmpty()) return
        state = state.copy(isConfirming = true, error = null)
        scope.launch {
            aiScanRepo.confirm(result.scanLogId, selected, mealType, loggedAt).fold(
                onSuccess = { state = state.copy(isConfirming = false, confirmed = true) },
                onFailure = { e -> state = state.copy(isConfirming = false, error = e.message) }
            )
        }
    }

    fun clearError() { state = state.copy(error = null) }
}
