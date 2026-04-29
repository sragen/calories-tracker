package com.company.app.ui.submit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.company.app.ui.components.*
import com.company.app.ui.theme.*

@Composable
fun SubmitFoodScreen(
    viewModel: SubmitFoodViewModel,
    initialBarcode: String = "",
    onSuccess: () -> Unit,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialBarcode) {
        if (initialBarcode.isNotBlank()) viewModel.initWithBarcode(initialBarcode)
    }

    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) onSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            // ── Header ───────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalSnapSpacing.screenPad)
                    .padding(top = CalSnapSpacing.lg, bottom = CalSnapSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CalSnapColors.SurfaceAlt)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "chev-l", size = 18.dp, color = CalSnapColors.Ink)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Submit New Food", style = CalSnapType.HeadlineMedium, color = CalSnapColors.Ink)
                    Text("Help grow the food database", style = CalSnapType.BodySmall, color = CalSnapColors.Muted)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalSnapSpacing.screenPad),
                verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
            ) {
                // Barcode badge (when pre-filled from scanner)
                if (state.barcode.isNotBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CalSnapRadius.md))
                            .background(CalSnapColors.CarbBg)
                            .padding(CalSnapSpacing.md),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
                    ) {
                        CalSnapIcon(name = "barcode", size = 18.dp, color = CalSnapColors.Warn)
                        Column {
                            Text("Barcode not found in database", style = CalSnapType.BodySmall, color = CalSnapColors.Warn)
                            Text(state.barcode, style = CalSnapType.Label, color = CalSnapColors.Warn)
                        }
                    }
                }

                // ── Food name ────────────────────────────────────────
                SectionLabel("FOOD NAME")
                CalSnapField(
                    value = state.name,
                    onValueChange = viewModel::onNameChanged,
                    placeholder = "e.g. Nasi Goreng",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )
                CalSnapField(
                    value = state.nameEn,
                    onValueChange = viewModel::onNameEnChanged,
                    placeholder = "English name (optional)",
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                )

                Spacer(Modifier.height(CalSnapSpacing.xs))

                // ── Nutrition per 100g ───────────────────────────────
                SectionLabel("NUTRITION PER 100g")

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
                ) {
                    CalSnapField(
                        value = state.caloriesPer100g,
                        onValueChange = viewModel::onCaloriesChanged,
                        placeholder = "Calories",
                        suffix = "kcal",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    CalSnapField(
                        value = state.proteinPer100g,
                        onValueChange = viewModel::onProteinChanged,
                        placeholder = "Protein",
                        suffix = "g",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
                ) {
                    CalSnapField(
                        value = state.carbsPer100g,
                        onValueChange = viewModel::onCarbsChanged,
                        placeholder = "Carbs",
                        suffix = "g",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                    CalSnapField(
                        value = state.fatPer100g,
                        onValueChange = viewModel::onFatChanged,
                        placeholder = "Fat",
                        suffix = "g",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f),
                    )
                }

                CalSnapField(
                    value = state.fiberPer100g,
                    onValueChange = viewModel::onFiberChanged,
                    placeholder = "Fiber (optional)",
                    suffix = "g",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(0.5f),
                )

                Spacer(Modifier.height(CalSnapSpacing.xs))

                // ── Default serving ──────────────────────────────────
                SectionLabel("DEFAULT SERVING")
                CalSnapField(
                    value = state.defaultServingG,
                    onValueChange = viewModel::onServingChanged,
                    placeholder = "100",
                    suffix = "g",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(0.5f),
                )

                // Error
                state.error?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(CalSnapRadius.md))
                            .background(CalSnapColors.RedSoft)
                            .padding(CalSnapSpacing.md),
                    ) {
                        Text(it, style = CalSnapType.Body, color = CalSnapColors.Red)
                    }
                }

                // Review notice
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CalSnapRadius.md))
                        .background(CalSnapColors.SurfaceAlt)
                        .padding(CalSnapSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
                    verticalAlignment = Alignment.Top,
                ) {
                    CalSnapIcon(name = "star", size = 16.dp, color = CalSnapColors.Muted)
                    Text(
                        text = "Your submission will be reviewed by our team before becoming searchable.",
                        style = CalSnapType.BodySmall,
                        color = CalSnapColors.Muted,
                    )
                }

                CalSnapPrimaryButton(
                    text = if (state.isSubmitting) "Submitting…" else "Submit Food",
                    onClick = viewModel::submit,
                    enabled = !state.isSubmitting,
                )

                Spacer(Modifier.height(CalSnapSpacing.xl))
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = CalSnapType.Label,
        color = CalSnapColors.Muted,
    )
}

@Composable
private fun CalSnapField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    suffix: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, style = CalSnapType.Body, color = CalSnapColors.Hint) },
        singleLine = true,
        keyboardOptions = keyboardOptions,
        suffix = suffix?.let { { Text(it, style = CalSnapType.Body, color = CalSnapColors.Muted) } },
        textStyle = CalSnapType.Body.copy(color = CalSnapColors.Ink),
        shape = RoundedCornerShape(CalSnapRadius.md),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CalSnapColors.Ink,
            unfocusedBorderColor = CalSnapColors.Border,
            focusedContainerColor = CalSnapColors.Background,
            unfocusedContainerColor = CalSnapColors.SurfaceAlt,
        ),
        modifier = modifier.fillMaxWidth(),
    )
}
