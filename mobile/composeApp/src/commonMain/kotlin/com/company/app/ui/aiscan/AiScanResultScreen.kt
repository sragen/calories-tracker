package com.company.app.ui.aiscan

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.AiDetectedFood
import com.company.app.shared.data.model.DailyGoalResponse
import com.company.app.ui.components.*
import com.company.app.ui.platform.decodeImageBitmap
import com.company.app.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.math.roundToInt

private val MealTypes = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiScanResultScreen(
    viewModel: AiScanViewModel,
    mealType: String = "LUNCH",
    isGuestMode: Boolean = false,
    onConfirmed: () -> Unit,
    onBack: () -> Unit,
    onReSnap: () -> Unit = onBack,
    onRegisterFromGuest: () -> Unit = {},
) {
    val state = viewModel.state
    val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
    var selectedMealType by remember(mealType) { mutableStateOf(mealType.uppercase()) }
    var refineFood by remember { mutableStateOf<AiDetectedFood?>(null) }

    LaunchedEffect(state.confirmed) { if (state.confirmed) onConfirmed() }

    val photo: ImageBitmap? = remember(state.imageBytes) {
        state.imageBytes?.decodeImageBitmap()
    }

    when {
        state.isAnalyzing -> AnalyzingScreen(photo = photo)
        state.scanResult == null -> EmptyScanContent(error = state.error, onBack = onBack)
        else -> {
            ResultContent(
                photo = photo,
                detected = state.scanResult.detectedFoods,
                selectedFoods = state.selectedFoods,
                goal = state.goal,
                selectedMealType = selectedMealType,
                onMealTypeChange = { selectedMealType = it },
                isConfirming = state.isConfirming,
                isGuestMode = isGuestMode,
                error = state.error,
                onBack = onBack,
                onReSnap = onReSnap,
                onItemTap = { refineFood = it },
                onLog = { viewModel.confirm(selectedMealType, today) },
                onRegister = onRegisterFromGuest,
            )
        }
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val sheetScope = rememberCoroutineScope()
    val activeFood = refineFood
    if (activeFood != null) {
        ModalBottomSheet(
            onDismissRequest = { refineFood = null },
            sheetState = sheetState,
            containerColor = CalSnapColors.Background,
        ) {
            val current = state.selectedFoods.firstOrNull {
                it.name == activeFood.name && it.matchedFoodId == activeFood.matchedFoodId
            } ?: activeFood
            RefinePortionSheet(
                food = current,
                onPortionChange = { viewModel.updatePortion(activeFood, it) },
                onDone = {
                    sheetScope.launch {
                        runCatching { sheetState.hide() }
                        refineFood = null
                    }
                },
            )
        }
    }
}

// ─── Analyzing screen ───────────────────────────────────────────────────────

@Composable
private fun AnalyzingScreen(photo: ImageBitmap?) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0A0A)),
    ) {
        // Captured photo
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        }
        // Dark overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f)),
        )

        AnalyzingScanLine()
        AnalyzingDetectionMarkers()
        AnalyzingBottomPanel(
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun AnalyzingScanLine() {
    val transition = rememberInfiniteTransition(label = "scanLine")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "scanLineY",
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val h = maxHeight
        Box(
            modifier = Modifier
                .padding(horizontal = 40.dp)
                .fillMaxWidth()
                .height(2.dp)
                .offset(y = h * progress)
                .background(
                    Brush.horizontalGradient(
                        listOf(Color.Transparent, CalSnapColors.Red, Color.Transparent),
                    ),
                ),
        )
    }
}

@Composable
private fun AnalyzingDetectionMarkers() {
    val transition = rememberInfiniteTransition(label = "markers")
    val pulse by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )
    val markers = listOf(
        Triple(0.28f, 0.36f, 0),
        Triple(0.60f, 0.28f, 220),
        Triple(0.45f, 0.60f, 440),
    )
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val w = maxWidth
        val h = maxHeight
        markers.forEach { (lf, tf, delayMs) ->
            var visible by remember { mutableStateOf(false) }
            LaunchedEffect(Unit) {
                delay(delayMs.toLong())
                visible = true
            }
            if (visible) {
                Box(
                    modifier = Modifier
                        .offset(x = w * lf, y = h * tf)
                        .alpha(pulse)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(CalSnapColors.Red),
                        )
                        Text(
                            text = "Detecting…",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.W600,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyzingBottomPanel(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "panel")
    val pulseAlpha by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "iconPulse",
    )
    val pulseScale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "iconScale",
    )
    val progress by transition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "progressBar",
    )

    val steps = remember {
        listOf(
            "Detecting food items" to 0,
            "Estimating portion size" to 800,
            "Calculating macros" to 1600,
        )
    }
    val stepProgress = remember { mutableStateListOf(false, false, false) }
    LaunchedEffect(Unit) {
        steps.forEachIndexed { i, (_, delayMs) ->
            delay(delayMs.toLong())
            if (i < stepProgress.size) stepProgress[i] = true
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(Color(0xFF14100A).copy(alpha = 0.92f))
            .padding(horizontal = 28.dp)
            .padding(top = 32.dp, bottom = 60.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .scale(pulseScale)
                    .alpha(pulseAlpha)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CalSnapColors.Red),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = "sparkle", size = 20.dp, color = Color.White, strokeWidth = 2.2f)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Analyzing your meal…",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.W700,
                    letterSpacing = (-0.4).sp,
                )
                Text(
                    text = "Identifying ingredients",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.1f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(CalSnapColors.Red),
            )
        }

        Spacer(Modifier.height(16.dp))

        steps.forEachIndexed { i, (label, _) ->
            val done = stepProgress.getOrNull(i) == true
            Row(
                modifier = Modifier.padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(if (done) CalSnapColors.Good else Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center,
                ) {
                    if (done) {
                        CalSnapIcon(name = "check", size = 10.dp, color = Color.White, strokeWidth = 3f)
                    }
                }
                Text(
                    text = label,
                    color = if (done) Color.White else Color.White.copy(alpha = 0.5f),
                    fontSize = 13.sp,
                )
            }
        }
    }
}

// ─── Result content ─────────────────────────────────────────────────────────

@Composable
private fun ResultContent(
    photo: ImageBitmap?,
    detected: List<AiDetectedFood>,
    selectedFoods: List<AiDetectedFood>,
    goal: DailyGoalResponse?,
    selectedMealType: String,
    onMealTypeChange: (String) -> Unit,
    isConfirming: Boolean,
    isGuestMode: Boolean,
    error: String?,
    onBack: () -> Unit,
    onReSnap: () -> Unit,
    onItemTap: (AiDetectedFood) -> Unit,
    onLog: () -> Unit,
    onRegister: () -> Unit,
) {
    val totalKcal = selectedFoods.sumOf { it.effectiveCalories() }
    val totalProtein = selectedFoods.sumOf { it.proteinPer100g * it.portionG / 100.0 }
    val totalCarbs = selectedFoods.sumOf { it.carbsPer100g * it.portionG / 100.0 }
    val totalFat = selectedFoods.sumOf { it.fatPer100g * it.portionG / 100.0 }
    val totalGrams = selectedFoods.sumOf { it.portionG }.roundToInt()
    val title = detected.firstOrNull()?.name?.replaceFirstChar { it.uppercase() } ?: "Your meal"
    val itemsLabel = "${detected.size} item${if (detected.size != 1) "s" else ""} · ≈ ${totalGrams}g"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 140.dp),
        ) {
            item {
                ResultPhotoHeader(photo = photo, onBack = onBack)
            }
            item {
                ResultSummaryCard(
                    title = title,
                    itemsLabel = itemsLabel,
                    totalKcal = totalKcal,
                    goal = goal,
                    totalProtein = totalProtein,
                    totalCarbs = totalCarbs,
                    totalFat = totalFat,
                )
            }
            item {
                Spacer(Modifier.height(22.dp))
                DetectedItemsHeader()
            }
            items(detected, key = { "${it.name}_${it.matchedFoodId}" }) { food ->
                val current = selectedFoods.firstOrNull {
                    it.name == food.name && it.matchedFoodId == food.matchedFoodId
                } ?: food
                DetectedItemRow(food = current, onTap = { onItemTap(food) })
            }
            item {
                Spacer(Modifier.height(18.dp))
                MealTypeSegmented(
                    selected = selectedMealType,
                    onChange = onMealTypeChange,
                    modifier = Modifier.padding(horizontal = 22.dp),
                )
            }
            error?.let {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 22.dp, vertical = 12.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CalSnapColors.RedSoft)
                            .padding(12.dp),
                    ) {
                        Text(
                            text = it,
                            color = CalSnapColors.Red,
                            fontSize = 13.sp,
                        )
                    }
                }
            }
        }

        ResultCtaBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            mealType = selectedMealType,
            isConfirming = isConfirming,
            enabled = selectedFoods.isNotEmpty() && !isConfirming,
            isGuestMode = isGuestMode,
            onReSnap = onReSnap,
            onLog = onLog,
            onRegister = onRegister,
        )
    }
}

private fun AiDetectedFood.effectiveCalories(): Double =
    if (totalCalories > 0.0) totalCalories else caloriesPer100g * portionG / 100.0

@Composable
private fun ResultPhotoHeader(photo: ImageBitmap?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
    ) {
        if (photo != null) {
            Image(
                bitmap = photo,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2A1808)),
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.6f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.25f),
                        ),
                    ),
                ),
        )

        // Back
        Box(
            modifier = Modifier
                .padding(top = 60.dp, start = 20.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.25f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "chev-l", size = 18.dp, color = Color.White, strokeWidth = 2.2f)
        }

        // AI confidence pill
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 60.dp, end = 20.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CalSnapIcon(name = "sparkle", size = 12.dp, color = Color.White, strokeWidth = 2.4f)
            Text(
                text = "AI · 94%",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.W600,
            )
        }
    }
}

@Composable
private fun ResultSummaryCard(
    title: String,
    itemsLabel: String,
    totalKcal: Double,
    goal: DailyGoalResponse?,
    totalProtein: Double,
    totalCarbs: Double,
    totalFat: Double,
) {
    val animKcal = remember { Animatable(0f) }
    LaunchedEffect(totalKcal) {
        animKcal.animateTo(totalKcal.toFloat(), tween(700, easing = FastOutSlowInEasing))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-28).dp)
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(CalSnapColors.Background)
            .padding(horizontal = 22.dp)
            .padding(top = 24.dp, bottom = 4.dp),
    ) {
        Text(
            text = title,
            fontSize = 26.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.6).sp,
            lineHeight = 30.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = itemsLabel,
            fontSize = 14.sp,
            color = CalSnapColors.Muted,
            modifier = Modifier.padding(top = 6.dp),
        )

        Spacer(Modifier.height(20.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = animKcal.value.roundToInt().toString(),
                fontSize = 80.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-3.5).sp,
                lineHeight = 80.sp,
            )
            Text(
                text = "kcal",
                fontSize = 14.sp,
                color = CalSnapColors.Muted,
                fontWeight = FontWeight.W500,
                modifier = Modifier.padding(bottom = 12.dp),
            )
            Spacer(Modifier.weight(1f))
            GoalPill(totalKcal = totalKcal, goal = goal, modifier = Modifier.padding(bottom = 12.dp))
        }

        Spacer(Modifier.height(22.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MacroCard(
                label = "Protein",
                value = totalProtein,
                target = goal?.targetProteinG ?: 100.0,
                color = CalSnapColors.Protein,
                bg = CalSnapColors.ProteinBg,
                modifier = Modifier.weight(1f),
            )
            MacroCard(
                label = "Carbs",
                value = totalCarbs,
                target = goal?.targetCarbsG ?: 250.0,
                color = CalSnapColors.Carb,
                bg = CalSnapColors.CarbBg,
                modifier = Modifier.weight(1f),
            )
            MacroCard(
                label = "Fat",
                value = totalFat,
                target = goal?.targetFatG ?: 70.0,
                color = CalSnapColors.Fat,
                bg = CalSnapColors.FatBg,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun GoalPill(totalKcal: Double, goal: DailyGoalResponse?, modifier: Modifier = Modifier) {
    val target = goal?.targetCalories ?: return
    if (target <= 0) return
    val pct = (totalKcal / target * 100).roundToInt()
    val under = pct <= 100
    val arrow = if (under) "↓" else "↑"
    val label = if (under) "${100 - pct}% under goal" else "${pct - 100}% over goal"
    val (fg, bg) = if (under) CalSnapColors.Good to CalSnapColors.GoodBg
                   else CalSnapColors.Red to CalSnapColors.RedSoft
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(bg)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Text(
            text = arrow,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.W700,
        )
        Text(
            text = label,
            color = fg,
            fontSize = 11.sp,
            fontWeight = FontWeight.W700,
        )
    }
}

@Composable
private fun MacroCard(
    label: String,
    value: Double,
    target: Double,
    color: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    val pct = (value / target.coerceAtLeast(1.0)).coerceIn(0.0, 1.0).toFloat()
    val animPct by animateFloatAsState(
        targetValue = pct,
        animationSpec = tween(700, easing = FastOutSlowInEasing),
        label = "macroBar",
    )
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(bg)
            .padding(14.dp),
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            color = CalSnapColors.Muted,
            fontWeight = FontWeight.W600,
            letterSpacing = 0.5.sp,
        )
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
            Text(
                text = value.roundToInt().toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.5).sp,
            )
            Text(
                text = "g",
                fontSize = 12.sp,
                color = CalSnapColors.Muted,
                fontWeight = FontWeight.W500,
                modifier = Modifier.padding(start = 1.dp, bottom = 3.dp),
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.White.copy(alpha = 0.6f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animPct)
                    .fillMaxHeight()
                    .background(color),
            )
        }
    }
}

@Composable
private fun DetectedItemsHeader() {
    Text(
        text = "DETECTED ITEMS · TAP TO REFINE",
        fontSize = 12.sp,
        color = CalSnapColors.Muted,
        fontWeight = FontWeight.W600,
        letterSpacing = 0.6.sp,
        modifier = Modifier.padding(start = 22.dp, end = 22.dp, bottom = 8.dp),
    )
}

@Composable
private fun DetectedItemRow(food: AiDetectedFood, onTap: () -> Unit) {
    val kcal = food.effectiveCalories()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onTap,
            )
            .padding(horizontal = 22.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CalSnapColors.ProteinBg),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "check", size = 12.dp, color = CalSnapColors.Red, strokeWidth = 3f)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = food.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.W600,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.2).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "${food.portionG.roundToInt()} g",
                fontSize = 12.sp,
                color = CalSnapColors.Muted,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        Text(
            text = "${kcal.roundToInt()} kcal",
            fontSize = 13.sp,
            fontWeight = FontWeight.W600,
            color = CalSnapColors.Ink,
        )
        CalSnapIcon(name = "chev-r", size = 14.dp, color = CalSnapColors.Mute2)
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 22.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(CalSnapColors.Divider),
    )
}

@Composable
private fun MealTypeSegmented(
    selected: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CalSnapColors.SurfaceAlt)
            .padding(4.dp),
    ) {
        MealTypes.forEach { type ->
            val isSelected = type == selected
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .then(
                        if (isSelected) Modifier.shadow(
                            elevation = 1.dp,
                            shape = RoundedCornerShape(9.dp),
                            ambientColor = Color.Black.copy(alpha = 0.08f),
                            spotColor = Color.Black.copy(alpha = 0.08f),
                        ) else Modifier
                    )
                    .background(if (isSelected) CalSnapColors.Background else Color.Transparent)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onChange(type) },
                    )
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = type.lowercase().replaceFirstChar { it.uppercase() },
                    color = if (isSelected) CalSnapColors.Ink else CalSnapColors.Muted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W600,
                )
            }
        }
    }
}

@Composable
private fun ResultCtaBar(
    modifier: Modifier,
    mealType: String,
    isConfirming: Boolean,
    enabled: Boolean,
    isGuestMode: Boolean,
    onReSnap: () -> Unit,
    onLog: () -> Unit,
    onRegister: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colorStops = arrayOf(
                        0f to Color.Transparent,
                        0.3f to CalSnapColors.Background,
                    ),
                ),
            )
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
    ) {
        if (isGuestMode) {
            CalSnapPrimaryButton(
                text = "Create account & save",
                onClick = onRegister,
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(CalSnapColors.SurfaceAlt)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isConfirming,
                            onClick = onReSnap,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "edit", size = 20.dp, color = CalSnapColors.Ink, strokeWidth = 2.2f)
                }
                val label = if (isConfirming) "Saving…"
                            else "Log to ${mealType.lowercase().replaceFirstChar { it.uppercase() }}"
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp)
                        .shadow(
                            elevation = 8.dp,
                            shape = RoundedCornerShape(28.dp),
                            ambientColor = CalSnapColors.Red.copy(alpha = 0.4f),
                            spotColor = CalSnapColors.Red.copy(alpha = 0.4f),
                        )
                        .clip(RoundedCornerShape(28.dp))
                        .background(if (enabled) CalSnapColors.Red else CalSnapColors.Red.copy(alpha = 0.5f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = enabled,
                            onClick = onLog,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (isConfirming) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = Color.White,
                            )
                        } else {
                            CalSnapIcon(name = "check", size = 18.dp, color = Color.White, strokeWidth = 2.5f)
                        }
                        Text(
                            text = label,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.W700,
                        )
                    }
                }
            }
        }
    }
}

// ─── Refine sheet ───────────────────────────────────────────────────────────

@Composable
private fun RefinePortionSheet(
    food: AiDetectedFood,
    onPortionChange: (Double) -> Unit,
    onDone: () -> Unit,
) {
    var portion by remember(food.portionG) { mutableStateOf(food.portionG) }
    val kcal = food.caloriesPer100g * portion / 100.0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 22.dp)
            .padding(bottom = 32.dp),
    ) {
        Text(
            text = food.name,
            fontSize = 20.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.4).sp,
        )
        Text(
            text = "Adjust portion",
            fontSize = 13.sp,
            color = CalSnapColors.Muted,
            modifier = Modifier.padding(top = 4.dp),
        )

        Spacer(Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(label = "−") {
                portion = (portion - 10.0).coerceAtLeast(10.0)
                onPortionChange(portion)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${portion.roundToInt()} g",
                    fontSize = 36.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-1).sp,
                )
                Text(
                    text = "${kcal.roundToInt()} kcal",
                    fontSize = 14.sp,
                    color = CalSnapColors.Muted,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            StepperButton(label = "+") {
                portion += 10.0
                onPortionChange(portion)
            }
        }

        Spacer(Modifier.height(24.dp))
        CalSnapBrandButton(text = "Done", onClick = onDone)
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(CircleShape)
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
            fontSize = 24.sp,
            fontWeight = FontWeight.W600,
            color = CalSnapColors.Ink,
        )
    }
}

@Composable
private fun EmptyScanContent(error: String?, onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text(if (error != null) "⚠️" else "📷", fontSize = 48.sp)
            Text(
                text = if (error != null) "Scan failed" else "No scan result",
                fontSize = 20.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
            )
            if (error != null) {
                Text(
                    text = error,
                    fontSize = 14.sp,
                    color = CalSnapColors.Muted,
                )
            }
            CalSnapTextButton(text = "Go back", onClick = onBack)
        }
    }
}
