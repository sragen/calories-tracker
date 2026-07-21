package com.company.app.ui.aiscan

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.AiDetectedFood
import com.company.app.shared.data.model.FoodItem
import com.company.app.ui.components.CalSnapBrandButton
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.platform.rememberHapticFeedback
import com.company.app.ui.theme.CalSnapColors
import com.company.app.ui.theme.numeralFont
import kotlin.math.roundToInt

/**
 * Correction sheet with two modes: refine the detected portion, or swap the
 * detected food for a different catalog item. Portion is carried over on swap.
 */
@Composable
fun AiCorrectionSheet(
    food: AiDetectedFood,
    mode: CorrectionMode,
    onModeChange: (CorrectionMode) -> Unit,
    onPortionChange: (Double) -> Unit,
    swapQuery: String,
    swapResults: List<FoodItem>,
    isSearchingSwap: Boolean,
    onSwapQueryChange: (String) -> Unit,
    onSelectSwap: (FoodItem) -> Unit,
    onDone: () -> Unit,
) {
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        Spacer(Modifier.height(14.dp))
        CorrectionModeTabs(mode = mode, onModeChange = onModeChange)
        Spacer(Modifier.height(20.dp))

        when (mode) {
            CorrectionMode.REFINE -> RefinePortionPane(
                food = food,
                onPortionChange = onPortionChange,
                onDone = onDone,
            )
            CorrectionMode.SWAP -> SwapFoodPane(
                query = swapQuery,
                results = swapResults,
                isSearching = isSearchingSwap,
                onQueryChange = onSwapQueryChange,
                onSelect = onSelectSwap,
            )
        }
    }
}

@Composable
private fun CorrectionModeTabs(mode: CorrectionMode, onModeChange: (CorrectionMode) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CalSnapColors.SurfaceAlt)
            .padding(4.dp),
    ) {
        CorrectionTab("Refine portion", mode == CorrectionMode.REFINE, Modifier.weight(1f)) {
            onModeChange(CorrectionMode.REFINE)
        }
        CorrectionTab("Swap food", mode == CorrectionMode.SWAP, Modifier.weight(1f)) {
            onModeChange(CorrectionMode.SWAP)
        }
    }
}

@Composable
private fun CorrectionTab(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (isSelected) CalSnapColors.Card else androidx.compose.ui.graphics.Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            color = if (isSelected) CalSnapColors.Ink else CalSnapColors.Muted,
            fontSize = 13.sp,
            fontWeight = FontWeight.W600,
        )
    }
}

// ─── Refine portion ───────────────────────────────────────────────────────────

@Composable
private fun RefinePortionPane(
    food: AiDetectedFood,
    onPortionChange: (Double) -> Unit,
    onDone: () -> Unit,
) {
    val haptic = rememberHapticFeedback()
    var portion by remember(food.name, food.matchedFoodId) { mutableStateOf(food.portionG) }
    val kcal = food.caloriesPer100g * portion / 100.0

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Adjust portion",
            fontSize = 13.sp,
            color = CalSnapColors.Muted,
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(label = "−") {
                portion = (portion - 10.0).coerceAtLeast(10.0)
                haptic.selection()
                onPortionChange(portion)
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${portion.roundToInt()} g",
                    fontSize = 36.sp,
                    fontFamily = numeralFont,
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
                haptic.selection()
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

// ─── Swap food ────────────────────────────────────────────────────────────────

@Composable
private fun SwapFoodPane(
    query: String,
    results: List<FoodItem>,
    isSearching: Boolean,
    onQueryChange: (String) -> Unit,
    onSelect: (FoodItem) -> Unit,
) {
    val haptic = rememberHapticFeedback()
    Column(modifier = Modifier.fillMaxWidth()) {
        // Search field
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .clip(RoundedCornerShape(999.dp))
                .background(CalSnapColors.SurfaceAlt)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            CalSnapIcon(name = "search", size = 16.dp, color = CalSnapColors.Muted)
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = CalSnapColors.Ink,
                    fontSize = 15.sp,
                ),
                modifier = Modifier.weight(1f),
                decorationBox = { inner ->
                    if (query.isEmpty()) {
                        Text(
                            text = "Search a different food…",
                            fontSize = 15.sp,
                            color = CalSnapColors.Hint,
                        )
                    }
                    inner()
                },
            )
            if (query.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .clip(CircleShape)
                        .background(CalSnapColors.Hint)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onQueryChange("") },
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "close", size = 10.dp, color = CalSnapColors.Background)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Box(modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp, max = 340.dp)) {
            when {
                isSearching -> Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                        color = CalSnapColors.Accent,
                    )
                }
                query.isNotBlank() && results.isEmpty() -> Text(
                    text = "No matching foods",
                    fontSize = 14.sp,
                    color = CalSnapColors.Muted,
                    modifier = Modifier.padding(top = 32.dp),
                )
                query.isBlank() -> Text(
                    text = "Type to find a replacement",
                    fontSize = 14.sp,
                    color = CalSnapColors.Muted,
                    modifier = Modifier.padding(top = 32.dp),
                )
                else -> LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(results, key = { it.id }) { item ->
                        SwapResultRow(item = item) {
                            haptic.success()
                            onSelect(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SwapResultRow(item: FoodItem, onClick: () -> Unit) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-0.2).sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${item.caloriesPer100g.roundToInt()} kcal / 100g",
                    fontSize = 12.sp,
                    color = CalSnapColors.Muted,
                    modifier = Modifier.padding(top = 1.dp),
                )
            }
            CalSnapIcon(name = "chev-r", size = 14.dp, color = CalSnapColors.Mute2)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CalSnapColors.Divider),
        )
    }
}
