package com.company.app.ui.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isComplete) {
        if (state.isComplete) onComplete()
    }

    LaunchedEffect(state.goal) {
        if (state.step == 2) viewModel.loadBmrPreview()
    }

    if (state.step == 1) {
        OnboardingStep1(state, viewModel)
    } else {
        OnboardingStep2(state, viewModel)
    }
}

@Composable
private fun OnboardingStep1(state: OnboardingState, viewModel: OnboardingViewModel) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Text("Your Body Profile", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Step 1 of 2", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = state.heightCm,
                    onValueChange = viewModel::onHeightChanged,
                    label = { Text("Height (cm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = state.weightKg,
                    onValueChange = viewModel::onWeightChanged,
                    label = { Text("Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.birthDate,
                onValueChange = viewModel::onBirthDateChanged,
                label = { Text("Birth Date (YYYY-MM-DD)") },
                singleLine = true,
                placeholder = { Text("1990-01-15") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            Text("Gender", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("MALE" to "Male", "FEMALE" to "Female").forEach { (value, label) ->
                    FilterChip(
                        selected = state.gender == value,
                        onClick = { viewModel.onGenderChanged(value) },
                        label = { Text(label) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Text("Activity Level", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))

            val activityOptions = listOf(
                "SEDENTARY" to "Sedentary",
                "LIGHTLY_ACTIVE" to "Lightly Active",
                "MODERATELY_ACTIVE" to "Moderate",
                "VERY_ACTIVE" to "Very Active",
                "EXTRA_ACTIVE" to "Extra Active"
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                activityOptions.chunked(2).forEach { row ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        row.forEach { (value, label) ->
                            FilterChip(
                                selected = state.activityLevel == value,
                                onClick = { viewModel.onActivityChanged(value) },
                                label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (row.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(32.dp))

            Button(onClick = viewModel::goToStep2, modifier = Modifier.fillMaxWidth()) {
                Text("Next")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun OnboardingStep2(state: OnboardingState, viewModel: OnboardingViewModel) {
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))
            Text("Your Goal", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Step 2 of 2", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(32.dp))

            Text("What's your goal?", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(8.dp))

            val goalOptions = listOf("LOSE" to "Lose Weight", "MAINTAIN" to "Maintain", "GAIN" to "Gain Muscle")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                goalOptions.forEach { (value, label) ->
                    FilterChip(
                        selected = state.goal == value,
                        onClick = { viewModel.onGoalChanged(value) },
                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (state.goal != "MAINTAIN") {
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.targetWeightKg,
                    onValueChange = viewModel::onTargetWeightChanged,
                    label = { Text("Target Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(24.dp))

            if (state.isLoadingPreview) {
                CircularProgressIndicator()
            } else {
                state.bmrPreview?.let { bmr ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Your Daily Targets", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            NutritionRow("Calories", "${bmr.recommendedCalories.toInt()} kcal")
                            NutritionRow("Protein", "${bmr.recommendedProteinG.toInt()} g")
                            NutritionRow("Carbs", "${bmr.recommendedCarbsG.toInt()} g")
                            NutritionRow("Fat", "${bmr.recommendedFatG.toInt()} g")
                        }
                    }
                }
            }

            state.error?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = viewModel::save,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Get Started")
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = viewModel::goBack, modifier = Modifier.fillMaxWidth()) {
                Text("Back")
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun NutritionRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}
