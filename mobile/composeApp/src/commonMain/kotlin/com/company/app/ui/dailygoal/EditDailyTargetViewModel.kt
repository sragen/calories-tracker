package com.company.app.ui.dailygoal

import com.company.app.shared.data.model.DailyGoalRequest
import com.company.app.shared.data.model.GoalPreset
import com.company.app.shared.data.repository.DailyGoalRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

enum class EditingField { CALORIES, PROTEIN, CARBS, FAT }

data class EditDailyTargetState(
    val isLoading: Boolean = true,
    val tdeeKcal: Double = 2000.0,
    val presets: List<GoalPreset> = emptyList(),
    val selectedPresetType: String? = null,
    val calories: Double = 2000.0,
    val proteinG: Double = 150.0,
    val carbsG: Double = 200.0,
    val fatG: Double = 65.0,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val editingField: EditingField? = null,
    val keypadBuffer: String = "",
)

class EditDailyTargetViewModel(
    private val dailyGoalRepo: DailyGoalRepository,
) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(EditDailyTargetState())
    val state: StateFlow<EditDailyTargetState> = _state

    init { load() }

    fun load() {
        _state.value = EditDailyTargetState(isLoading = true)
        scope.launch {
            dailyGoalRepo.getPresets()
                .onSuccess { response ->
                    val current = response.current
                    val matchedType = response.presets.find { preset ->
                        kotlin.math.abs(preset.targetCalories - current.targetCalories) < 50.0
                    }?.type
                    _state.value = EditDailyTargetState(
                        isLoading = false,
                        tdeeKcal = response.tdeeKcal,
                        presets = response.presets,
                        selectedPresetType = matchedType,
                        calories = current.targetCalories,
                        proteinG = current.targetProteinG,
                        carbsG = current.targetCarbsG,
                        fatG = current.targetFatG,
                    )
                }
                .onFailure { err ->
                    _state.value = EditDailyTargetState(
                        isLoading = false,
                        error = err.message,
                    )
                }
        }
    }

    fun selectPreset(preset: GoalPreset) {
        _state.value = _state.value.copy(
            selectedPresetType = preset.type,
            calories = preset.targetCalories,
            proteinG = preset.targetProteinG,
            carbsG = preset.targetCarbsG,
            fatG = preset.targetFatG,
        )
    }

    fun openKeypad(field: EditingField) {
        val current = when (field) {
            EditingField.CALORIES -> _state.value.calories.toInt().toString()
            EditingField.PROTEIN  -> _state.value.proteinG.toInt().toString()
            EditingField.CARBS    -> _state.value.carbsG.toInt().toString()
            EditingField.FAT      -> _state.value.fatG.toInt().toString()
        }
        _state.value = _state.value.copy(editingField = field, keypadBuffer = current)
    }

    fun closeKeypad() {
        _state.value = _state.value.copy(editingField = null, keypadBuffer = "")
    }

    fun onKeypadInput(key: String) {
        val buf = _state.value.keypadBuffer
        val next = when (key) {
            "⌫" -> if (buf.isEmpty()) "" else buf.dropLast(1)
            "." -> if ("." in buf) buf else "$buf."
            else -> if (buf.length >= 5) buf else "$buf$key"
        }
        _state.value = _state.value.copy(keypadBuffer = next)
    }

    fun quickAdjust(delta: Int) {
        val buf = _state.value.keypadBuffer
        val current = buf.toDoubleOrNull() ?: return
        val isCalories = _state.value.editingField == EditingField.CALORIES
        val next = (current + delta).coerceIn(
            if (isCalories) 500.0 else 0.0,
            if (isCalories) 9999.0 else 800.0,
        )
        _state.value = _state.value.copy(keypadBuffer = next.toInt().toString())
    }

    fun confirmKeypad() {
        val s = _state.value
        val value = s.keypadBuffer.toDoubleOrNull() ?: return
        _state.value = when (s.editingField) {
            EditingField.CALORIES -> s.copy(
                calories = value.coerceIn(500.0, 9999.0),
                editingField = null, selectedPresetType = null,
            )
            EditingField.PROTEIN -> s.copy(
                proteinG = value.coerceIn(0.0, 500.0),
                editingField = null, selectedPresetType = null,
            )
            EditingField.CARBS -> s.copy(
                carbsG = value.coerceIn(0.0, 800.0),
                editingField = null, selectedPresetType = null,
            )
            EditingField.FAT -> s.copy(
                fatG = value.coerceIn(0.0, 300.0),
                editingField = null, selectedPresetType = null,
            )
            null -> s.copy(editingField = null)
        }
    }

    fun reset() {
        scope.launch {
            dailyGoalRepo.reset().onSuccess { response ->
                _state.value = _state.value.copy(
                    calories = response.targetCalories,
                    proteinG = response.targetProteinG,
                    carbsG = response.targetCarbsG,
                    fatG = response.targetFatG,
                    selectedPresetType = null,
                    editingField = null,
                )
            }
        }
    }

    fun save(onSuccess: () -> Unit) {
        val s = _state.value
        _state.value = s.copy(isSaving = true)
        scope.launch {
            dailyGoalRepo.update(
                DailyGoalRequest(
                    targetCalories = s.calories,
                    targetProteinG = s.proteinG,
                    targetCarbsG = s.carbsG,
                    targetFatG = s.fatG,
                )
            ).onSuccess {
                _state.value = _state.value.copy(isSaving = false, isSaved = true)
                onSuccess()
            }.onFailure { err ->
                _state.value = _state.value.copy(isSaving = false, error = err.message)
            }
        }
    }
}
