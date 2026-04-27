package com.company.app.ui.submit

import com.company.app.shared.data.model.FoodSubmitRequest
import com.company.app.shared.data.repository.FoodRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class SubmitFoodState(
    val name: String = "",
    val nameEn: String = "",
    val caloriesPer100g: String = "",
    val proteinPer100g: String = "0",
    val carbsPer100g: String = "0",
    val fatPer100g: String = "0",
    val fiberPer100g: String = "",
    val defaultServingG: String = "100",
    val barcode: String = "",
    val isSubmitting: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null
)

class SubmitFoodViewModel(private val foodRepo: FoodRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(SubmitFoodState())
    val state: StateFlow<SubmitFoodState> = _state

    fun initWithBarcode(barcode: String) {
        _state.value = _state.value.copy(barcode = barcode)
    }

    fun onNameChanged(v: String) { _state.value = _state.value.copy(name = v) }
    fun onNameEnChanged(v: String) { _state.value = _state.value.copy(nameEn = v) }
    fun onCaloriesChanged(v: String) { _state.value = _state.value.copy(caloriesPer100g = v) }
    fun onProteinChanged(v: String) { _state.value = _state.value.copy(proteinPer100g = v) }
    fun onCarbsChanged(v: String) { _state.value = _state.value.copy(carbsPer100g = v) }
    fun onFatChanged(v: String) { _state.value = _state.value.copy(fatPer100g = v) }
    fun onFiberChanged(v: String) { _state.value = _state.value.copy(fiberPer100g = v) }
    fun onServingChanged(v: String) { _state.value = _state.value.copy(defaultServingG = v) }

    fun submit() {
        val s = _state.value
        if (s.name.isBlank()) { _state.value = s.copy(error = "Food name is required"); return }
        val calories = s.caloriesPer100g.toDoubleOrNull()
        if (calories == null || calories < 0) { _state.value = s.copy(error = "Valid calories required"); return }

        _state.value = s.copy(isSubmitting = true, error = null)
        scope.launch {
            foodRepo.submit(
                FoodSubmitRequest(
                    name = s.name.trim(),
                    nameEn = s.nameEn.takeIf { it.isNotBlank() },
                    caloriesPer100g = calories,
                    proteinPer100g = s.proteinPer100g.toDoubleOrNull() ?: 0.0,
                    carbsPer100g = s.carbsPer100g.toDoubleOrNull() ?: 0.0,
                    fatPer100g = s.fatPer100g.toDoubleOrNull() ?: 0.0,
                    fiberPer100g = s.fiberPer100g.toDoubleOrNull(),
                    defaultServingG = s.defaultServingG.toDoubleOrNull() ?: 100.0,
                    barcode = s.barcode.takeIf { it.isNotBlank() }
                )
            ).onSuccess {
                _state.value = _state.value.copy(isSubmitting = false, isSuccess = true)
            }.onFailure {
                _state.value = _state.value.copy(isSubmitting = false, error = it.message)
            }
        }
    }
}
