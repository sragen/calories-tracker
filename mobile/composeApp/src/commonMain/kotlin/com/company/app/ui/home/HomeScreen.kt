package com.company.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.app.shared.data.model.MealLogEntry
import com.company.app.shared.data.model.NutritionSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onLogout: () -> Unit,
    onAddFood: (mealType: String) -> Unit,
    onAiScan: (mealType: String) -> Unit = {},
    onSubscription: () -> Unit = {},
    onAnalytics: () -> Unit = {},
    onProfile: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (state.streak >= 2) {
                        Text("Today  🔥 ${state.streak}")
                    } else {
                        Text("Today")
                    }
                },
                actions = {
                    IconButton(onClick = { onAiScan("LUNCH") }) { Text("📷") }
                    IconButton(onClick = onAnalytics) { Text("📊") }
                    IconButton(onClick = onSubscription) { Text("★") }
                    IconButton(onClick = onProfile) { Text("👤") }
                    TextButton(onClick = viewModel::logout) { Text("Logout") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAddFood("SNACK") },
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add food")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.error != null -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::loadDiary) { Text("Retry") }
                    }
                }
                state.diary != null -> {
                    val diary = state.diary!!
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 88.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
                            CalorieSummaryCard(
                                summary = diary.summary,
                                modifier = Modifier.padding(16.dp)
                            )
                        }
                        item {
                            MacroBarsCard(
                                summary = diary.summary,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }

                        val mealOrder = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
                        mealOrder.forEach { mealType ->
                            val entries = diary.meals[mealType] ?: emptyList()
                            item {
                                MealSection(
                                    mealType = mealType,
                                    entries = entries,
                                    deletingId = state.deletingId,
                                    onAddFood = { onAddFood(mealType) },
                                    onDelete = viewModel::deleteLog
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalorieSummaryCard(summary: NutritionSummary, modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val sweepAngle = (360f * summary.caloriesPercent / 100f).coerceAtMost(360f)

    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center) {
                Canvas(modifier = Modifier.size(120.dp)) {
                    val stroke = Stroke(width = 16.dp.toPx(), cap = StrokeCap.Round)
                    val arcSize = Size(size.width - 16.dp.toPx(), size.height - 16.dp.toPx())
                    val topLeft = Offset(8.dp.toPx(), 8.dp.toPx())
                    drawArc(color = surfaceVariant, startAngle = -90f, sweepAngle = 360f, useCenter = false, style = stroke, size = arcSize, topLeft = topLeft)
                    if (sweepAngle > 0f) {
                        drawArc(color = primary, startAngle = -90f, sweepAngle = sweepAngle, useCenter = false, style = stroke, size = arcSize, topLeft = topLeft)
                    }
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = summary.totalCalories.toInt().toString(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text("kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(Modifier.width(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CalorieStatRow("Goal", "${summary.targetCalories.toInt()} kcal")
                CalorieStatRow("Eaten", "${summary.totalCalories.toInt()} kcal")
                CalorieStatRow("Remaining", "${summary.remainingCalories.toInt()} kcal")
            }
        }
    }
}

@Composable
private fun CalorieStatRow(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.width(72.dp))
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MacroBarsCard(summary: NutritionSummary, modifier: Modifier = Modifier) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MacroBar("Protein", summary.totalProteinG, summary.targetCalories * 0.25 / 4, Color(0xFF4CAF50))
            MacroBar("Carbs", summary.totalCarbsG, summary.targetCalories * 0.50 / 4, Color(0xFF2196F3))
            MacroBar("Fat", summary.totalFatG, summary.targetCalories * 0.25 / 9, Color(0xFFFF9800))
        }
    }
}

@Composable
private fun MacroBar(label: String, current: Double, target: Double, color: Color) {
    val progress = if (target > 0) (current / target).toFloat().coerceIn(0f, 1f) else 0f
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodySmall)
            Text("${current.toInt()} / ${target.toInt()} g", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(6.dp),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun MealSection(
    mealType: String,
    entries: List<MealLogEntry>,
    deletingId: Long?,
    onAddFood: () -> Unit,
    onDelete: (MealLogEntry) -> Unit
) {
    val label = mealType.lowercase().replaceFirstChar { it.uppercase() }
    val totalCal = entries.sumOf { it.calories }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (totalCal > 0) {
                    Text("${totalCal.toInt()} kcal", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(8.dp))
                }
                IconButton(onClick = onAddFood, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Add, contentDescription = "Add $label", modifier = Modifier.size(18.dp))
                }
            }
        }

        if (entries.isEmpty()) {
            Text(
                "No food logged",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            entries.forEach { entry ->
                MealLogItem(entry = entry, isDeleting = deletingId == entry.id, onDelete = { onDelete(entry) })
            }
        }

        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun MealLogItem(entry: MealLogEntry, isDeleting: Boolean, onDelete: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.foodItem.name, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${entry.quantityG.toInt()} g · P:${entry.proteinG.toInt()}g C:${entry.carbsG.toInt()}g F:${entry.fatG.toInt()}g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text("${entry.calories.toInt()} kcal", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(4.dp))
        if (isDeleting) {
            CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
        } else {
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}
