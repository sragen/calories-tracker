package com.company.app.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.company.app.shared.data.model.FoodItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFoodScreen(
    viewModel: SearchFoodViewModel,
    initialMealType: String,
    onBack: () -> Unit,
    onLogSuccess: () -> Unit,
    onOpenBarcodeScanner: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(initialMealType) {
        if (state.mealType == "SNACK" && initialMealType != "SNACK") {
            viewModel.onMealTypeChanged(initialMealType)
        }
    }

    LaunchedEffect(state.logSuccess) {
        if (state.logSuccess) onLogSuccess()
    }

    // Load more when near bottom
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = (layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            lastVisibleIndex >= totalItems - 3
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) viewModel.loadMore()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = state.query,
                        onValueChange = viewModel::onQueryChanged,
                        placeholder = { Text("Search foods...") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surface,
                            focusedBorderColor = MaterialTheme.colorScheme.surface
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onOpenBarcodeScanner) {
                        Text("Scan")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Category chips
            if (state.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = state.selectedCategoryId == null,
                            onClick = { viewModel.onCategorySelected(null) },
                            label = { Text("All") }
                        )
                    }
                    items(state.categories) { cat ->
                        FilterChip(
                            selected = state.selectedCategoryId == cat.id,
                            onClick = { viewModel.onCategorySelected(cat.id) },
                            label = { Text(cat.name) }
                        )
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (state.isSearching && state.results.isEmpty()) {
                    CircularProgressIndicator(Modifier.align(Alignment.Center))
                } else {
                    LazyColumn(state = listState) {
                        // Recent foods section (show when no query)
                        if (state.query.isBlank() && state.recentFoods.isNotEmpty()) {
                            item {
                                Text(
                                    "Recent",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                            items(state.recentFoods) { food ->
                                FoodListItem(food = food, onClick = { viewModel.selectFood(food, state.mealType) })
                            }
                            item { HorizontalDivider(modifier = androidx.compose.ui.Modifier.padding(vertical = 8.dp)) }
                            item {
                                Text(
                                    "All Foods",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = androidx.compose.ui.Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                )
                            }
                        }

                        items(state.results) { food ->
                            FoodListItem(
                                food = food,
                                onClick = { viewModel.selectFood(food, state.mealType) }
                            )
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                }
                            }
                        }
                        if (!state.isSearching && state.results.isEmpty()) {
                            item {
                                Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                    Text("No foods found", color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                state.error?.let {
                    Snackbar(modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)) { Text(it) }
                }
            }
        }
    }

    // Food Detail Bottom Sheet
    state.selectedFood?.let { food ->
        FoodDetailSheet(
            food = food,
            quantityG = state.quantityG,
            mealType = state.mealType,
            isLogging = state.isLogging,
            onQuantityChanged = viewModel::onQuantityChanged,
            onMealTypeChanged = viewModel::onMealTypeChanged,
            onDismiss = viewModel::dismissFoodDetail,
            onLog = viewModel::logFood
        )
    }
}

@Composable
private fun FoodListItem(food: FoodItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(food.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(
                "${food.caloriesPer100g.toInt()} kcal · per 100g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            food.categoryName?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary) }
        }
        Text(
            "${food.defaultServingG.toInt()}g",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FoodDetailSheet(
    food: FoodItem,
    quantityG: String,
    mealType: String,
    isLogging: Boolean,
    onQuantityChanged: (String) -> Unit,
    onMealTypeChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onLog: () -> Unit
) {
    val qty = quantityG.toDoubleOrNull() ?: 0.0
    val calories = food.caloriesForServing(qty)
    val protein = food.proteinForServing(qty)
    val carbs = food.carbsForServing(qty)
    val fat = food.fatForServing(qty)

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(food.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            food.categoryName?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }

            // Nutrition preview
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    NutritionChip("${calories.toInt()}", "kcal", MaterialTheme.colorScheme.onPrimaryContainer)
                    NutritionChip("${protein.toInt()}g", "Protein", MaterialTheme.colorScheme.onPrimaryContainer)
                    NutritionChip("${carbs.toInt()}g", "Carbs", MaterialTheme.colorScheme.onPrimaryContainer)
                    NutritionChip("${fat.toInt()}g", "Fat", MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }

            // Quantity input
            OutlinedTextField(
                value = quantityG,
                onValueChange = onQuantityChanged,
                label = { Text("Quantity (grams)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                suffix = { Text("g") },
                modifier = Modifier.fillMaxWidth()
            )

            // Meal type selector
            Text("Meal Type", style = MaterialTheme.typography.labelLarge)
            val mealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(mealTypes) { type ->
                    FilterChip(
                        selected = mealType == type,
                        onClick = { onMealTypeChanged(type) },
                        label = { Text(type.lowercase().replaceFirstChar { it.uppercase() }) }
                    )
                }
            }

            Button(
                onClick = onLog,
                enabled = !isLogging && qty > 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLogging) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Add to Log")
                }
            }
        }
    }
}

@Composable
private fun NutritionChip(value: String, label: String, contentColor: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = contentColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = contentColor)
    }
}
