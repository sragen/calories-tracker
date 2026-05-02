package com.company.app.ui.analytics

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.DailyRangeSummary
import com.company.app.ui.components.CalSnapBottomTabBar
import com.company.app.ui.components.CalSnapBrandButton
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.components.CalSnapTab
import com.company.app.ui.theme.*
import kotlin.math.roundToInt

@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onTabSelected: (CalSnapTab) -> Unit = {},
    onSnapTap: () -> Unit = {},
    onUpgrade: () -> Unit,
    onBack: () -> Unit,
) {
    val state = viewModel.state

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
            !state.isPremium -> {
                Column {
                    StatsHeader()
                    PremiumGate(onUpgrade = onUpgrade)
                }
            }
            else -> {
                LazyColumn(contentPadding = PaddingValues(bottom = 100.dp)) {
                    item { StatsHeader() }
                    item {
                        Spacer(Modifier.height(4.dp))
                        DailyAverageCard(
                            data = state.weeklyData,
                            avgCalories = state.avgCalories,
                            goalCalories = state.targetCalories,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                        )
                    }
                    item {
                        Spacer(Modifier.height(14.dp))
                        MacroSplitCard(
                            avgProtein = state.avgProtein,
                            avgCarbs = state.avgCarbs,
                            avgFat = state.avgFat,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 20.dp),
                        )
                    }
                    state.error?.let {
                        item {
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CalSnapColors.RedSoft)
                                    .padding(12.dp),
                            ) {
                                Text(it, fontSize = 13.sp, color = CalSnapColors.Red)
                            }
                        }
                    }
                }
            }
        }

        CalSnapBottomTabBar(
            selectedTab = CalSnapTab.STATS,
            onTabSelected = { tab ->
                if (tab == CalSnapTab.STATS) viewModel.refresh() else onTabSelected(tab)
            },
            onSnapTap = onSnapTap,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

@Composable
private fun StatsHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 60.dp, bottom = 12.dp),
    ) {
        Text(
            text = "Stats",
            fontSize = 26.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.6).sp,
        )
    }
}

// ─── Daily Average card ─────────────────────────────────────────────────────

@Composable
private fun DailyAverageCard(
    data: List<DailyRangeSummary>,
    avgCalories: Double,
    goalCalories: Double,
    modifier: Modifier = Modifier,
) {
    val pctOfGoal = if (goalCalories > 0) (avgCalories / goalCalories * 100).roundToInt() else 0

    Column(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x0A140F08),
                spotColor = Color(0x10140F08),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(CalSnapColors.Background)
            .padding(18.dp),
    ) {
        Text(
            text = "DAILY AVERAGE",
            fontSize = 12.sp,
            color = CalSnapColors.Muted,
            fontWeight = FontWeight.W600,
            letterSpacing = 0.6.sp,
        )
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 4.dp),
        ) {
            Text(
                text = formatThousands(avgCalories.roundToInt()),
                fontSize = 40.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-1.5).sp,
                lineHeight = 40.sp,
            )
            Text(
                text = "kcal · $pctOfGoal% of goal",
                fontSize = 13.sp,
                color = CalSnapColors.Muted,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }

        Spacer(Modifier.height(16.dp))
        WeeklyBarChart(
            data = data,
            goalKcal = goalCalories,
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
        )
    }
}

@Composable
private fun WeeklyBarChart(
    data: List<DailyRangeSummary>,
    goalKcal: Double,
    modifier: Modifier = Modifier,
) {
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    val padded = (0 until 7).map { i -> data.getOrNull(i)?.totalCalories ?: 0.0 }
    val maxVal = (padded + goalKcal).max() * 1.1
    val todayIdx = padded.indexOfLast { it > 0 }.coerceAtLeast(0)

    val anim by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(800, easing = FastOutSlowInEasing),
        label = "barFill",
    )

    val ink = CalSnapColors.Ink
    val red = CalSnapColors.Red
    val muted = CalSnapColors.Muted

    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val barCount = padded.size
                val slot = w / barCount
                val barW = slot * 0.55f
                val gap = (slot - barW) / 2f

                // Goal dashed line
                if (goalKcal > 0) {
                    val y = h * (1f - (goalKcal / maxVal).toFloat()).coerceIn(0f, 1f)
                    drawLine(
                        color = red,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                    )
                }

                // Bars
                padded.forEachIndexed { i, v ->
                    val ratio = if (maxVal > 0) (v / maxVal).toFloat() else 0f
                    val animatedRatio = ratio * anim
                    val barH = (h * animatedRatio).coerceAtLeast(if (v > 0) 4f else 0f)
                    val x = i * slot + gap
                    val y = h - barH
                    val isToday = i == todayIdx
                    drawRoundedBar(
                        color = if (isToday) red else ink.copy(alpha = 0.85f),
                        x = x, y = y, w = barW, h = barH,
                        radiusPx = 6.dp.toPx(),
                    )
                }
            }

            // Goal label top-right
            if (goalKcal > 0) {
                Text(
                    text = "${formatThousands(goalKcal.roundToInt())} goal",
                    fontSize = 10.sp,
                    color = red,
                    fontWeight = FontWeight.W700,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = (-2).dp),
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
        ) {
            days.forEachIndexed { i, d ->
                Text(
                    text = d,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W700,
                    color = if (i == todayIdx) red else muted,
                )
            }
        }
    }
}

private fun DrawScope.drawRoundedBar(
    color: Color,
    x: Float, y: Float, w: Float, h: Float,
    radiusPx: Float,
) {
    if (h <= 0f) return
    val effectiveRadius = radiusPx.coerceAtMost(h)
    drawRoundRect(
        color = color,
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(effectiveRadius, effectiveRadius),
    )
}

// ─── Macro split card ───────────────────────────────────────────────────────

@Composable
private fun MacroSplitCard(
    avgProtein: Double,
    avgCarbs: Double,
    avgFat: Double,
    modifier: Modifier = Modifier,
) {
    val proteinKcal = avgProtein * 4
    val carbsKcal = avgCarbs * 4
    val fatKcal = avgFat * 9
    val total = (proteinKcal + carbsKcal + fatKcal).coerceAtLeast(1.0)
    val pP = (proteinKcal / total).toFloat()
    val pC = (carbsKcal / total).toFloat()
    val pF = (fatKcal / total).toFloat()

    Column(
        modifier = modifier
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = Color(0x0A140F08),
                spotColor = Color(0x10140F08),
            )
            .clip(RoundedCornerShape(22.dp))
            .background(CalSnapColors.Background)
            .padding(18.dp),
    ) {
        Text(
            text = "Macro split — this week",
            fontSize = 14.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.2).sp,
        )

        Spacer(Modifier.height(12.dp))

        val animP by animateFloatAsState(pP, tween(700, easing = FastOutSlowInEasing), label = "p")
        val animC by animateFloatAsState(pC, tween(700, easing = FastOutSlowInEasing), label = "c")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(CalSnapColors.Divider),
        ) {
            Box(modifier = Modifier.fillMaxHeight().weight(animP.coerceAtLeast(0.001f)).background(CalSnapColors.Protein))
            Box(modifier = Modifier.fillMaxHeight().weight(animC.coerceAtLeast(0.001f)).background(CalSnapColors.Carb))
            Box(modifier = Modifier.fillMaxHeight().weight((1f - animP - animC).coerceAtLeast(0.001f)).background(CalSnapColors.Fat))
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            MacroColumn(
                label = "Protein",
                pct = (pP * 100).roundToInt(),
                grams = (avgProtein * 7).roundToInt(),
                color = CalSnapColors.Protein,
            )
            MacroColumn(
                label = "Carbs",
                pct = (pC * 100).roundToInt(),
                grams = (avgCarbs * 7).roundToInt(),
                color = CalSnapColors.Carb,
            )
            MacroColumn(
                label = "Fat",
                pct = (pF * 100).roundToInt(),
                grams = (avgFat * 7).roundToInt(),
                color = CalSnapColors.Fat,
            )
        }
    }
}

@Composable
private fun MacroColumn(label: String, pct: Int, grams: Int, color: Color) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(color),
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = CalSnapColors.Muted,
                fontWeight = FontWeight.W600,
            )
        }
        Text(
            text = "$pct%",
            fontSize = 18.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = "$grams g",
            fontSize = 11.sp,
            color = CalSnapColors.Muted,
            modifier = Modifier.padding(top = 1.dp),
        )
    }
}

// ─── Premium gate ───────────────────────────────────────────────────────────

@Composable
private fun PremiumGate(onUpgrade: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(CalSnapColors.CarbBg),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "sparkle", size = 32.dp, color = CalSnapColors.Carb, strokeWidth = 2.2f)
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "Stats are a CalSnap Pro feature",
            fontSize = 22.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.4).sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Unlock weekly trends, macro splits, and deficit tracking with CalSnap Pro.",
            fontSize = 14.sp,
            color = CalSnapColors.Muted,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        CalSnapBrandButton(text = "Try CalSnap Pro", onClick = onUpgrade)
    }
}

private fun formatThousands(n: Int): String =
    if (n >= 1000) "${n / 1000},${(n % 1000).toString().padStart(3, '0')}" else n.toString()

private fun List<Double>.max(): Double = if (isEmpty()) 0.0 else maxOrNull() ?: 0.0
