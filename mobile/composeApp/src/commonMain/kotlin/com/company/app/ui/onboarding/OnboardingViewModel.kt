package com.company.app.ui.onboarding

import com.company.app.shared.data.model.BodyProfileRequest
import com.company.app.shared.data.model.BmrPreviewResponse
import com.company.app.shared.data.repository.BodyProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class OnboardingState(
    val step: Int = 1,              // 1=Goal  2=Body  3=Activity  4=PlanReveal
    val goal: String = "LOSE",      // LOSE | MAINTAIN | GAIN
    val weightKg: Float = 75f,
    val targetWeightKg: Float = 65f,
    val heightCm: Float = 170f,
    val age: Int = 25,
    val gender: String = "MALE",    // MALE | FEMALE
    val activityLevel: String = "SEDENTARY",
    val bmrPreview: BmrPreviewResponse? = null,
    val isLoadingPreview: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isComplete: Boolean = false,
)

class OnboardingViewModel(private val bodyProfileRepo: BodyProfileRepository) {
    private val scope = CoroutineScope(Dispatchers.Main)
    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun onGoalChanged(goal: String) { _state.value = _state.value.copy(goal = goal, bmrPreview = null) }
    fun onWeightChanged(kg: Float) { _state.value = _state.value.copy(weightKg = kg) }
    fun onTargetWeightChanged(kg: Float) { _state.value = _state.value.copy(targetWeightKg = kg) }
    fun onHeightChanged(cm: Float) { _state.value = _state.value.copy(heightCm = cm) }
    fun onAgeChanged(age: Int) { _state.value = _state.value.copy(age = age) }
    fun onGenderChanged(gender: String) { _state.value = _state.value.copy(gender = gender) }
    fun onActivityChanged(level: String) { _state.value = _state.value.copy(activityLevel = level) }

    fun goToStep(step: Int) {
        _state.value = _state.value.copy(step = step, error = null)
        if (step == 4) loadBmrPreview()
    }

    fun goBack() {
        val prev = (_state.value.step - 1).coerceAtLeast(1)
        _state.value = _state.value.copy(step = prev, error = null)
    }

    fun loadBmrPreview() {
        val req = buildRequest()
        _state.value = _state.value.copy(isLoadingPreview = true, bmrPreview = null)
        scope.launch {
            bodyProfileRepo.previewBmr(req)
                .onSuccess { _state.value = _state.value.copy(isLoadingPreview = false, bmrPreview = it) }
                .onFailure { _state.value = _state.value.copy(isLoadingPreview = false, error = it.message) }
        }
    }

    fun save() {
        val req = buildRequest()
        _state.value = _state.value.copy(isSaving = true, error = null)
        scope.launch {
            bodyProfileRepo.save(req)
                .onSuccess { _state.value = _state.value.copy(isSaving = false, isComplete = true) }
                .onFailure { _state.value = _state.value.copy(isSaving = false, error = it.message) }
        }
    }

    private fun buildRequest(): BodyProfileRequest {
        val s = _state.value
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        val birthDate = "${currentYear - s.age}-01-01"
        return BodyProfileRequest(
            heightCm = s.heightCm.toDouble(),
            weightKg = s.weightKg.toDouble(),
            birthDate = birthDate,
            gender = s.gender,
            activityLevel = s.activityLevel,
            goal = s.goal,
            targetWeightKg = if (s.goal != "MAINTAIN") s.targetWeightKg.toDouble() else null,
        )
    }
}
