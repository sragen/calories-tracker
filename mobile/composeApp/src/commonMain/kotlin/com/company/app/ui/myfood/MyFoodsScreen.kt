package com.company.app.ui.myfood

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.UserFoodResponse
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.platform.isIosPlatform
import com.company.app.ui.theme.CalSnapColors
import com.company.app.ui.theme.CalSnapRadius
import com.company.app.ui.theme.CalSnapSpacing

// ─── Standalone My Foods screen (from Profile) ────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyFoodsScreen(
    viewModel: MyFoodsViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.logSuccess) {
        if (state.logSuccess) viewModel.clearLogSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Surface)
            .statusBarsPadding()
            .navigationBarsPadding(),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Header ──────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalSnapSpacing.screenPad)
                    .padding(top = 60.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(glassOrSurface())
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "chev-l", size = 18.dp, color = CalSnapColors.Ink)
                }
                Text(
                    text = "My Foods",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-0.5).sp,
                    modifier = Modifier.weight(1f),
                )
                NewFoodPill(onClick = viewModel::openCreate)
            }

            // ── Content ──────────────────────────────────────────────
            MyFoodsTabContent(
                state = state,
                onSearch = viewModel::onSearch,
                onSort = viewModel::onSort,
                onQuickAdd = viewModel::selectForQuickAdd,
                onEdit = viewModel::openEdit,
                onDelete = viewModel::deleteFood,
            )
        }

        // ── Quick-add sheet ─────────────────────────────────────────
        if (state.selectedFood != null) {
            QuickAddSheet(
                state = state,
                onDismiss = viewModel::dismissQuickAdd,
                onMealType = viewModel::setMealType,
                onMultiplier = viewModel::setMultiplier,
                onConfirm = viewModel::quickLog,
            )
        }

        // ── Create / edit sheet ─────────────────────────────────────
        if (state.isCreating) {
            CreateFoodSheet(
                editingFood = state.editingFood,
                isSaving = state.isSaving,
                onDismiss = viewModel::dismissCreate,
                onSave = viewModel::saveFood,
            )
        }
    }
}

// ─── Tab content (reused in SearchFoodScreen My Foods tab) ────────

// ─── Embeddable view (used as tab inside SearchFoodScreen) ───────

@Composable
fun MyFoodsView(
    viewModel: MyFoodsViewModel,
    onLogSuccess: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.logSuccess) {
        if (state.logSuccess) {
            onLogSuccess()
            viewModel.clearLogSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalSnapSpacing.screenPad)
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                NewFoodPill(onClick = viewModel::openCreate)
            }
            MyFoodsTabContent(
                state = state,
                onSearch = viewModel::onSearch,
                onSort = viewModel::onSort,
                onQuickAdd = viewModel::selectForQuickAdd,
                onEdit = viewModel::openEdit,
                onDelete = viewModel::deleteFood,
            )
        }
        if (state.selectedFood != null) {
            QuickAddSheet(
                state = state,
                onDismiss = viewModel::dismissQuickAdd,
                onMealType = viewModel::setMealType,
                onMultiplier = viewModel::setMultiplier,
                onConfirm = viewModel::quickLog,
            )
        }
        if (state.isCreating) {
            CreateFoodSheet(
                editingFood = state.editingFood,
                isSaving = state.isSaving,
                onDismiss = viewModel::dismissCreate,
                onSave = viewModel::saveFood,
            )
        }
    }
}

// ─── Tab content ─────────────────────────────────────────────────

@Composable
fun MyFoodsTabContent(
    state: MyFoodsState,
    onSearch: (String) -> Unit,
    onSort: (MyFoodSort) -> Unit,
    onQuickAdd: (UserFoodResponse) -> Unit,
    onEdit: (UserFoodResponse) -> Unit,
    onDelete: (Long) -> Unit,
) {
    when {
        state.isLoading && state.foods.isEmpty() -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CalSnapColors.Red)
            }
        }
        state.foods.isEmpty() && state.query.isBlank() -> {
            MyFoodsEmptyState()
        }
        else -> {
            Column(modifier = Modifier.fillMaxSize()) {

                // Glass search bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CalSnapSpacing.screenPad)
                        .padding(bottom = 10.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(glassOrSurface())
                        .padding(horizontal = 14.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CalSnapIcon(name = "search", size = 18.dp, color = CalSnapColors.Muted)
                    BasicTextField(
                        value = state.query,
                        onValueChange = onSearch,
                        singleLine = true,
                        cursorBrush = SolidColor(CalSnapColors.Red),
                        textStyle = TextStyle(
                            fontSize = 14.sp,
                            color = CalSnapColors.Ink,
                            fontWeight = FontWeight.W500,
                        ),
                        modifier = Modifier.weight(1f),
                        decorationBox = { inner ->
                            if (state.query.isEmpty()) {
                                Text(
                                    text = "Search saved foods…",
                                    fontSize = 14.sp,
                                    color = CalSnapColors.Mute2,
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
                                    onClick = { onSearch("") },
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            CalSnapIcon(name = "close", size = 10.dp, color = CalSnapColors.Background)
                        }
                    }
                }

                // Sort pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = CalSnapSpacing.screenPad)
                        .padding(bottom = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    MyFoodSort.entries.forEach { sort ->
                        val isActive = state.sort == sort
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(CalSnapRadius.pill))
                                .background(if (isActive) CalSnapColors.Ink else glassOrSurface())
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onSort(sort) },
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                        ) {
                            Text(
                                text = sort.label,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.W600,
                                color = if (isActive) Color.White else CalSnapColors.Muted,
                            )
                        }
                    }
                }

                // List
                if (state.foods.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            CalSnapIcon(name = "search", size = 40.dp, color = CalSnapColors.Hint)
                            Text(
                                text = "No results for \"${state.query}\"",
                                fontSize = 14.sp,
                                color = CalSnapColors.Muted,
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(
                            horizontal = CalSnapSpacing.screenPad,
                            vertical = 0.dp,
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(state.foods, key = { it.id }) { food ->
                            MyFoodCard(
                                food = food,
                                onQuickAdd = { onQuickAdd(food) },
                                onEdit = { onEdit(food) },
                                onDelete = { onDelete(food.id) },
                            )
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

// ─── Food card ────────────────────────────────────────────────────

@Composable
private fun MyFoodCard(
    food: UserFoodResponse,
    onQuickAdd: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    var showActions by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(18.dp), ambientColor = Color(0x0A140F08), spotColor = Color(0x0A140F08))
            .clip(RoundedCornerShape(18.dp))
            .background(glassOrSurface())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { showActions = !showActions },
            ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Avatar / photo placeholder
            FoodAvatar(name = food.foodName, imageUrl = food.imageUrl)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.foodName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 2.dp),
                ) {
                    Text(
                        text = "${food.totalCalories.toInt()}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W700,
                        color = CalSnapColors.Ink,
                    )
                    Text(
                        text = "kcal · ${food.useCount}×",
                        fontSize = 11.sp,
                        color = CalSnapColors.Muted,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 6.dp),
                ) {
                    MacroPill("P", food.totalProteinG, CalSnapColors.Protein, CalSnapColors.ProteinBg)
                    MacroPill("C", food.totalCarbsG, CalSnapColors.Carb, CalSnapColors.CarbBg)
                    MacroPill("F", food.totalFatG, CalSnapColors.Fat, CalSnapColors.FatBg)
                }
            }

            // Quick-add red button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(CalSnapColors.Red, CalSnapColors.RedDark),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        )
                    )
                    .shadow(6.dp, CircleShape, ambientColor = CalSnapColors.Red.copy(alpha = 0.4f), spotColor = CalSnapColors.Red.copy(alpha = 0.4f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onQuickAdd,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = "plus", size = 18.dp, color = Color.White, strokeWidth = 2.4f)
            }
        }

        // Action row (edit + delete) — revealed on tap
        AnimatedVisibility(
            visible = showActions,
            enter = expandVertically(tween(200)) + fadeIn(tween(200)),
            exit = shrinkVertically(tween(180)) + fadeOut(tween(180)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CalSnapColors.SurfaceAlt)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                ActionChip(
                    label = "Edit",
                    icon = "edit",
                    tint = CalSnapColors.Ink,
                    modifier = Modifier.weight(1f),
                    onClick = { showActions = false; onEdit() },
                )
                ActionChip(
                    label = "Delete",
                    icon = "close",
                    tint = CalSnapColors.Red,
                    modifier = Modifier.weight(1f),
                    onClick = { showActions = false; onDelete() },
                )
            }
        }
    }
}

@Composable
private fun FoodAvatar(name: String, imageUrl: String?) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CalSnapColors.SurfaceAlt),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initial,
            fontSize = 22.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Muted,
        )
    }
}

@Composable
private fun MacroPill(label: String, value: Double, color: Color, bg: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bg),
    ) {
        Text(
            text = "$label ${value.toInt()}g",
            fontSize = 10.sp,
            fontWeight = FontWeight.W700,
            color = color,
            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
        )
    }
}

@Composable
private fun ActionChip(label: String, icon: String, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(tint.copy(alpha = 0.08f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CalSnapIcon(name = icon, size = 14.dp, color = tint, strokeWidth = 2f)
        Spacer(Modifier.width(6.dp))
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.W600, color = tint)
    }
}

// ─── Empty state ──────────────────────────────────────────────────

@Composable
private fun MyFoodsEmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            // Glass illustration circle
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(glassOrSurface())
                    .shadow(14.dp, CircleShape, ambientColor = Color(0x1A1C1408), spotColor = Color(0x1A1C1408)),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = "star", size = 56.dp, color = CalSnapColors.Red, strokeWidth = 2f)
            }

            Spacer(Modifier.height(28.dp))

            Text(
                text = "No saved foods yet",
                fontSize = 22.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.5).sp,
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Log meals to save them here for\none-tap logging next time.",
                fontSize = 14.sp,
                color = CalSnapColors.Muted,
                lineHeight = 21.sp,
                modifier = Modifier.padding(horizontal = 12.dp),
            )
        }
    }
}

// ─── Quick-add bottom sheet ───────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddSheet(
    state: MyFoodsState,
    onDismiss: () -> Unit,
    onMealType: (String) -> Unit,
    onMultiplier: (Float) -> Unit,
    onConfirm: () -> Unit,
) {
    val food = state.selectedFood ?: return
    val multipliers = listOf(0.5f to "½×", 1f to "1×", 1.5f to "1½×", 2f to "2×")
    val mealTypes = listOf("BREAKFAST" to "🌅", "LUNCH" to "☀️", "DINNER" to "🌙", "SNACK" to "✨")

    val displayCalories = (food.totalCalories * state.quickAddMultiplier).toInt()
    val displayProtein  = (food.totalProteinG  * state.quickAddMultiplier).toInt()
    val displayCarbs    = (food.totalCarbsG    * state.quickAddMultiplier).toInt()
    val displayFat      = (food.totalFatG      * state.quickAddMultiplier).toInt()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isIosPlatform) Color(0xE0FFFDF5) else Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 2.dp)
                    .size(width = 40.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x2E0E0E0E)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            // Food header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                FoodAvatar(name = food.foodName, imageUrl = food.imageUrl)
                Column {
                    Text(
                        text = food.foodName,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.W700,
                        color = CalSnapColors.Ink,
                        letterSpacing = (-0.3).sp,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = "${food.totalCalories.toInt()} kcal · P ${food.totalProteinG.toInt()}g · C ${food.totalCarbsG.toInt()}g · F ${food.totalFatG.toInt()}g",
                        fontSize = 12.sp,
                        color = CalSnapColors.Muted,
                        modifier = Modifier.padding(top = 2.dp),
                    )
                }
            }

            // Meal type selector
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetLabel("Meal type")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    mealTypes.forEach { (type, emoji) ->
                        val isSelected = state.quickAddMealType == type
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) CalSnapColors.Ink else glassOrSurface())
                                
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onMealType(type) },
                                )
                                .padding(vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                        ) {
                            Text(text = emoji, fontSize = 18.sp)
                            Text(
                                text = type.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.W700,
                                color = if (isSelected) Color.White else CalSnapColors.Ink,
                            )
                        }
                    }
                }
            }

            // Serving multiplier
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SheetLabel("Serving size")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    multipliers.forEach { (mult, label) ->
                        val isSelected = state.quickAddMultiplier == mult
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) CalSnapColors.Red else glassOrSurface())
                                
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onMultiplier(mult) },
                                )
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = label,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W700,
                                color = if (isSelected) Color.White else CalSnapColors.Ink,
                            )
                        }
                    }
                }
            }

            // Live total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x0A0E0E0E))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = "ADDING",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                        color = CalSnapColors.Muted,
                        letterSpacing = 0.5.sp,
                    )
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 2.dp),
                    ) {
                        Text(
                            text = "$displayCalories",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.W700,
                            color = CalSnapColors.Ink,
                            letterSpacing = (-0.5).sp,
                        )
                        Text(text = "kcal", fontSize = 12.sp, color = CalSnapColors.Muted)
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        Triple("P", "$displayProtein g", CalSnapColors.Protein),
                        Triple("C", "$displayCarbs g", CalSnapColors.Carb),
                        Triple("F", "$displayFat g", CalSnapColors.Fat),
                    ).forEach { (l, v, c) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                                .padding(horizontal = 9.dp, vertical = 5.dp),
                        ) {
                            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(text = l, fontSize = 11.sp, fontWeight = FontWeight.W700, color = c)
                                Text(text = v, fontSize = 11.sp, fontWeight = FontWeight.W700, color = CalSnapColors.Ink)
                            }
                        }
                    }
                }
            }

            // CTA
            val mealLabel = state.quickAddMealType.lowercase().replaceFirstChar { it.uppercase() }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(CalSnapColors.Red, CalSnapColors.RedDark),
                            start = Offset(0f, 0f),
                            end = Offset(Float.POSITIVE_INFINITY, 0f),
                        )
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { if (!state.isLogging) onConfirm() },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isLogging) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CalSnapIcon(name = "plus", size = 18.dp, color = Color.White, strokeWidth = 2.4f)
                        Text(
                            text = "Add to $mealLabel",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W700,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

// ─── Create / Edit bottom sheet ───────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateFoodSheet(
    editingFood: UserFoodResponse?,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Double, Double, Double, Double, Double) -> Unit,
) {
    val isEdit = editingFood != null

    var name     by remember(editingFood) { mutableStateOf(editingFood?.foodName ?: "") }
    var calories by remember(editingFood) { mutableStateOf(editingFood?.totalCalories?.toInt()?.toString() ?: "") }
    var protein  by remember(editingFood) { mutableStateOf(editingFood?.totalProteinG?.toInt()?.toString() ?: "") }
    var carbs    by remember(editingFood) { mutableStateOf(editingFood?.totalCarbsG?.toInt()?.toString() ?: "") }
    var fat      by remember(editingFood) { mutableStateOf(editingFood?.totalFatG?.toInt()?.toString() ?: "") }
    var serving  by remember(editingFood) { mutableStateOf(editingFood?.servingSizeG?.toInt()?.toString() ?: "100") }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = if (isIosPlatform) Color(0xE0FFFDF5) else Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 14.dp, bottom = 2.dp)
                    .size(width = 40.dp, height = 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0x2E0E0E0E)),
            )
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CalSnapColors.RedSoft),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "star", size = 20.dp, color = CalSnapColors.Red, strokeWidth = 2f)
                }
                Column {
                    Text(
                        text = if (isEdit) "Edit food" else "New food",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.W700,
                        color = CalSnapColors.Ink,
                        letterSpacing = (-0.3).sp,
                    )
                    Text(
                        text = "Nutrition per serving",
                        fontSize = 12.sp,
                        color = CalSnapColors.Muted,
                    )
                }
            }

            // Name field
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SheetLabel("Food name")
                FormField(
                    value = name,
                    onValueChange = { name = it },
                    placeholder = "e.g. Chicken Caesar Wrap",
                    keyboardType = KeyboardType.Text,
                )
            }

            // Macros 2×2 grid
            SheetLabel("Nutrition (per serving)")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MacroField(
                        label = "Calories",
                        unit = "kcal",
                        color = CalSnapColors.Ink,
                        value = calories,
                        onValueChange = { calories = it },
                        modifier = Modifier.weight(1f),
                    )
                    MacroField(
                        label = "Protein",
                        unit = "g",
                        color = CalSnapColors.Protein,
                        value = protein,
                        onValueChange = { protein = it },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    MacroField(
                        label = "Carbs",
                        unit = "g",
                        color = CalSnapColors.Carb,
                        value = carbs,
                        onValueChange = { carbs = it },
                        modifier = Modifier.weight(1f),
                    )
                    MacroField(
                        label = "Fat",
                        unit = "g",
                        color = CalSnapColors.Fat,
                        value = fat,
                        onValueChange = { fat = it },
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Serving size
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                SheetLabel("Serving size (g)")
                FormField(
                    value = serving,
                    onValueChange = { serving = it },
                    placeholder = "100",
                    keyboardType = KeyboardType.Number,
                )
            }

            // Hint
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x14F4A23A))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "💡", fontSize = 16.sp)
                Text(
                    text = "Enter the total nutrition for one serving, not per 100g.",
                    fontSize = 12.sp,
                    color = CalSnapColors.Ink,
                    lineHeight = 18.sp,
                )
            }

            // Save CTA
            val canSave = name.isNotBlank() && calories.isNotBlank()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(27.dp))
                    .background(
                        if (canSave)
                            Brush.linearGradient(
                                listOf(CalSnapColors.Red, CalSnapColors.RedDark),
                                start = Offset(0f, 0f),
                                end = Offset(Float.POSITIVE_INFINITY, 0f),
                            )
                        else
                            Brush.linearGradient(listOf(CalSnapColors.Hint, CalSnapColors.Hint))
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = canSave && !isSaving,
                        onClick = {
                            val cal  = calories.toDoubleOrNull() ?: return@clickable
                            val prot = protein.toDoubleOrNull() ?: 0.0
                            val crb  = carbs.toDoubleOrNull() ?: 0.0
                            val ft   = fat.toDoubleOrNull() ?: 0.0
                            val srv  = serving.toDoubleOrNull()?.coerceAtLeast(1.0) ?: 100.0
                            // convert serving totals → per100g
                            val factor = 100.0 / srv
                            onSave(name.trim(), cal * factor, prot * factor, crb * factor, ft * factor, srv)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        CalSnapIcon(name = "star", size = 18.dp, color = Color.White, strokeWidth = 2.2f)
                        Text(
                            text = if (isEdit) "Save changes" else "Save to library",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W700,
                            color = Color.White,
                        )
                    }
                }
            }
        }
    }
}

// ─── Helpers ──────────────────────────────────────────────────────

@Composable
fun NewFoodPill(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(CalSnapRadius.pill))
            .background(glassOrSurface())
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CalSnapIcon(name = "plus", size = 14.dp, color = CalSnapColors.Ink, strokeWidth = 2.2f)
        Text(text = "New", fontSize = 12.sp, fontWeight = FontWeight.W600, color = CalSnapColors.Ink)
    }
}

@Composable
fun LogMealTabStrip(
    activeTab: LogMealTab,
    onSearch: () -> Unit,
    onAiScan: () -> Unit,
    onBarcode: () -> Unit,
    onMyFoods: () -> Unit,
) {
    val tabs = listOf(
        Triple(LogMealTab.SEARCH, "search", "Search"),
        Triple(LogMealTab.AI_SCAN, "sparkle", "AI Scan"),
        Triple(LogMealTab.MY_FOODS, "star", "My Foods"),
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(glassOrSurface())
            .padding(6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tabs.forEach { (tab, icon, label) ->
            val isSelected = activeTab == tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) CalSnapColors.Ink else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            when (tab) {
                                LogMealTab.SEARCH   -> onSearch()
                                LogMealTab.AI_SCAN  -> onAiScan()
                                LogMealTab.MY_FOODS -> onMyFoods()
                            }
                        },
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    CalSnapIcon(
                        name = icon,
                        size = 15.dp,
                        color = if (isSelected) Color.White else CalSnapColors.Mute2,
                        strokeWidth = 2f,
                    )
                    Text(
                        text = label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W600,
                        color = if (isSelected) Color.White else CalSnapColors.Mute2,
                    )
                }
            }
        }
    }
}

enum class LogMealTab { SEARCH, AI_SCAN, MY_FOODS }

@Composable
private fun SheetLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.W700,
        color = CalSnapColors.Muted,
        letterSpacing = 0.7.sp,
    )
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(glassOrSurface())
            .padding(horizontal = 14.dp, vertical = 12.dp),
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(CalSnapColors.Red),
            textStyle = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.W600, color = CalSnapColors.Ink),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            decorationBox = { inner ->
                if (value.isEmpty()) {
                    Text(text = placeholder, fontSize = 15.sp, color = CalSnapColors.Hint)
                }
                inner()
            },
        )
    }
}

@Composable
private fun MacroField(
    label: String,
    unit: String,
    color: Color,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(glassOrSurface())
            .padding(10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(color))
            Text(
                text = label.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.W600,
                color = CalSnapColors.Muted,
                letterSpacing = 0.5.sp,
            )
        }
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                cursorBrush = SolidColor(CalSnapColors.Red),
                textStyle = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-0.5).sp,
                ),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.widthIn(max = 80.dp),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(text = "0", fontSize = 22.sp, fontWeight = FontWeight.W700, color = CalSnapColors.Hint)
                    }
                    inner()
                },
            )
            Text(text = unit, fontSize = 12.sp, color = CalSnapColors.Muted)
        }
    }
}

// Glass/card surface helpers — shared within this package
@Composable
internal fun glassOrSurface(): Color =
    if (isIosPlatform) Color.White.copy(alpha = 0.55f) else CalSnapColors.SurfaceAlt
