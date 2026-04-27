package com.company.app.ui.aiscan

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.company.app.shared.data.model.AiDetectedFood
import com.company.app.shared.data.model.AiScanResponse
import com.company.app.shared.data.repository.AiScanRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

data class AiScanState(
    val isAnalyzing: Boolean = false,
    val scanResult: AiScanResponse? = null,
    val selectedFoods: List<AiDetectedFood> = emptyList(),
    val isConfirming: Boolean = false,
    val error: String? = null,
    val confirmed: Boolean = false
)

class AiScanViewModel(private val aiScanRepo: AiScanRepository) {

    var state by mutableStateOf(AiScanState())
        private set

    private val scope = CoroutineScope(Dispatchers.Main)

    fun analyze(imageBytes: ByteArray, mimeType: String = "image/jpeg") {
        state = state.copy(isAnalyzing = true, error = null, scanResult = null)
        scope.launch {
            aiScanRepo.analyze(imageBytes, mimeType).fold(
                onSuccess = { result ->
                    state = state.copy(
                        isAnalyzing = false,
                        scanResult = result,
                        selectedFoods = result.detectedFoods
                    )
                },
                onFailure = { e ->
                    state = state.copy(isAnalyzing = false, error = e.message)
                }
            )
        }
    }

    fun toggleFood(food: AiDetectedFood) {
        val current = state.selectedFoods.toMutableList()
        if (current.contains(food)) current.remove(food) else current.add(food)
        state = state.copy(selectedFoods = current)
    }

    fun updatePortion(food: AiDetectedFood, portionG: Double) {
        val updated = state.selectedFoods.map {
            if (it.name == food.name && it.matchedFoodId == food.matchedFoodId)
                it.copy(portionG = portionG, totalCalories = it.caloriesPer100g * portionG / 100.0)
            else it
        }
        state = state.copy(selectedFoods = updated)
    }

    fun confirm(mealType: String, loggedAt: String) {
        val result = state.scanResult ?: return
        if (state.selectedFoods.isEmpty()) return
        state = state.copy(isConfirming = true, error = null)
        scope.launch {
            aiScanRepo.confirm(result.scanLogId, state.selectedFoods, mealType, loggedAt).fold(
                onSuccess = { state = state.copy(isConfirming = false, confirmed = true) },
                onFailure = { e -> state = state.copy(isConfirming = false, error = e.message) }
            )
        }
    }

    fun clearError() { state = state.copy(error = null) }
}
