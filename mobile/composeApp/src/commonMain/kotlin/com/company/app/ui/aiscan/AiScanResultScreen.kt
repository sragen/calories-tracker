package com.company.app.ui.aiscan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.AiDetectedFood
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@Composable
fun AiScanResultScreen(
    viewModel: AiScanViewModel,
    mealType: String = "LUNCH",
    onConfirmed: () -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel.state
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    LaunchedEffect(state.confirmed) {
        if (state.confirmed) onConfirmed()
    }

    if (state.isAnalyzing) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator()
                Text("AI sedang menganalisis makanan...")
            }
        }
        return
    }

    val result = state.scanResult
    if (result == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Tidak ada hasil scan", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(16.dp))
                Button(onClick = onBack) { Text("Kembali") }
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hasil Scan AI") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = { viewModel.confirm(mealType, today) },
                    enabled = state.selectedFoods.isNotEmpty() && !state.isConfirming,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    if (state.isConfirming) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Catat ${state.selectedFoods.size} Makanan")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                if (result.detectedFoods.isEmpty()) {
                    Card(Modifier.fillMaxWidth()) {
                        Box(Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                            Text("Tidak ada makanan terdeteksi. Coba foto lagi dengan pencahayaan lebih baik.")
                        }
                    }
                } else {
                    Text(
                        "AI mendeteksi ${result.detectedFoods.size} makanan. Centang yang ingin dicatat:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            items(result.detectedFoods) { food ->
                val isSelected = state.selectedFoods.any { it.name == food.name && it.matchedFoodId == food.matchedFoodId }
                val currentFood = state.selectedFoods.find { it.name == food.name && it.matchedFoodId == food.matchedFoodId } ?: food

                AiDetectedFoodCard(
                    food = currentFood,
                    isSelected = isSelected,
                    onToggle = { viewModel.toggleFood(food) },
                    onPortionChange = { newPortion -> viewModel.updatePortion(food, newPortion) }
                )
            }

            if (state.error != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            state.error,
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AiDetectedFoodCard(
    food: AiDetectedFood,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onPortionChange: (Double) -> Unit
) {
    var portionText by remember(food.portionG) { mutableStateOf(food.portionG.toInt().toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = isSelected, onCheckedChange = { onToggle() })
            Spacer(Modifier.width(8.dp))
            Column(Modifier.weight(1f)) {
                Text(food.name, fontWeight = FontWeight.SemiBold)
                if (food.matchedFoodId != null) {
                    Text("Cocok di database ✓", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "${food.caloriesPer100g.toInt()} kcal/100g  •  " +
                    "P:${food.proteinPer100g.toInt()}g C:${food.carbsPer100g.toInt()}g F:${food.fatPer100g.toInt()}g",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End, modifier = Modifier.width(80.dp)) {
                OutlinedTextField(
                    value = portionText,
                    onValueChange = { v ->
                        portionText = v
                        v.toDoubleOrNull()?.let { onPortionChange(it) }
                    },
                    label = { Text("gram", fontSize = 10.sp) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                val kcal = food.caloriesPer100g * (portionText.toDoubleOrNull() ?: food.portionG) / 100.0
                Text("${kcal.toInt()} kcal", fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}
