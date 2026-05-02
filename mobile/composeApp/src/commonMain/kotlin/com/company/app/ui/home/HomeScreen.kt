package com.company.app.ui.home

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.DailySummary
import com.company.app.shared.data.model.MealLogEntry
import com.company.app.shared.data.model.NutritionSummary
import com.company.app.ui.components.*
import com.company.app.ui.platform.rememberHapticFeedback
import com.company.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

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
                    userName = state.userName,
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
    userName: String?,
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
            HomeTopBar(streak = streak, userName = userName, onProfile = onProfile)
        }
        item {
            CalorieDashboardCard(
                summary = diary.summary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalSnapSpacing.screenPad),
            )
        }
        item {
            Spacer(Modifier.height(16.dp))
            QuickActions(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalSnapSpacing.screenPad),
                onSnap = { onAiScan("LUNCH") },
                onSearch = { onAddFood("LUNCH") },
            )
        }
        item {
            Spacer(Modifier.height(22.dp))
            TodaysMealsSection(
                diary = diary,
                deletingId = deletingId,
                onDelete = onDelete,
                onAddFood = onAddFood,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = CalSnapSpacing.screenPad),
            )
        }
    }
}

// ─── Header ─────────────────────────────────────────────────────────────────

@Composable
private fun HomeTopBar(streak: Int, userName: String?, onProfile: () -> Unit) {
    val now = remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()) }
    val dayLabel = remember(now) { dayOfWeekLabel(now.dayOfWeek) }
    val firstName = remember(userName) { userName?.trim()?.takeIf { it.isNotEmpty() }?.split(' ')?.first() }
    val greeting = remember(now, firstName) { greetingFor(now.hour, firstName) }
    val initial = remember(firstName) { firstName?.firstOrNull()?.uppercaseChar()?.toString() ?: "?" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = CalSnapSpacing.screenPad, end = CalSnapSpacing.screenPad)
            .padding(top = 60.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(CalSnapColors.Carb, CalSnapColors.Red),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onProfile,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = initial,
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.W700,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = dayLabel,
                style = CalSnapType.BodySmall,
                color = CalSnapColors.Muted,
            )
            Text(
                text = greeting,
                fontSize = 18.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.4).sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (streak >= 1) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(CalSnapRadius.pill))
                    .background(CalSnapColors.RedSoft)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                CalSnapIcon(name = "streak", size = 14.dp, color = CalSnapColors.Red, strokeWidth = 2.4f)
                Text(
                    text = "$streak",
                    color = CalSnapColors.Red,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.W700,
                )
            }
        }
    }
}

private fun dayOfWeekLabel(day: DayOfWeek): String = when (day) {
    DayOfWeek.MONDAY -> "Monday"
    DayOfWeek.TUESDAY -> "Tuesday"
    DayOfWeek.WEDNESDAY -> "Wednesday"
    DayOfWeek.THURSDAY -> "Thursday"
    DayOfWeek.FRIDAY -> "Friday"
    DayOfWeek.SATURDAY -> "Saturday"
    DayOfWeek.SUNDAY -> "Sunday"
    else -> "Today"
}

private fun greetingFor(hour: Int, firstName: String?): String {
    val prefix = when (hour) {
        in 5..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        in 17..20 -> "Good evening"
        else -> "Good night"
    }
    val who = firstName ?: "there"
    return "$prefix, $who 👋"
}

// ─── Calorie dashboard card ──────────────────────────────────────────────────

@Composable
private fun CalorieDashboardCard(summary: NutritionSummary, modifier: Modifier = Modifier) {
    val progress = (summary.caloriesPercent / 100f).coerceIn(0f, 1f)
    val isOver = summary.caloriesPercent > 100
    val ringColor = if (isOver) CalSnapColors.Red else CalSnapColors.Ink
    val displayCals = summary.remainingCalories.toInt().coerceAtLeast(0)
    val pct = summary.caloriesPercent.toInt().coerceIn(0, 999)

    val proteinTarget = (summary.targetCalories * 0.25 / 4).toFloat().coerceAtLeast(1f)
    val carbTarget = (summary.targetCalories * 0.50 / 4).toFloat().coerceAtLeast(1f)
    val fatTarget = (summary.targetCalories * 0.25 / 9).toFloat().coerceAtLeast(1f)

    Column(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0x0A140F08),
                spotColor = Color(0x10140F08),
            )
            .clip(RoundedCornerShape(28.dp))
            .background(CalSnapColors.Background)
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Calories left",
                fontSize = 13.sp,
                fontWeight = FontWeight.W500,
                color = CalSnapColors.Muted,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(CalSnapColors.SurfaceAlt)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${summary.targetCalories.toInt()} goal",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W600,
                    color = CalSnapColors.Muted,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$displayCals",
                    style = CalSnapType.Hero,
                    color = if (isOver) CalSnapColors.Red else CalSnapColors.Ink,
                )
                Text(
                    text = "${summary.totalCalories.toInt()} eaten so far",
                    style = CalSnapType.BodySmall,
                    color = CalSnapColors.Muted,
                )
            }

            CalSnapRing(
                progress = progress,
                size = 120.dp,
                strokeWidth = 12.dp,
                color = ringColor,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    CalSnapIcon(name = "flame", size = 28.dp, color = CalSnapColors.Red, strokeWidth = 2f)
                    Text(
                        text = "$pct%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W700,
                        color = CalSnapColors.Ink,
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CalSnapColors.Divider),
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
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

// ─── Quick actions row ───────────────────────────────────────────────────────

@Composable
private fun QuickActions(
    modifier: Modifier = Modifier,
    onSnap: () -> Unit,
    onSearch: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        QuickActionButton(
            icon = "camera",
            label = "Snap",
            bgColor = CalSnapColors.Ink,
            contentColor = Color.White,
            elevated = true,
            modifier = Modifier.weight(1f),
            onClick = onSnap,
        )
        QuickActionButton(
            icon = "barcode",
            label = "Scan",
            bgColor = CalSnapColors.Background,
            contentColor = CalSnapColors.Ink,
            modifier = Modifier.weight(1f),
            onClick = onSearch,
        )
        QuickActionButton(
            icon = "search",
            label = "Search",
            bgColor = CalSnapColors.Background,
            contentColor = CalSnapColors.Ink,
            modifier = Modifier.weight(1f),
            onClick = onSearch,
        )
    }
}

@Composable
private fun QuickActionButton(
    icon: String,
    label: String,
    bgColor: Color,
    contentColor: Color,
    elevated: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .shadow(
                elevation = if (elevated) 6.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (elevated) Color.Black.copy(alpha = 0.15f) else Color(0x0A140F08),
                spotColor = if (elevated) Color.Black.copy(alpha = 0.15f) else Color(0x0A140F08),
            )
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 12.dp, horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
    ) {
        CalSnapIcon(name = icon, size = 18.dp, color = contentColor, strokeWidth = 2f)
        Text(
            text = label,
            fontSize = 14.sp,
            fontWeight = FontWeight.W600,
            color = contentColor,
        )
    }
}

// ─── Today's meals ───────────────────────────────────────────────────────────

@Composable
private fun TodaysMealsSection(
    diary: DailySummary,
    deletingId: Long?,
    onDelete: (MealLogEntry) -> Unit,
    onAddFood: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val allEntries: List<Pair<String, MealLogEntry>> = listOf("BREAKFAST", "LUNCH", "DINNER", "SNACK")
        .flatMap { type -> (diary.meals[type] ?: emptyList()).map { type to it } }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Today's meals",
                fontSize = 17.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.3).sp,
            )
            Text(
                text = "See all",
                fontSize = 13.sp,
                fontWeight = FontWeight.W600,
                color = CalSnapColors.Red,
            )
        }

        Spacer(Modifier.height(6.dp))

        if (allEntries.isEmpty()) {
            EmptyMealSlot(onAdd = { onAddFood("LUNCH") })
        } else {
            allEntries.forEach { (mealType, entry) ->
                MealRowItem(
                    entry = entry,
                    mealType = mealType,
                    isDeleting = deletingId == entry.id,
                    onDelete = { onDelete(entry) },
                )
            }
        }
    }
}

@Composable
private fun MealRowItem(
    entry: MealLogEntry,
    mealType: String,
    isDeleting: Boolean,
    onDelete: () -> Unit,
) {
    val mealLabel = mealType.lowercase().replaceFirstChar { it.uppercase() }
    val haptic = rememberHapticFeedback()
    val density = LocalDensity.current
    val revealWidth = 88.dp
    val revealPx = with(density) { revealWidth.toPx() }
    val offsetX = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(isDeleting) {
        if (isDeleting && offsetX.value != -revealPx) {
            offsetX.animateTo(-revealPx, spring(dampingRatio = 0.85f, stiffness = 600f))
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clipToBounds(),
    ) {
        Row(
            modifier = Modifier.matchParentSize(),
            horizontalArrangement = Arrangement.End,
        ) {
            Box(
                modifier = Modifier
                    .width(revealWidth)
                    .fillMaxHeight()
                    .background(CalSnapColors.Red)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = !isDeleting,
                    ) {
                        haptic.medium()
                        onDelete()
                    },
                contentAlignment = Alignment.Center,
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = Color.White,
                    )
                } else {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        CalSnapIcon(name = "trash", size = 18.dp, color = Color.White, strokeWidth = 2f)
                        Text(
                            text = "Delete",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.W600,
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                .fillMaxWidth()
                .background(CalSnapColors.Background)
                .draggable(
                    enabled = !isDeleting,
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        scope.launch {
                            offsetX.snapTo((offsetX.value + delta).coerceIn(-revealPx, 0f))
                        }
                    },
                    onDragStopped = { velocity ->
                        val target = if (offsetX.value < -revealPx / 2 || velocity < -400f) -revealPx else 0f
                        offsetX.animateTo(
                            targetValue = target,
                            animationSpec = spring(dampingRatio = 0.85f, stiffness = 400f),
                        )
                    },
                )
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            CalSnapFoodPhoto(
                name = entry.foodItem.name,
                size = 48.dp,
                cornerRadius = 12.dp,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.foodItem.name,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "$mealLabel · ${entry.quantityG.toInt()}g",
                    fontSize = 12.sp,
                    color = CalSnapColors.Muted,
                    modifier = Modifier.padding(top = 2.dp),
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.calories.toInt()}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Ink,
                )
                Text(
                    text = "kcal",
                    fontSize = 11.sp,
                    color = CalSnapColors.Muted,
                )
            }
        }
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
            text = "Add your first meal",
            style = CalSnapType.Body,
            color = CalSnapColors.Muted,
        )
    }
}
