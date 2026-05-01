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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
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
    var initialScrollDone by remember { mutableStateOf(false) }

    // Scroll to current value on first composition.
    // paddingH = halfWidth - tickWidth/2 so scrollToItem(idx) centers item[idx] exactly.
    LaunchedEffect(Unit) {
        val idx = (value - min).toInt().coerceIn(0, count - 1)
        listState.scrollToItem(idx)
        initialScrollDone = true
    }

    // Update value + snap when scroll stops.
    // Guard against premature firing on initial composition (isScrollInProgress starts false
    // before LaunchedEffect(Unit) has scrolled to the correct position).
    // Threshold > 2px breaks the infinite loop: after animateScrollToItem the item lands
    // within 2px of center, so the next isScrollInProgress=false does not re-snap.
    LaunchedEffect(listState.isScrollInProgress) {
        if (!initialScrollDone) return@LaunchedEffect
        if (!listState.isScrollInProgress) {
            val info = listState.layoutInfo
            if (info.viewportSize.width == 0 || info.visibleItemsInfo.isEmpty()) return@LaunchedEffect
            val vpCenter = info.viewportSize.width / 2
            val closest = info.visibleItemsInfo.minByOrNull { item ->
                kotlin.math.abs(item.offset + item.size / 2 - vpCenter)
            } ?: return@LaunchedEffect
            val itemCenter = closest.offset + closest.size / 2
            if (kotlin.math.abs(itemCenter - vpCenter) > 2) {
                listState.animateScrollToItem(closest.index)
            }
            onValueChange((min + closest.index).coerceIn(min, max))
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
        // Subtract half-tick so item CENTERS land on viewport center, not leading edges
        val paddingH = (halfWidth - tickWidthDp / 2).coerceAtLeast(0.dp)

        LazyRow(
            state = listState,
            contentPadding = PaddingValues(horizontal = paddingH),
            flingBehavior = rememberSnapFlingBehavior(listState),
            modifier = Modifier.height(72.dp),
        ) {
            items(count) { idx ->
                val tickVal = (min + idx).toInt()
                val isMajor = tickVal % 10 == 0
                val isMedium = tickVal % 5 == 0 && !isMajor
                // Box allows the label to overflow the 14dp column width without wrapping
                Box(
                    contentAlignment = Alignment.TopCenter,
                    modifier = Modifier.width(tickWidthDp).height(72.dp),
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.wrapContentWidth(unbounded = true),
                    ) {
                        Spacer(Modifier.height(8.dp))
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
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = tickVal.toString(),
                                style = CalSnapType.BodySmall,
                                color = CalSnapColors.Muted,
                                softWrap = false,
                                overflow = TextOverflow.Visible,
                            )
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 4.dp)
                .width(2.dp)
                .height(40.dp)
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
