package com.company.app.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.DailyRangeSummary
import com.company.app.ui.components.CalSnapBrandButton
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.theme.*

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onUpgrade: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Surface),
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CalSnapColors.Red,
                )
            }
            !state.isPremium -> {
                PremiumGate(onUpgrade = onUpgrade)
            }
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = CalSnapSpacing.xxl),
                ) {
                    item { AnalyticsHeader(onBack = onBack, onRefresh = viewModel::refresh) }

                    item {
                        Spacer(Modifier.height(CalSnapSpacing.sm))
                        WeeklyCalorieChart(
                            data = state.weeklyData,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CalSnapSpacing.screenPad),
                        )
                    }

                    item {
                        Spacer(Modifier.height(CalSnapSpacing.md))
                        AverageMacrosCard(
                            calories = state.avgCalories,
                            protein = state.avgProtein,
                            carbs = state.avgCarbs,
                            fat = state.avgFat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = CalSnapSpacing.screenPad),
                        )
                    }

                    if (state.weeklyData.isNotEmpty()) {
                        item {
                            Spacer(Modifier.height(CalSnapSpacing.md))
                            MacroBreakdownBars(
                                data = state.weeklyData,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = CalSnapSpacing.screenPad),
                            )
                        }
                    }

                    state.error?.let {
                        item {
                            Spacer(Modifier.height(CalSnapSpacing.sm))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = CalSnapSpacing.screenPad)
                                    .clip(RoundedCornerShape(CalSnapRadius.md))
                                    .background(CalSnapColors.RedSoft)
                                    .padding(CalSnapSpacing.md),
                            ) {
                                Text(it, style = CalSnapType.Body, color = CalSnapColors.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnalyticsHeader(onBack: () -> Unit, onRefresh: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
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

        Text(
            text = "Analytics",
            style = CalSnapType.HeadlineMedium,
            color = CalSnapColors.Ink,
            modifier = Modifier.weight(1f),
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(CalSnapColors.SurfaceAlt)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onRefresh,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "arrow-r", size = 18.dp, color = CalSnapColors.Ink)
        }
    }
}

@Composable
private fun WeeklyCalorieChart(
    data: List<DailyRangeSummary>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CalSnapRadius.card))
            .background(CalSnapColors.Background)
            .padding(CalSnapSpacing.cardPad),
    ) {
        Text("CALORIES — LAST 7 DAYS", style = CalSnapType.Label, color = CalSnapColors.Muted)
        Spacer(Modifier.height(CalSnapSpacing.md))

        if (data.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No data logged yet",
                    style = CalSnapType.Body,
                    color = CalSnapColors.Hint,
                )
            }
        } else {
            val maxCal = data.maxOf { it.totalCalories }.coerceAtLeast(500.0)
            val inkColor = CalSnapColors.Ink
            val dividerColor = CalSnapColors.Divider

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp),
            ) {
                val chartH = size.height - 24.dp.toPx()
                val slotW = size.width / data.size
                val barW = slotW * 0.5f
                val gap = (slotW - barW) / 2f

                // 3 horizontal grid lines
                repeat(3) { i ->
                    val y = chartH * (1f - (i + 1) / 3f)
                    drawLine(
                        color = dividerColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f)),
                    )
                }

                data.forEachIndexed { idx, day ->
                    val barH = (day.totalCalories / maxCal * chartH).toFloat().coerceAtLeast(4f)
                    val x = idx * slotW + gap
                    val y = chartH - barH
                    drawRoundRect(
                        color = inkColor,
                        topLeft = Offset(x, y),
                        size = Size(barW, barH),
                        cornerRadius = CornerRadius(4.dp.toPx()),
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround,
            ) {
                data.forEach { day ->
                    Text(
                        text = day.date.takeLast(5),
                        style = CalSnapType.Label,
                        color = CalSnapColors.Muted,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AverageMacrosCard(
    calories: Double,
    protein: Double,
    carbs: Double,
    fat: Double,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CalSnapRadius.card))
            .background(CalSnapColors.Background)
            .padding(CalSnapSpacing.cardPad),
    ) {
        Text("DAILY AVERAGE", style = CalSnapType.Label, color = CalSnapColors.Muted)
        Spacer(Modifier.height(CalSnapSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            AvgCell(value = "${calories.toInt()}", unit = "kcal", label = "Calories", color = CalSnapColors.Ink)
            CellDivider()
            AvgCell(value = "${protein.toInt()}g", unit = "", label = "Protein", color = CalSnapColors.Protein)
            CellDivider()
            AvgCell(value = "${carbs.toInt()}g", unit = "", label = "Carbs", color = CalSnapColors.Carb)
            CellDivider()
            AvgCell(value = "${fat.toInt()}g", unit = "", label = "Fat", color = CalSnapColors.Fat)
        }
    }
}

@Composable
private fun MacroBreakdownBars(
    data: List<DailyRangeSummary>,
    modifier: Modifier = Modifier,
) {
    val avgProtein = if (data.isEmpty()) 0.0 else data.sumOf { it.totalProteinG } / data.size
    val avgCarbs = if (data.isEmpty()) 0.0 else data.sumOf { it.totalCarbsG } / data.size
    val avgFat = if (data.isEmpty()) 0.0 else data.sumOf { it.totalFatG } / data.size
    val total = (avgProtein + avgCarbs + avgFat).coerceAtLeast(1.0)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(CalSnapRadius.card))
            .background(CalSnapColors.Background)
            .padding(CalSnapSpacing.cardPad),
        verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
    ) {
        Text("MACRO SPLIT", style = CalSnapType.Label, color = CalSnapColors.Muted)

        MacroSplitBar("Protein", avgProtein, total, CalSnapColors.Protein)
        MacroSplitBar("Carbs", avgCarbs, total, CalSnapColors.Carb)
        MacroSplitBar("Fat", avgFat, total, CalSnapColors.Fat)
    }
}

@Composable
private fun MacroSplitBar(
    label: String,
    value: Double,
    total: Double,
    color: androidx.compose.ui.graphics.Color,
) {
    val pct = ((value / total) * 100).toInt()
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
    ) {
        Text(
            text = label,
            style = CalSnapType.BodySmall,
            color = CalSnapColors.Muted,
            modifier = Modifier.width(52.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(CalSnapRadius.pill))
                .background(CalSnapColors.Divider),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth((value / total).toFloat().coerceIn(0f, 1f))
                    .clip(RoundedCornerShape(CalSnapRadius.pill))
                    .background(color),
            )
        }
        Text(
            text = "$pct%",
            style = CalSnapType.BodySmall,
            color = CalSnapColors.Muted,
            modifier = Modifier.width(32.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun AvgCell(
    value: String,
    unit: String,
    label: String,
    color: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = CalSnapType.HeadlineMedium,
            color = color,
        )
        if (unit.isNotEmpty()) {
            Text(unit, style = CalSnapType.Label, color = CalSnapColors.Muted)
        }
        Text(label, style = CalSnapType.BodySmall, color = CalSnapColors.Muted)
    }
}

@Composable
private fun CellDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(CalSnapColors.Divider),
    )
}

@Composable
private fun PremiumGate(onUpgrade: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(CalSnapSpacing.xl),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CalSnapColors.CarbBg),
                contentAlignment = Alignment.Center,
            ) {
                Text("★", fontSize = 32.sp)
            }

            Text(
                text = "Analytics is Premium",
                style = CalSnapType.HeadlineLarge,
                color = CalSnapColors.Ink,
                textAlign = TextAlign.Center,
            )

            Text(
                text = "Unlock weekly calorie charts, macro splits,\nand trend tracking.",
                style = CalSnapType.Body,
                color = CalSnapColors.Muted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(CalSnapSpacing.sm))

            CalSnapBrandButton(
                text = "Upgrade to Premium",
                onClick = onUpgrade,
            )
        }
    }
}
