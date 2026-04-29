package com.company.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.DailySummary
import com.company.app.shared.data.model.MealLogEntry
import com.company.app.shared.data.model.NutritionSummary
import com.company.app.ui.components.*
import com.company.app.ui.theme.*

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onAddFood: (mealType: String) -> Unit,
    onAiScan: (mealType: String) -> Unit = {},
    onSubscription: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    onProfile: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CalSnapColors.Red,
                )
            }
            state.error != null -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
                ) {
                    Text(
                        text = state.error!!,
                        style = CalSnapType.Body,
                        color = CalSnapColors.Muted,
                    )
                    Button(
                        onClick = viewModel::loadDiary,
                        colors = ButtonDefaults.buttonColors(containerColor = CalSnapColors.Ink),
                        shape = RoundedCornerShape(CalSnapRadius.pill),
                    ) {
                        Text("Retry", style = CalSnapType.ButtonLarge)
                    }
                }
            }
            state.diary != null -> {
                HomeContent(
                    diary = state.diary!!,
                    streak = state.streak,
                    deletingId = state.deletingId,
                    onAddFood = onAddFood,
                    onAiScan = onAiScan,
                    onDelete = viewModel::deleteLog,
                    onProfile = onProfile,
                )
            }
        }

        CalSnapBottomTabBar(
            selectedTab = CalSnapTab.HOME,
            onTabSelected = { tab ->
                when (tab) {
                    CalSnapTab.STATS -> onAnalytics()
                    CalSnapTab.PROFILE -> onProfile()
                    CalSnapTab.LOG -> onAddFood("SNACK")
                    else -> {}
                }
            },
            onSnapTap = { onAiScan("LUNCH") },
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun HomeContent(
    diary: DailySummary,
    streak: Int,
    deletingId: Long?,
    onAddFood: (String) -> Unit,
    onAiScan: (String) -> Unit,
    onDelete: (MealLogEntry) -> Unit,
    onProfile: () -> Unit,
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 100.dp),
    ) {
        item {
            HomeTopBar(streak = streak, onProfile = onProfile)
        }
        item {
            CalorieDashboard(summary = diary.summary)
        }
        item {
            Spacer(Modifier.height(CalSnapSpacing.lg))
        }

        val mealOrder = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
        mealOrder.forEach { mealType ->
            val entries = diary.meals[mealType] ?: emptyList()
            item(key = mealType) {
                MealSection(
                    mealType = mealType,
                    entries = entries,
                    deletingId = deletingId,
                    onAddFood = { onAddFood(mealType) },
                    onAiScan = { onAiScan(mealType) },
                    onDelete = onDelete,
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(streak: Int, onProfile: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad)
            .padding(top = CalSnapSpacing.lg, bottom = CalSnapSpacing.sm),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text(
                text = "Today",
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
            )
            if (streak >= 2) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                    Text("🔥", fontSize = 12.sp)
                    Text(
                        text = "$streak day streak",
                        style = CalSnapType.BodySmall,
                        color = CalSnapColors.Muted,
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(CalSnapColors.SurfaceAlt)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onProfile,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "profile", size = 20.dp, color = CalSnapColors.Ink)
        }
    }
}

@Composable
private fun CalorieDashboard(summary: NutritionSummary) {
    val progress = (summary.caloriesPercent / 100f).coerceIn(0f, 1.5f)
    val isOverTarget = summary.caloriesPercent > 100
    val ringColor = if (isOverTarget) CalSnapColors.Red else CalSnapColors.Ink
    val centerCals = kotlin.math.abs(summary.remainingCalories.toInt())
    val centerLabel = if (isOverTarget) "kcal over" else "kcal left"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CalSnapRing(
            progress = progress.coerceIn(0f, 1f),
            size = 220.dp,
            strokeWidth = 14.dp,
            color = ringColor,
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = centerCals.toString(),
                    style = CalSnapType.Hero,
                    color = if (isOverTarget) CalSnapColors.Red else CalSnapColors.Ink,
                )
                Text(
                    text = centerLabel,
                    style = CalSnapType.BodySmall,
                    color = CalSnapColors.Muted,
                )
            }
        }

        Spacer(Modifier.height(CalSnapSpacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            CalorieStatBadge(label = "Goal", value = "${summary.targetCalories.toInt()}")
            CalorieStatDivider()
            CalorieStatBadge(label = "Eaten", value = "${summary.totalCalories.toInt()}")
            CalorieStatDivider()
            CalorieStatBadge(label = "Left", value = "${summary.remainingCalories.toInt()}")
        }

        Spacer(Modifier.height(CalSnapSpacing.lg))

        // Macro bars derived from target calories
        val proteinTarget = (summary.targetCalories * 0.25 / 4).toFloat().coerceAtLeast(1f)
        val carbTarget = (summary.targetCalories * 0.50 / 4).toFloat().coerceAtLeast(1f)
        val fatTarget = (summary.targetCalories * 0.25 / 9).toFloat().coerceAtLeast(1f)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
        ) {
            CalSnapMacroBar(
                label = "Protein",
                current = summary.totalProteinG.toFloat(),
                target = proteinTarget,
                color = CalSnapColors.Protein,
                modifier = Modifier.weight(1f),
            )
            CalSnapMacroBar(
                label = "Carbs",
                current = summary.totalCarbsG.toFloat(),
                target = carbTarget,
                color = CalSnapColors.Carb,
                modifier = Modifier.weight(1f),
            )
            CalSnapMacroBar(
                label = "Fat",
                current = summary.totalFatG.toFloat(),
                target = fatTarget,
                color = CalSnapColors.Fat,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun CalorieStatBadge(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = CalSnapType.HeadlineMedium,
            color = CalSnapColors.Ink,
        )
        Text(
            text = label,
            style = CalSnapType.BodySmall,
            color = CalSnapColors.Muted,
        )
    }
}

@Composable
private fun CalorieStatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(32.dp)
            .background(CalSnapColors.Divider),
    )
}

@Composable
private fun MealSection(
    mealType: String,
    entries: List<MealLogEntry>,
    deletingId: Long?,
    onAddFood: () -> Unit,
    onAiScan: () -> Unit,
    onDelete: (MealLogEntry) -> Unit,
) {
    val label = mealType.lowercase().replaceFirstChar { it.uppercase() }
    val emoji = when (mealType) {
        "BREAKFAST" -> "☀️"
        "LUNCH" -> "🌤"
        "DINNER" -> "🌙"
        else -> "🍎"
    }
    val totalCal = entries.sumOf { it.calories }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad)
            .padding(bottom = CalSnapSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(CalSnapRadius.sm))
                    .background(CalSnapColors.SurfaceAlt),
                contentAlignment = Alignment.Center,
            ) {
                Text(emoji, fontSize = 18.sp)
            }
            Spacer(Modifier.width(CalSnapSpacing.sm))
            Text(
                text = label,
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
                modifier = Modifier.weight(1f),
            )
            if (totalCal > 0) {
                Text(
                    text = "${totalCal.toInt()} kcal",
                    style = CalSnapType.BodySmall,
                    color = CalSnapColors.Muted,
                )
                Spacer(Modifier.width(CalSnapSpacing.sm))
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(CalSnapRadius.sm))
                    .background(CalSnapColors.SurfaceAlt)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onAddFood,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = "plus", size = 18.dp, color = CalSnapColors.Ink)
            }
        }

        Spacer(Modifier.height(CalSnapSpacing.sm))

        if (entries.isEmpty()) {
            EmptyMealSlot(onAdd = onAddFood)
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.xs)) {
                entries.forEach { entry ->
                    FoodLogItem(
                        entry = entry,
                        isDeleting = deletingId == entry.id,
                        onDelete = { onDelete(entry) },
                    )
                }
            }
        }

        Spacer(Modifier.height(CalSnapSpacing.md))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CalSnapColors.Divider),
        )
    }
}

@Composable
private fun EmptyMealSlot(onAdd: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CalSnapRadius.md))
            .background(CalSnapColors.SurfaceAlt)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onAdd,
            )
            .padding(CalSnapSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CalSnapColors.Divider),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "plus", size = 18.dp, color = CalSnapColors.Muted)
        }
        Text(
            text = "Add food",
            style = CalSnapType.Body,
            color = CalSnapColors.Muted,
        )
    }
}

@Composable
private fun FoodLogItem(
    entry: MealLogEntry,
    isDeleting: Boolean,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CalSnapRadius.md))
            .background(CalSnapColors.Background)
            .padding(CalSnapSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
    ) {
        CalSnapFoodPhoto(
            name = entry.foodItem.name,
            size = 48.dp,
            cornerRadius = CalSnapRadius.md,
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.foodItem.name,
                style = CalSnapType.BodyLarge,
                color = CalSnapColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${entry.quantityG.toInt()}g · P${entry.proteinG.toInt()} C${entry.carbsG.toInt()} F${entry.fatG.toInt()}",
                style = CalSnapType.BodySmall,
                color = CalSnapColors.Muted,
            )
        }

        Text(
            text = "${entry.calories.toInt()}",
            style = CalSnapType.BodyLarge,
            color = CalSnapColors.Ink,
        )

        if (isDeleting) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = CalSnapColors.Red,
            )
        } else {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(CalSnapRadius.sm))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDelete,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = "close", size = 16.dp, color = CalSnapColors.Muted)
            }
        }
    }
}
