package com.company.app.ui.onboarding

import com.company.app.shared.data.model.BodyProfileRequest
import com.company.app.shared.data.model.BmrPreviewResponse
import com.company.app.shared.data.repository.BodyProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OnboardingState(
    val step: Int = 1,
    // Step 1 inputs
    val name: String = "",
    val heightCm: String = "",
    val weightKg: String = "",
    val birthDate: String = "",
    val gender: String = "MALE",
    val activityLevel: String = "SEDENTARY",
    // Step 2 inputs
    val goal: String = "MAINTAIN",
    val targetWeightKg: String = "",
    // Step 2 preview
    val bmrPreview: BmrPreviewResponse? = null,
    val isLoadingPreview: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false
)

class OnboardingViewModel(private val bodyProfileRepo: BodyProfileRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun onHeightChanged(v: String) { _state.value = _state.value.copy(heightCm = v) }
    fun onWeightChanged(v: String) { _state.value = _state.value.copy(weightKg = v) }
    fun onBirthDateChanged(v: String) { _state.value = _state.value.copy(birthDate = v) }
    fun onGenderChanged(v: String) { _state.value = _state.value.copy(gender = v) }
    fun onActivityChanged(v: String) { _state.value = _state.value.copy(activityLevel = v) }
    fun onGoalChanged(v: String) { _state.value = _state.value.copy(goal = v, bmrPreview = null) }
    fun onTargetWeightChanged(v: String) { _state.value = _state.value.copy(targetWeightKg = v) }

    fun goToStep2() {
        val s = _state.value
        if (s.heightCm.isBlank() || s.weightKg.isBlank() || s.birthDate.isBlank()) {
            _state.value = s.copy(error = "Please fill all fields")
            return
        }
        _state.value = s.copy(step = 2, error = null)
        loadBmrPreview()
    }

    fun goBack() {
        _state.value = _state.value.copy(step = 1, error = null)
    }

    fun loadBmrPreview() {
        val req = buildRequest() ?: return
        _state.value = _state.value.copy(isLoadingPreview = true, bmrPreview = null)
        scope.launch {
            bodyProfileRepo.previewBmr(req)
                .onSuccess { _state.value = _state.value.copy(isLoadingPreview = false, bmrPreview = it) }
                .onFailure { _state.value = _state.value.copy(isLoadingPreview = false, error = it.message) }
        }
    }

    fun save() {
        val req = buildRequest() ?: return
        _state.value = _state.value.copy(isSaving = true, error = null)
        scope.launch {
            bodyProfileRepo.save(req)
                .onSuccess { _state.value = _state.value.copy(isSaving = false, isComplete = true) }
                .onFailure { _state.value = _state.value.copy(isSaving = false, error = it.message) }
        }
    }

    private fun buildRequest(): BodyProfileRequest? {
        val s = _state.value
        val height = s.heightCm.toDoubleOrNull() ?: return null
        val weight = s.weightKg.toDoubleOrNull() ?: return null
        if (s.birthDate.isBlank()) return null
        return BodyProfileRequest(
            heightCm = height,
            weightKg = weight,
            birthDate = s.birthDate,
            gender = s.gender,
            activityLevel = s.activityLevel,
            goal = s.goal,
            targetWeightKg = s.targetWeightKg.toDoubleOrNull()
        )
    }
}
