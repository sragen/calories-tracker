package com.company.app.ui.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.FoodItem
import com.company.app.ui.components.*
import com.company.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchFoodScreen(
    viewModel: SearchFoodViewModel,
    initialMealType: String,
    onBack: () -> Unit,
    onLogSuccess: () -> Unit,
    onOpenBarcodeScanner: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(initialMealType) {
        if (state.mealType == "SNACK" && initialMealType != "SNACK") {
            viewModel.onMealTypeChanged(initialMealType)
        }
    }

    LaunchedEffect(state.logSuccess) {
        if (state.logSuccess) onLogSuccess()
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val info = listState.layoutInfo
            val lastVisible = (info.visibleItemsInfo.lastOrNull()?.index ?: 0) + 1
            lastVisible >= info.totalItemsCount - 3
        }
    }
    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) viewModel.loadMore()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Search header ────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalSnapColors.Background)
                    .padding(horizontal = CalSnapSpacing.screenPad)
                    .padding(top = CalSnapSpacing.lg, bottom = CalSnapSpacing.sm),
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

                // Search field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .clip(RoundedCornerShape(CalSnapRadius.pill))
                        .background(CalSnapColors.SurfaceAlt)
                        .padding(horizontal = CalSnapSpacing.md),
                    contentAlignment = Alignment.CenterStart,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
                    ) {
                        CalSnapIcon(name = "search", size = 16.dp, color = CalSnapColors.Muted)
                        BasicTextField(
                            value = state.query,
                            onValueChange = viewModel::onQueryChanged,
                            singleLine = true,
                            textStyle = CalSnapType.Body.copy(color = CalSnapColors.Ink),
                            modifier = Modifier
                                .weight(1f)
                                .focusRequester(focusRequester),
                            decorationBox = { inner ->
                                if (state.query.isEmpty()) {
                                    Text(
                                        text = "Search foods…",
                                        style = CalSnapType.Body,
                                        color = CalSnapColors.Hint,
                                    )
                                }
                                inner()
                            },
                        )
                        if (state.query.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(CalSnapColors.Hint)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null,
                                        onClick = { viewModel.onQueryChanged("") },
                                    ),
                                contentAlignment = Alignment.Center,
                            ) {
                                CalSnapIcon(name = "close", size = 10.dp, color = CalSnapColors.Background)
                            }
                        }
                    }
                }

                // Barcode scan button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(CalSnapRadius.sm))
                        .background(CalSnapColors.SurfaceAlt)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onOpenBarcodeScanner,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "barcode", size = 18.dp, color = CalSnapColors.Ink)
                }
            }

            // ── Meal type chips ──────────────────────────────────────
            LazyRow(
                contentPadding = PaddingValues(horizontal = CalSnapSpacing.screenPad, vertical = CalSnapSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
            ) {
                val mealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
                items(mealTypes) { type ->
                    val isSelected = state.mealType == type
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(CalSnapRadius.pill))
                            .background(if (isSelected) CalSnapColors.Ink else CalSnapColors.SurfaceAlt)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { viewModel.onMealTypeChanged(type) },
                            )
                            .padding(horizontal = CalSnapSpacing.md, vertical = 6.dp),
                    ) {
                        Text(
                            text = type.lowercase().replaceFirstChar { it.uppercase() },
                            style = CalSnapType.Label,
                            color = if (isSelected) CalSnapColors.Background else CalSnapColors.Muted,
                        )
                    }
                }
            }

            // ── Category chips ───────────────────────────────────────
            if (state.categories.isNotEmpty()) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = CalSnapSpacing.screenPad, vertical = CalSnapSpacing.xs),
                    horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
                ) {
                    item {
                        CategoryChip("All", state.selectedCategoryId == null) {
                            viewModel.onCategorySelected(null)
                        }
                    }
                    items(state.categories) { cat ->
                        CategoryChip(cat.name, state.selectedCategoryId == cat.id) {
                            viewModel.onCategorySelected(cat.id)
                        }
                    }
                }
            }

            // ── Results list ─────────────────────────────────────────
            Box(modifier = Modifier.weight(1f)) {
                if (state.isSearching && state.results.isEmpty()) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = CalSnapColors.Red,
                    )
                } else {
                    LazyColumn(state = listState) {
                        if (state.query.isBlank() && state.recentFoods.isNotEmpty()) {
                            item {
                                SectionLabel("Recent")
                            }
                            items(state.recentFoods, key = { "recent_${it.id}" }) { food ->
                                FoodListItem(
                                    food = food,
                                    onClick = { viewModel.selectFood(food, state.mealType) },
                                )
                            }
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .padding(horizontal = CalSnapSpacing.screenPad)
                                        .background(CalSnapColors.Divider),
                                )
                            }
                            item { SectionLabel("All Foods") }
                        }

                        items(state.results, key = { it.id }) { food ->
                            FoodListItem(
                                food = food,
                                onClick = { viewModel.selectFood(food, state.mealType) },
                            )
                        }

                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(CalSnapSpacing.md),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = CalSnapColors.Red,
                                        strokeWidth = 2.dp,
                                    )
                                }
                            }
                        }

                        if (!state.isSearching && state.results.isEmpty()) {
                            item {
                                EmptySearchState(
                                    query = state.query,
                                    onSubmitNew = onOpenBarcodeScanner,
                                )
                            }
                        }
                    }
                }
            }
        }

        // Error snackbar
        state.error?.let {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(CalSnapSpacing.md)
                    .clip(RoundedCornerShape(CalSnapRadius.md))
                    .background(CalSnapColors.Ink)
                    .padding(horizontal = CalSnapSpacing.md, vertical = CalSnapSpacing.sm),
            ) {
                Text(it, style = CalSnapType.Body, color = CalSnapColors.Background)
            }
        }
    }

    // ── Food detail bottom sheet ─────────────────────────────────────
    state.selectedFood?.let { food ->
        FoodDetailSheet(
            food = food,
            quantityG = state.quantityG,
            mealType = state.mealType,
            isLogging = state.isLogging,
            onQuantityChanged = viewModel::onQuantityChanged,
            onMealTypeChanged = viewModel::onMealTypeChanged,
            onDismiss = viewModel::dismissFoodDetail,
            onLog = viewModel::logFood,
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = CalSnapType.Label,
        color = CalSnapColors.Muted,
        modifier = Modifier.padding(
            horizontal = CalSnapSpacing.screenPad,
            vertical = CalSnapSpacing.sm,
        ),
    )
}

@Composable
private fun CategoryChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(CalSnapRadius.pill))
            .background(if (isSelected) CalSnapColors.Ink else CalSnapColors.SurfaceAlt)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = CalSnapSpacing.md, vertical = 6.dp),
    ) {
        Text(
            text = label,
            style = CalSnapType.Label,
            color = if (isSelected) CalSnapColors.Background else CalSnapColors.Muted,
        )
    }
}

@Composable
private fun FoodListItem(food: FoodItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = CalSnapSpacing.screenPad, vertical = CalSnapSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalSnapFoodPhoto(name = food.name, size = 44.dp, cornerRadius = CalSnapRadius.md)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                style = CalSnapType.BodyLarge,
                color = CalSnapColors.Ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "${food.caloriesPer100g.toInt()} kcal/100g",
                    style = CalSnapType.BodySmall,
                    color = CalSnapColors.Muted,
                )
                food.categoryName?.let { cat ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(CalSnapRadius.pill))
                            .background(CalSnapColors.SurfaceAlt)
                            .padding(horizontal = 6.dp, vertical = 1.dp),
                    ) {
                        Text(cat, style = CalSnapType.Label, color = CalSnapColors.Muted)
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${food.defaultServingG.toInt()}g",
                style = CalSnapType.BodySmall,
                color = CalSnapColors.Ink,
            )
            Text(
                text = "serving",
                style = CalSnapType.Label,
                color = CalSnapColors.Hint,
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .padding(horizontal = CalSnapSpacing.screenPad)
            .background(CalSnapColors.Divider),
    )
}

@Composable
private fun EmptySearchState(query: String, onSubmitNew: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad, vertical = CalSnapSpacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
    ) {
        Text("🔍", fontSize = 36.sp)
        Text(
            text = if (query.isBlank()) "No foods yet" else "No results for \"$query\"",
            style = CalSnapType.HeadlineMedium,
            color = CalSnapColors.Ink,
        )
        Text(
            text = "Can't find what you're looking for?",
            style = CalSnapType.Body,
            color = CalSnapColors.Muted,
        )
        Spacer(Modifier.height(CalSnapSpacing.sm))
        CalSnapPrimaryButton(
            text = "Submit New Food",
            onClick = onSubmitNew,
            modifier = Modifier.fillMaxWidth(0.7f),
        )
    }
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
    onLog: () -> Unit,
) {
    val qty = quantityG.toDoubleOrNull() ?: 0.0
    val calories = food.caloriesForServing(qty)
    val protein = food.proteinForServing(qty)
    val carbs = food.carbsForServing(qty)
    val fat = food.fatForServing(qty)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CalSnapColors.Background,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = CalSnapSpacing.sm)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(CalSnapRadius.pill))
                    .background(CalSnapColors.Divider),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = CalSnapSpacing.screenPad)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
        ) {
            // Food header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
            ) {
                CalSnapFoodPhoto(name = food.name, size = 56.dp, cornerRadius = CalSnapRadius.lg)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = food.name,
                        style = CalSnapType.HeadlineMedium,
                        color = CalSnapColors.Ink,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    food.categoryName?.let {
                        Text(it, style = CalSnapType.BodySmall, color = CalSnapColors.Muted)
                    }
                }
            }

            // Macro row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CalSnapRadius.card))
                    .background(CalSnapColors.Surface)
                    .padding(vertical = CalSnapSpacing.md),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                MacroStatCell(value = "${calories.toInt()}", label = "kcal", highlight = true)
                VerticalDivider()
                MacroStatCell(value = "${protein.toInt()}g", label = "Protein")
                VerticalDivider()
                MacroStatCell(value = "${carbs.toInt()}g", label = "Carbs")
                VerticalDivider()
                MacroStatCell(value = "${fat.toInt()}g", label = "Fat")
            }

            // Quantity stepper
            Text("PORTION", style = CalSnapType.Label, color = CalSnapColors.Muted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
            ) {
                QuantityStepButton("-") {
                    val cur = quantityG.toDoubleOrNull() ?: 100.0
                    onQuantityChanged((cur - 10.0).coerceAtLeast(10.0).toInt().toString())
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(CalSnapRadius.md))
                        .background(CalSnapColors.SurfaceAlt),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(3.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        Text(
                            text = quantityG.ifEmpty { "0" },
                            style = CalSnapType.HeadlineMedium,
                            color = CalSnapColors.Ink,
                        )
                        Text(
                            "g",
                            style = CalSnapType.Body,
                            color = CalSnapColors.Muted,
                            modifier = Modifier.padding(bottom = 2.dp),
                        )
                    }
                }
                QuantityStepButton("+") {
                    val cur = quantityG.toDoubleOrNull() ?: 100.0
                    onQuantityChanged((cur + 10.0).toInt().toString())
                }
            }

            // Meal type chips
            Text("MEAL", style = CalSnapType.Label, color = CalSnapColors.Muted)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
            ) {
                listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK").forEach { type ->
                    val isSelected = mealType == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(38.dp)
                            .clip(RoundedCornerShape(CalSnapRadius.md))
                            .background(if (isSelected) CalSnapColors.Ink else CalSnapColors.SurfaceAlt)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onMealTypeChanged(type) },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = type.lowercase().replaceFirstChar { it.uppercase() },
                            style = CalSnapType.Label,
                            color = if (isSelected) CalSnapColors.Background else CalSnapColors.Muted,
                        )
                    }
                }
            }

            CalSnapPrimaryButton(
                text = if (isLogging) "Logging…" else "Add to Log",
                onClick = onLog,
                enabled = !isLogging && qty > 0,
            )
        }
    }
}

@Composable
private fun MacroStatCell(value: String, label: String, highlight: Boolean = false) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = CalSnapType.HeadlineMedium,
            color = if (highlight) CalSnapColors.Ink else CalSnapColors.Ink,
        )
        Text(
            text = label,
            style = CalSnapType.Label,
            color = CalSnapColors.Muted,
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(CalSnapColors.Divider),
    )
}

@Composable
private fun QuantityStepButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(CalSnapRadius.md))
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
            style = CalSnapType.HeadlineMedium,
            color = CalSnapColors.Ink,
        )
    }
}
