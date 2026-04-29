package com.company.app.ui.onboarding

import androidx.compose.runtime.*

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    when (state.step) {
        1 -> OnboardingGoalScreen(
            selectedGoal = state.goal,
            onGoalSelected = viewModel::onGoalChanged,
            onContinue = { viewModel.goToStep(2) },
        )
        2 -> OnboardingBodyScreen(
            weightKg = state.weightKg,
            heightCm = state.heightCm,
            age = state.age,
            gender = state.gender,
            onWeightChanged = viewModel::onWeightChanged,
            onHeightChanged = viewModel::onHeightChanged,
            onAgeChanged = viewModel::onAgeChanged,
            onGenderChanged = viewModel::onGenderChanged,
            onContinue = { viewModel.goToStep(3) },
            onBack = viewModel::goBack,
        )
        3 -> OnboardingActivityScreen(
            selectedActivity = state.activityLevel,
            onActivitySelected = viewModel::onActivityChanged,
            onContinue = { viewModel.goToStep(4) },
            onBack = viewModel::goBack,
        )
        else -> OnboardingPlanRevealScreen(
            preview = state.bmrPreview,
            isLoading = state.isLoadingPreview,
            onStart = viewModel::save,
            onBack = viewModel::goBack,
        )
    }
}
