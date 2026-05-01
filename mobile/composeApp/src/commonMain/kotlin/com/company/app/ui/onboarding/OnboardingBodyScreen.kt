package com.company.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.components.CalSnapTextButton
import com.company.app.ui.theme.*

@Composable
fun OnboardingBodyScreen(
    weightKg: Float,
    heightCm: Float,
    age: Int,
    gender: String,
    onWeightChanged: (Float) -> Unit,
    onHeightChanged: (Float) -> Unit,
    onAgeChanged: (Int) -> Unit,
    onGenderChanged: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Surface)
            .padding(horizontal = CalSnapSpacing.screenPad),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(CalSnapSpacing.xxl))

        StepIndicator(current = 2, total = 4, showBack = true, onBack = onBack)

        Spacer(Modifier.height(CalSnapSpacing.lg))

        Text(
            text = "A few quick numbers",
            style = CalSnapType.HeadlineLarge,
            color = CalSnapColors.Ink,
        )

        Spacer(Modifier.height(CalSnapSpacing.sm))

        Text(
            text = "We use these to calculate your BMR & daily targets. Nothing leaves your phone.",
            style = CalSnapType.Body,
            color = CalSnapColors.Muted,
        )

        Spacer(Modifier.height(CalSnapSpacing.xl))

        RulerSection(
            label = "WEIGHT",
            value = weightKg,
            unit = "kg",
            min = 30f,
            max = 200f,
            onValueChange = onWeightChanged,
        )

        Spacer(Modifier.height(CalSnapSpacing.lg))

        RulerSection(
            label = "HEIGHT",
            value = heightCm,
            unit = "cm",
            min = 100f,
            max = 250f,
            onValueChange = onHeightChanged,
        )

        Spacer(Modifier.height(CalSnapSpacing.lg))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AGE",
                    style = CalSnapType.Label,
                    color = CalSnapColors.Muted,
                )
                Spacer(Modifier.height(CalSnapSpacing.sm))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CalSnapRadius.md))
                        .background(CalSnapColors.Surface)
                        .padding(horizontal = CalSnapSpacing.md, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    StepperButton("-") { onAgeChanged((age - 1).coerceAtLeast(12)) }
                    Text(
                        text = "$age",
                        style = CalSnapType.HeadlineMedium,
                        color = CalSnapColors.Ink,
                        textAlign = TextAlign.Center,
                    )
                    StepperButton("+") { onAgeChanged((age + 1).coerceAtMost(90)) }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "GENDER",
                    style = CalSnapType.Label,
                    color = CalSnapColors.Muted,
                )
                Spacer(Modifier.height(CalSnapSpacing.sm))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CalSnapRadius.md))
                        .background(CalSnapColors.Surface)
                        .height(52.dp),
                ) {
                    listOf("MALE" to "M", "FEMALE" to "F").forEach { (key, label) ->
                        GenderChip(
                            label = label,
                            isSelected = gender == key,
                            onClick = { onGenderChanged(key) },
                            modifier = Modifier.weight(1f).fillMaxHeight(),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        CalSnapPrimaryButton(
            text = "Continue",
            onClick = onContinue,
        )

        Spacer(Modifier.height(CalSnapSpacing.xs))

        CalSnapTextButton(
            text = "Back",
            onClick = onBack,
        )

        Spacer(Modifier.height(CalSnapSpacing.lg))
    }
}

@Composable
private fun RulerSection(
    label: String,
    value: Float,
    unit: String,
    min: Float,
    max: Float,
    onValueChange: (Float) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label,
                style = CalSnapType.Label,
                color = CalSnapColors.Muted,
            )
            Row(
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = value.toInt().toString(),
                    style = CalSnapType.HeadlineMedium,
                    color = CalSnapColors.Ink,
                )
                Text(
                    text = unit,
                    style = CalSnapType.Body,
                    color = CalSnapColors.Muted,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
        }
        Spacer(Modifier.height(CalSnapSpacing.sm))
        HorizontalRuler(
            value = value,
            onValueChange = onValueChange,
            min = min,
            max = max,
        )
    }
}

@Composable
private fun HorizontalRuler(
    value: Float,
    onValueChange: (Float) -> Unit,
    min: Float,
    max: Float,
    modifier: Modifier = Modifier,
) {
    val count = (max - min).toInt() + 1
    val tickWidthDp = 14.dp
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    // Scroll to initial value, centered (offset = -halfTick so item center lands on viewport center)
    LaunchedEffect(Unit) {
        val idx = (value - min).toInt().coerceIn(0, count - 1)
        val halfTickPx = with(density) { (tickWidthDp / 2).roundToPx() }
        listState.scrollToItem(idx, scrollOffset = -halfTickPx)
    }

    // Real-time value update while scrolling
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .filter { it.viewportSize.width > 0 && it.visibleItemsInfo.isNotEmpty() }
            .mapNotNull { info ->
                val vpCenter = info.viewportSize.width / 2
                info.visibleItemsInfo.minByOrNull { item ->
                    kotlin.math.abs(item.offset + item.size / 2 - vpCenter)
                }?.index
            }
            .distinctUntilChanged()
            .collect { idx ->
                onValueChange((min + idx).coerceIn(min, max))
            }
    }

    // Snap to nearest tick when a slow drag ends (no fling)
    LaunchedEffect(listState.isScrollInProgress) {
        if (!listState.isScrollInProgress) {
            val info = listState.layoutInfo
            if (info.viewportSize.width == 0 || info.visibleItemsInfo.isEmpty()) return@LaunchedEffect
            val vpCenter = info.viewportSize.width / 2
            val closest = info.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs(item.offset + item.size / 2 - vpCenter)
            } ?: return@LaunchedEffect
            val halfTickPx = with(density) { (tickWidthDp / 2).roundToPx() }
            listState.animateScrollToItem(closest.index, scrollOffset = -halfTickPx)
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CalSnapRadius.md))
            .background(CalSnapColors.Surface),
        contentAlignment = Alignment.Center,
    ) {
        val halfWidth = maxWidth / 2

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = halfWidth),
            flingBehavior = rememberSnapFlingBehavior(listState),
            modifier = Modifier.height(80.dp),
        ) {
            items(count) { idx ->
                val tickVal = (min + idx).toInt()
                val isMajor = tickVal % 10 == 0
                val isMedium = tickVal % 5 == 0 && !isMajor
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .width(tickWidthDp)
                        .height(80.dp),
                    verticalArrangement = Arrangement.Top,
                ) {
                    Spacer(Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .height(
                                when {
                                    isMajor -> 28.dp
                                    isMedium -> 18.dp
                                    else -> 10.dp
                                }
                            )
                            .clip(RoundedCornerShape(1.dp))
                            .background(
                                when {
                                    isMajor -> CalSnapColors.Ink
                                    isMedium -> CalSnapColors.Muted
                                    else -> CalSnapColors.Divider
                                }
                            )
                    )
                    if (isMajor) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = tickVal.toString(),
                            style = CalSnapType.BodySmall,
                            color = CalSnapColors.Muted,
                        )
                    }
                }
            }
        }

        // Center indicator line — top-anchored with same top offset as ticks
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 6.dp)
                .width(2.dp)
                .height(46.dp)
                .background(CalSnapColors.Red),
        )
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(RoundedCornerShape(CalSnapRadius.sm))
            .background(CalSnapColors.SurfaceAlt)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = CalSnapType.HeadlineMedium,
            color = CalSnapColors.Ink,
        )
    }
}

@Composable
private fun GenderChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(CalSnapRadius.md))
            .background(if (isSelected) CalSnapColors.Ink else CalSnapColors.Surface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = CalSnapType.BodyLarge,
            color = if (isSelected) CalSnapColors.Background else CalSnapColors.Muted,
        )
    }
}
