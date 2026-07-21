package com.company.app.ui.dailygoal

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import com.company.app.shared.data.model.GoalPreset
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.theme.*
import kotlinx.coroutines.launch

// ── Design tokens from screens-phase3-classic.jsx ────────────────────────────

private val ProteinColor = CalSnapColors.Protein
private val CarbColor    = CalSnapColors.Carb
private val FatColor     = CalSnapColors.Fat

private val CardShadowAmbient = CalSnapColors.ShadowAmbient
private val CardShadowSpot    = CalSnapColors.ShadowSpot

private data class PresetMeta(
    val type: String,
    val icon: String,
    val name: String,
    val sub: String,
    val tag: String,
    val accent: Color,
)

private val PRESET_META = listOf(
    PresetMeta("CUT",      "🔥", "Cut",      "Deficit ~20%", "Weight loss", CalSnapColors.Accent),
    PresetMeta("MAINTAIN", "⚖️", "Maintain", "TDEE",         "Hold weight", CalSnapColors.Good),
    PresetMeta("BULK",     "💪", "Bulk",     "Surplus ~15%", "Muscle gain", CalSnapColors.Fat),
)

// ── Screen entry ──────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDailyTargetScreen(
    viewModel: EditDailyTargetViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
) {
    val state by viewModel.state.collectAsState()
    val haptic = com.company.app.ui.platform.rememberHapticFeedback()
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showSheet = state.editingField != null

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Surface),
    ) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = CalSnapColors.Accent,
            )
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> MainContent(state, viewModel, onBack, onSaved, haptic)
        }

        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeKeypad,
                sheetState = sheetState,
                containerColor = CalSnapColors.Card,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                dragHandle = {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Box(
                            modifier = Modifier
                                .size(width = 40.dp, height = 5.dp)
                                .clip(CircleShape)
                                .background(CalSnapColors.Divider),
                        )
                    }
                },
            ) {
                KeypadSheet(
                    field = state.editingField!!,
                    buffer = state.keypadBuffer,
                    onKey = viewModel::onKeypadInput,
                    onQuickAdjust = viewModel::quickAdjust,
                    onDone = {
                        haptic.light()
                        // Animate the sheet out, then commit the value. Swallow any
                        // hide() failure (e.g. mid-animation state tear-down) so the
                        // value still commits and we never bubble to the root scope's
                        // exception handler.
                        scope.launch {
                            try { sheetState.hide() } catch (_: Throwable) { /* ignored */ }
                            viewModel.confirmKeypad()
                        }
                    },
                )
            }
        }
    }
}

// ── Main scrollable content ───────────────────────────────────────────────────

@Composable
private fun MainContent(
    state: EditDailyTargetState,
    viewModel: EditDailyTargetViewModel,
    onBack: () -> Unit,
    onSaved: () -> Unit,
    haptic: com.company.app.ui.platform.HapticFeedback,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        GoalsHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 30.dp),
        ) {
            Spacer(Modifier.height(4.dp))
            HeroDonutCard(state)

            // Presets section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 22.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "CHOOSE A PRESET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Muted,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "Reset",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W600,
                    color = CalSnapColors.Accent,
                    modifier = Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { viewModel.reset(); haptic.light() },
                    ),
                )
            }
            PresetCards(state, onSelect = { preset ->
                viewModel.selectPreset(preset); haptic.light()
            })

            // Fine tune section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "FINE TUNE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Muted,
                    letterSpacing = 1.sp,
                )
                Text(
                    text = "Tap a number to edit",
                    fontSize = 11.sp,
                    color = CalSnapColors.Mute2,
                )
            }
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                MacroInput(
                    label = "Calories",
                    value = state.calories.toInt(),
                    suffix = "kcal",
                    accent = CalSnapColors.Ink,
                    max = 3500,
                    isFocused = state.editingField == EditingField.CALORIES,
                    onClick = { viewModel.openKeypad(EditingField.CALORIES); haptic.light() },
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    MacroInput(
                        label = "Protein",
                        value = state.proteinG.toInt(),
                        suffix = "g",
                        accent = ProteinColor,
                        max = 250,
                        isFocused = state.editingField == EditingField.PROTEIN,
                        onClick = { viewModel.openKeypad(EditingField.PROTEIN); haptic.light() },
                        modifier = Modifier.weight(1f),
                    )
                    MacroInput(
                        label = "Fat",
                        value = state.fatG.toInt(),
                        suffix = "g",
                        accent = FatColor,
                        max = 120,
                        isFocused = state.editingField == EditingField.FAT,
                        onClick = { viewModel.openKeypad(EditingField.FAT); haptic.light() },
                        modifier = Modifier.weight(1f),
                    )
                }
                MacroInput(
                    label = "Carbohydrates",
                    value = state.carbsG.toInt(),
                    suffix = "g",
                    accent = CarbColor,
                    max = 400,
                    isFocused = state.editingField == EditingField.CARBS,
                    onClick = { viewModel.openKeypad(EditingField.CARBS); haptic.light() },
                )
            }

            // Save CTA
            Box(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 16.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        ambientColor = CalSnapColors.Accent.copy(alpha = 0.35f),
                        spotColor    = CalSnapColors.Accent.copy(alpha = 0.35f),
                    )
                    .clip(RoundedCornerShape(28.dp))
                    .background(CalSnapColors.Accent)
                    .clickable(
                        enabled = !state.isSaving,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { haptic.medium(); viewModel.save(onSuccess = onSaved) },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = CalSnapColors.OnAccent,
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Text(
                        text = "Save goals",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = CalSnapColors.OnAccent,
                    )
                }
            }
        }
    }
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
private fun GoalsHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "chev-l", size = 24.dp, color = CalSnapColors.Ink, strokeWidth = 2f)
        }

        Text(
            text = "Daily goals",
            fontSize = 22.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.4).sp,
            modifier = Modifier.weight(1f),
        )

        // Auto · TDEE pill (solid white with border)
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(CalSnapColors.Card)
                .border(width = 1.dp, color = CalSnapColors.Border, shape = CircleShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
        ) {
            Text(
                text = "Auto · TDEE",
                fontSize = 12.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Muted,
            )
        }
    }
}

// ── Hero donut card (solid white) ─────────────────────────────────────────────

@Composable
private fun HeroDonutCard(state: EditDailyTargetState) {
    val total2 = state.proteinG * 4 + state.carbsG * 4 + state.fatG * 9
    val pf = if (total2 > 0) (state.proteinG * 4 / total2).toFloat() else 0.33f
    val cf = if (total2 > 0) (state.carbsG * 4 / total2).toFloat() else 0.44f
    val ff = if (total2 > 0) (state.fatG * 9 / total2).toFloat() else 0.23f
    val macroKcal = (state.proteinG * 4 + state.carbsG * 4 + state.fatG * 9).toInt()
    val isOver = macroKcal > state.calories.toInt() + 10
    val selectedMeta = PRESET_META.find { it.type == state.selectedPresetType }

    Column(
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CalSnapColors.Card)
            .border(width = 1.dp, color = CalSnapColors.Border, shape = RoundedCornerShape(24.dp))
            .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 22.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ClassicDonut(
                size = 140.dp,
                stroke = 18.dp,
                proteinFrac = pf,
                carbFrac = cf,
                fatFrac = ff,
                isOver = isOver,
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = (selectedMeta?.tag ?: "Custom").uppercase(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Muted,
                    letterSpacing = 0.7.sp,
                )
                Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 2.dp)) {
                    Text(
                        text = state.calories.toInt().toString(),
                        fontSize = 30.sp,
                        fontFamily = numeralFont,
                        fontWeight = FontWeight.W700,
                        color = CalSnapColors.Ink,
                        letterSpacing = (-0.9).sp,
                        lineHeight = 33.sp,
                    )
                    Text(
                        text = " kcal",
                        fontSize = 14.sp,
                        color = CalSnapColors.Muted,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.padding(bottom = 3.dp),
                    )
                }
                Spacer(Modifier.height(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    MacroLegendRow("Protein", state.proteinG.toInt(), ProteinColor, (pf * 100).toInt())
                    MacroLegendRow("Carbs",   state.carbsG.toInt(),   CarbColor,    (cf * 100).toInt())
                    MacroLegendRow("Fat",     state.fatG.toInt(),     FatColor,     (ff * 100).toInt())
                }
            }
        }

        if (isOver) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(CalSnapColors.CarbBg)
                    .border(
                        width = 1.dp,
                        color = CalSnapColors.Warn.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp),
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(CalSnapColors.Warn, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("!", fontSize = 13.sp, fontWeight = FontWeight.W700, color = CalSnapColors.OnAccent)
                }
                Text(
                    text = "Macros add up to $macroKcal kcal, exceeding your ${state.calories.toInt()} target.",
                    fontSize = 12.sp,
                    color = CalSnapColors.Ink,
                    lineHeight = 17.sp,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun MacroLegendRow(label: String, value: Int, color: Color, pct: Int) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color),
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = CalSnapColors.Muted,
            fontWeight = FontWeight.W600,
            modifier = Modifier.width(50.dp),
        )
        Text(
            text = "${value}g",
            fontSize = 12.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = "$pct%",
            fontSize = 11.sp,
            color = CalSnapColors.Mute2,
            fontWeight = FontWeight.W600,
        )
    }
}

// ── Classic donut: flat 3-arc canvas ──────────────────────────────────────────

@Composable
private fun ClassicDonut(
    size: Dp,
    stroke: Dp,
    proteinFrac: Float,
    carbFrac: Float,
    fatFrac: Float,
    isOver: Boolean,
) {
    val animP by animateFloatAsState(proteinFrac, animationSpec = tween(600, easing = FastOutSlowInEasing))
    val animC by animateFloatAsState(carbFrac,    animationSpec = tween(600, easing = FastOutSlowInEasing))
    val animF by animateFloatAsState(fatFrac,     animationSpec = tween(600, easing = FastOutSlowInEasing))

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = stroke.toPx()
            val r = (size.toPx() - strokePx) / 2f
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(r * 2f, r * 2f)
            val gapDeg = 2f

            drawArc(
                color = CalSnapColors.Divider,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Butt),
            )

            var acc = -90f
            fun arc(frac: Float, color: Color) {
                val sweep = (frac * 360f - gapDeg).coerceAtLeast(0f)
                if (sweep > 0f) {
                    drawArc(
                        color = color,
                        startAngle = acc,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokePx, cap = StrokeCap.Butt),
                    )
                }
                acc += frac * 360f
            }
            arc(animP, ProteinColor)
            arc(animC, CarbColor)
            arc(animF, FatColor)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (isOver) "OVER" else "MACROS",
                fontSize = 11.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Muted,
                letterSpacing = 0.6.sp,
            )
            Text(
                text = "${((proteinFrac + carbFrac + fatFrac) * 100).toInt()}%",
                fontSize = 22.sp,
                fontWeight = FontWeight.W700,
                color = if (isOver) CalSnapColors.Warn else CalSnapColors.Ink,
                letterSpacing = (-0.5).sp,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
    }
}

// ── Preset cards (horizontal scroll) ──────────────────────────────────────────

@Composable
private fun PresetCards(state: EditDailyTargetState, onSelect: (GoalPreset) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(state.presets) { preset ->
            val meta = PRESET_META.find { it.type == preset.type }
                ?: PresetMeta(preset.type, "⚙️", preset.label, "", "", CalSnapColors.Ink)
            val isSelected = state.selectedPresetType == preset.type
            PresetCard(meta = meta, preset = preset, isSelected = isSelected, onClick = { onSelect(preset) })
        }
    }
}

@Composable
private fun PresetCard(
    meta: PresetMeta,
    preset: GoalPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) meta.accent else CalSnapColors.Border
    val borderWidth = if (isSelected) 2.dp else 1.dp
    val shadowColor = if (isSelected) meta.accent.copy(alpha = 0.13f) else CardShadowSpot
    val shadowElevation = if (isSelected) 12.dp else 6.dp

    Box(
        modifier = Modifier
            .width(140.dp)
            .shadow(
                elevation = shadowElevation,
                shape = RoundedCornerShape(20.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor,
            )
            .clip(RoundedCornerShape(20.dp))
            .background(CalSnapColors.Card)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 16.dp),
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(22.dp)
                    .background(meta.accent, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = "check", size = 12.dp, color = CalSnapColors.OnAccent, strokeWidth = 3f)
            }
        }

        Column {
            // Icon tile (accent at 8% alpha background)
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(meta.accent.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = meta.icon, fontSize = 24.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = meta.name,
                fontSize = 17.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.3).sp,
            )
            Text(
                text = meta.sub,
                fontSize = 11.sp,
                color = CalSnapColors.Muted,
                modifier = Modifier.padding(top = 2.dp),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = preset.targetCalories.toInt().toString(),
                fontSize = 22.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.6).sp,
            )
            Text(
                text = "KCAL/DAY",
                fontSize = 10.sp,
                color = CalSnapColors.Mute2,
                fontWeight = FontWeight.W600,
                letterSpacing = 0.5.sp,
            )
        }
    }
}

// ── Macro input row (solid white + progress bar) ──────────────────────────────

@Composable
private fun MacroInput(
    label: String,
    value: Int,
    suffix: String,
    accent: Color,
    max: Int,
    isFocused: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pct = (value.toFloat() / max).coerceIn(0f, 1f)
    val borderColor = if (isFocused) accent else CalSnapColors.Border
    val borderWidth = if (isFocused) 2.dp else 1.dp

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CalSnapColors.Card)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label.uppercase(),
                fontSize = 12.sp,
                fontWeight = FontWeight.W600,
                color = CalSnapColors.Muted,
                letterSpacing = 0.7.sp,
            )
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = value.toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-0.6).sp,
                    lineHeight = 22.sp,
                )
                Text(
                    text = suffix,
                    fontSize = 12.sp,
                    color = CalSnapColors.Mute2,
                    modifier = Modifier.padding(bottom = 1.dp),
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(CalSnapColors.SurfaceAlt),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent),
            )
        }
    }
}

// ── Keypad bottom sheet (flat iOS-style) ──────────────────────────────────────

@Composable
private fun KeypadSheet(
    field: EditingField,
    buffer: String,
    onKey: (String) -> Unit,
    onQuickAdjust: (Int) -> Unit,
    onDone: () -> Unit,
) {
    val title = when (field) {
        EditingField.CALORIES -> "Daily calories"
        EditingField.PROTEIN  -> "Protein"
        EditingField.CARBS    -> "Carbohydrates"
        EditingField.FAT      -> "Fat"
    }
    val suffix = if (field == EditingField.CALORIES) "kcal" else "g"
    val isCalories = field == EditingField.CALORIES

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 8.dp),
    ) {
        // Value display card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(CalSnapColors.Card)
                .border(1.dp, CalSnapColors.Border, RoundedCornerShape(24.dp))
                .padding(vertical = 24.dp, horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Muted,
                letterSpacing = 0.8.sp,
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.Center) {
                Text(
                    text = if (buffer.isEmpty()) "0" else buffer,
                    fontSize = 56.sp,
                    fontFamily = numeralFont,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-2).sp,
                    lineHeight = 60.sp,
                )
                Box(
                    modifier = Modifier
                        .padding(start = 2.dp, bottom = 8.dp)
                        .width(2.dp)
                        .height(40.dp)
                        .background(CalSnapColors.Accent),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = suffix,
                    fontSize = 16.sp,
                    color = CalSnapColors.Muted,
                    modifier = Modifier.padding(bottom = 10.dp),
                )
            }
            if (isCalories) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = "TDEE base · tap −/+ to adjust by 50",
                    fontSize = 12.sp,
                    color = CalSnapColors.Muted,
                )
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(-50 to "− 50", 50 to "+ 50", 200 to "+ 200").forEach { (delta, label) ->
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(CalSnapColors.SurfaceAlt)
                                .border(1.dp, CalSnapColors.Border, CircleShape)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { onQuickAdjust(delta) },
                                )
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                        ) {
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.W600, color = CalSnapColors.Ink)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Keypad — flat iOS-style on white background; Done lives inside.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(CalSnapColors.Card)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(end = 8.dp),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(CalSnapColors.Accent)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDone,
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("Done", fontSize = 13.sp, fontWeight = FontWeight.W700, color = CalSnapColors.OnAccent)
                }
            }
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf(".", "0", "⌫"),
            )
            keys.forEach { row ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    row.forEach { key ->
                        KeypadButton(key = key, modifier = Modifier.weight(1f), onClick = { onKey(key) })
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(key: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(CalSnapColors.SurfaceAlt)
            .border(width = 1.dp, color = CalSnapColors.Border, shape = RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = key,
            fontSize = 22.sp,
            fontWeight = FontWeight.W500,
            color = CalSnapColors.Ink,
        )
    }
}

// ── Error state ───────────────────────────────────────────────────────────────

@Composable
private fun BoxScope.ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.align(Alignment.Center),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(message, color = CalSnapColors.Muted, fontSize = 14.sp)
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = CalSnapColors.Accent),
        ) {
            Text("Retry")
        }
    }
}
