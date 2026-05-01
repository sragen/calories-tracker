package com.company.app.ui.aiscan

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.AiDetectedFood
import com.company.app.ui.components.*
import com.company.app.ui.theme.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun AiScanResultScreen(
    viewModel: AiScanViewModel,
    mealType: String = "LUNCH",
    isGuestMode: Boolean = false,
    onConfirmed: () -> Unit,
    onBack: () -> Unit,
    onRegisterFromGuest: () -> Unit = {},
) {
    val state = viewModel.state
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    LaunchedEffect(state.confirmed) {
        if (state.confirmed) onConfirmed()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Surface),
    ) {
        when {
            state.isAnalyzing -> AnalyzingContent()
            state.scanResult == null -> EmptyScanContent(error = state.error, onBack = onBack)
            else -> {
                val result = state.scanResult
                val totalKcal = state.selectedFoods.sumOf { it.totalCalories.let { c ->
                    if (c > 0.0) c else it.caloriesPer100g * it.portionG / 100.0
                }}

                LazyColumn(
                    contentPadding = PaddingValues(bottom = 120.dp),
                ) {
                    item {
                        ResultHeader(
                            count = result.detectedFoods.size,
                            onBack = onBack,
                        )
                    }

                    if (result.detectedFoods.isEmpty()) {
                        item { NoFoodDetectedCard() }
                    } else {
                        items(result.detectedFoods, key = { "${it.name}_${it.matchedFoodId}" }) { food ->
                            val isSelected = state.selectedFoods.any {
                                it.name == food.name && it.matchedFoodId == food.matchedFoodId
                            }
                            val currentFood = state.selectedFoods.find {
                                it.name == food.name && it.matchedFoodId == food.matchedFoodId
                            } ?: food

                            FoodDetectedCard(
                                food = currentFood,
                                isSelected = isSelected,
                                onToggle = { viewModel.toggleFood(food) },
                                onPortionChange = { viewModel.updatePortion(food, it) },
                            )
                        }
                    }

                    state.error?.let {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = CalSnapSpacing.screenPad)
                                    .clip(RoundedCornerShape(CalSnapRadius.md))
                                    .background(CalSnapColors.RedSoft)
                                    .padding(CalSnapSpacing.md),
                            ) {
                                Text(
                                    text = it,
                                    style = CalSnapType.Body,
                                    color = CalSnapColors.Red,
                                )
                            }
                        }
                    }
                }

                // Bottom action bar
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .background(CalSnapColors.Background)
                        .navigationBarsPadding()
                        .padding(CalSnapSpacing.screenPad),
                ) {
                    if (isGuestMode) {
                        GuestBottomBar(onRegister = onRegisterFromGuest)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            if (state.selectedFoods.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text = "${state.selectedFoods.size} items selected",
                                        style = CalSnapType.BodySmall,
                                        color = CalSnapColors.Muted,
                                    )
                                    Text(
                                        text = "${totalKcal.toInt()} kcal",
                                        style = CalSnapType.BodySmall,
                                        color = CalSnapColors.Ink,
                                    )
                                }
                            }
                            CalSnapBrandButton(
                                text = if (state.isConfirming) "Saving…"
                                       else "Log ${state.selectedFoods.size} items",
                                onClick = { viewModel.confirm(mealType, today) },
                                enabled = state.selectedFoods.isNotEmpty() && !state.isConfirming,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyzingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CalSnapColors.SurfaceAlt),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    color = CalSnapColors.Red,
                    modifier = Modifier.size(36.dp),
                    strokeWidth = 3.dp,
                )
            }
            Text(
                text = "Analyzing your meal…",
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
            )
            Text(
                text = "This takes a few seconds",
                style = CalSnapType.Body,
                color = CalSnapColors.Muted,
            )
        }
    }
}

@Composable
private fun EmptyScanContent(error: String?, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
            modifier = Modifier.padding(CalSnapSpacing.xl),
        ) {
            Text(if (error != null) "⚠️" else "📷", fontSize = 48.sp)
            Text(
                text = if (error != null) "Scan failed" else "No scan result",
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
            )
            if (error != null) {
                Text(
                    text = error,
                    style = CalSnapType.Body,
                    color = CalSnapColors.Muted,
                )
            }
            CalSnapTextButton(text = "Go back", onClick = onBack)
        }
    }
}

@Composable
private fun ResultHeader(count: Int, onBack: () -> Unit) {
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
            Text(
                text = "AI Detected $count ${if (count == 1) "item" else "items"}",
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
            )
            Text(
                text = "Tap to deselect items you don't want to log",
                style = CalSnapType.BodySmall,
                color = CalSnapColors.Muted,
            )
        }
    }
}

@Composable
private fun NoFoodDetectedCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad)
            .clip(RoundedCornerShape(CalSnapRadius.card))
            .background(CalSnapColors.SurfaceAlt)
            .padding(CalSnapSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
        ) {
            Text("🤔", fontSize = 36.sp)
            Text(
                text = "No food detected",
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
            )
            Text(
                text = "Try retaking the photo with better lighting\nor a clearer view of the food.",
                style = CalSnapType.Body,
                color = CalSnapColors.Muted,
            )
        }
    }
}

@Composable
private fun FoodDetectedCard(
    food: AiDetectedFood,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onPortionChange: (Double) -> Unit,
) {
    var portionText by remember(food.portionG) { mutableStateOf(food.portionG.toInt().toString()) }
    val kcal = food.caloriesPer100g * (portionText.toDoubleOrNull() ?: food.portionG) / 100.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad, vertical = CalSnapSpacing.xs)
            .clip(RoundedCornerShape(CalSnapRadius.card))
            .background(if (isSelected) CalSnapColors.Background else CalSnapColors.SurfaceAlt)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CalSnapColors.Ink else CalSnapColors.Border,
                shape = RoundedCornerShape(CalSnapRadius.card),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onToggle,
            )
            .padding(CalSnapSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
    ) {
        CalSnapFoodPhoto(
            name = food.name,
            size = 52.dp,
            cornerRadius = CalSnapRadius.md,
        )

        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = food.name,
                    style = CalSnapType.BodyLarge,
                    color = CalSnapColors.Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )
                if (food.matchedFoodId != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(CalSnapRadius.pill))
                            .background(CalSnapColors.GoodBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                    ) {
                        Text(
                            text = "✓ matched",
                            style = CalSnapType.Label,
                            color = CalSnapColors.Good,
                        )
                    }
                }
            }
            Spacer(Modifier.height(2.dp))
            Text(
                text = "P${food.proteinPer100g.toInt()} C${food.carbsPer100g.toInt()} F${food.fatPer100g.toInt()} per 100g",
                style = CalSnapType.BodySmall,
                color = CalSnapColors.Muted,
            )
        }

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            // Portion stepper
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                PortionStepButton("-") {
                    val current = portionText.toDoubleOrNull() ?: food.portionG
                    val next = (current - 10.0).coerceAtLeast(10.0)
                    portionText = next.toInt().toString()
                    onPortionChange(next)
                }
                Text(
                    text = "${portionText}g",
                    style = CalSnapType.BodySmall,
                    color = CalSnapColors.Ink,
                )
                PortionStepButton("+") {
                    val current = portionText.toDoubleOrNull() ?: food.portionG
                    val next = current + 10.0
                    portionText = next.toInt().toString()
                    onPortionChange(next)
                }
            }
            Text(
                text = "${kcal.toInt()} kcal",
                style = CalSnapType.BodySmall,
                color = CalSnapColors.Muted,
            )
        }

        // Selection indicator
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) CalSnapColors.Ink else CalSnapColors.Divider),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Text("✓", color = CalSnapColors.Background, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun PortionStepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(RoundedCornerShape(CalSnapRadius.sm))
            .background(CalSnapColors.SurfaceAlt)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = CalSnapType.BodyLarge,
            color = CalSnapColors.Ink,
        )
    }
}

@Composable
private fun GuestBottomBar(onRegister: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.xs),
    ) {
        Text(
            text = "Create a free account to save these results",
            style = CalSnapType.BodySmall,
            color = CalSnapColors.Muted,
        )
        CalSnapPrimaryButton(
            text = "Create Account & Save",
            onClick = onRegister,
        )
    }
}
