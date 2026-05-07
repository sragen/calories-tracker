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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.company.app.shared.data.model.GoalPreset
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.platform.isIosPlatform
import com.company.app.ui.theme.*
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// ── Design tokens from screens-phase3 ────────────────────────────────────────

private val ProteinColor = Color(0xFFE63946)
private val CarbColor    = Color(0xFFF4A23A)
private val FatColor     = Color(0xFF5A8DEF)

private data class PresetMeta(
    val type: String,
    val icon: String,
    val name: String,
    val sub: String,
    val tag: String,
    val accent: Color,
)

private val PRESET_META = listOf(
    PresetMeta("CUT",      "🔥", "Cut",      "Deficit ~20%", "Weight loss",  Color(0xFFE63946)),
    PresetMeta("MAINTAIN", "⚖️", "Maintain", "TDEE",          "Hold weight",  Color(0xFF0E0E0E)),
    PresetMeta("BULK",     "💪", "Bulk",     "Surplus ~15%",  "Muscle gain",  Color(0xFF5A8DEF)),
)

// ── Screen ────────────────────────────────────────────────────────────────────

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

    // Keypad bottom sheet state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val showSheet = state.editingField != null

    LaunchedEffect(showSheet) {
        if (showSheet) scope.launch { sheetState.show() }
        else scope.launch { sheetState.hide() }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Ambient gradient background (LiquidBg)
        AmbientBackground()

        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = CalSnapColors.Red,
            )
            state.error != null -> ErrorState(state.error!!, onRetry = viewModel::load)
            else -> MainContent(state, viewModel, onBack, onSaved, haptic)
        }

        // Keypad bottom sheet
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = viewModel::closeKeypad,
                sheetState = sheetState,
                containerColor = if (isIosPlatform) Color.White.copy(alpha = 0.95f) else Color.White,
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
                        viewModel.confirmKeypad()
                        haptic.light()
                    },
                )
            }
        }
    }
}

// ── Ambient background ────────────────────────────────────────────────────────

@Composable
private fun AmbientBackground() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFF8F0), Color(0xFFF7F0E4), Color(0xFFFBEFE0)),
                )
            )
    ) {
        // Red blob — top left
        Box(
            modifier = Modifier
                .offset(x = (-60).dp, y = (-40).dp)
                .size(260.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFE63946).copy(alpha = 0.18f), Color.Transparent)
                    ),
                    shape = CircleShape,
                )
        )
        // Amber blob — top right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = 30.dp)
                .size(200.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFFF4A23A).copy(alpha = 0.16f), Color.Transparent)
                    ),
                    shape = CircleShape,
                )
        )
        // Blue blob — bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .offset(y = 80.dp)
                .size(300.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF5A8DEF).copy(alpha = 0.14f), Color.Transparent)
                    ),
                    shape = CircleShape,
                )
        )
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
        // Header
        GoalsHeader(onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // Hero donut + macro summary
            HeroDonutCard(state)

            Spacer(Modifier.height(20.dp))

            // Presets section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "CHOOSE A PRESET",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Muted,
                    letterSpacing = 0.8.sp,
                )
                GlassPill(
                    text = "Reset",
                    onClick = { viewModel.reset(); haptic.light() },
                )
            }
            Spacer(Modifier.height(10.dp))
            PresetCards(state, onSelect = { preset ->
                viewModel.selectPreset(preset)
                haptic.light()
            })

            Spacer(Modifier.height(24.dp))

            // Fine-tune section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "FINE TUNE",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Muted,
                    letterSpacing = 0.8.sp,
                )
                Text(
                    text = "Tap a number to edit",
                    fontSize = 11.sp,
                    color = CalSnapColors.Mute2,
                )
            }
            Spacer(Modifier.height(10.dp))
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

            Spacer(Modifier.height(28.dp))

            // Save button
            Box(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFE63946), Color(0xFFC42E3A)),
                        )
                    )
                    .clickable(
                        enabled = !state.isSaving,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = {
                            haptic.medium()
                            viewModel.save(onSuccess = onSaved)
                        },
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (state.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Text(
                        text = "Save goals",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        color = Color.White,
                        letterSpacing = (-0.2).sp,
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
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Glass back button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(if (isIosPlatform) Color.White.copy(alpha = 0.55f) else CalSnapColors.SurfaceAlt)
                .border(
                    width = 1.dp,
                    color = if (isIosPlatform) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                    shape = CircleShape,
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "chev-l", size = 18.dp, color = CalSnapColors.Ink, strokeWidth = 2.2f)
        }

        Text(
            text = "Daily goals",
            fontSize = 17.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.3).sp,
            modifier = Modifier.weight(1f),
        )

        // "Auto · TDEE" pill
        GlassPill(text = "Auto · TDEE")
    }
}

// ── Donut card ────────────────────────────────────────────────────────────────

@Composable
private fun HeroDonutCard(state: EditDailyTargetState) {
    val total2 = state.proteinG * 4 + state.carbsG * 4 + state.fatG * 9
    val pf = if (total2 > 0) (state.proteinG * 4 / total2).toFloat() else 0.33f
    val cf = if (total2 > 0) (state.carbsG * 4 / total2).toFloat() else 0.44f
    val ff = if (total2 > 0) (state.fatG * 9 / total2).toFloat() else 0.23f
    val macroKcal = (state.proteinG * 4 + state.carbsG * 4 + state.fatG * 9).toInt()
    val isOver = macroKcal > state.calories.toInt() + 10

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Donut
            MacroDonut(
                size = 150.dp,
                stroke = 20.dp,
                proteinFrac = pf,
                carbFrac = cf,
                fatFrac = ff,
                isOver = isOver,
                targetKcal = state.calories.toInt(),
            )

            // Right side legend
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                val selectedMeta = PRESET_META.find { it.type == state.selectedPresetType }
                Text(
                    text = selectedMeta?.tag?.uppercase() ?: "CUSTOM",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Muted,
                    letterSpacing = 0.7.sp,
                )
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = state.calories.toInt().toString(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.W700,
                        color = CalSnapColors.Ink,
                        letterSpacing = (-0.8).sp,
                        lineHeight = 28.sp,
                    )
                    Text(
                        text = " kcal",
                        fontSize = 14.sp,
                        color = CalSnapColors.Muted,
                        fontWeight = FontWeight.W500,
                        modifier = Modifier.padding(bottom = 2.dp),
                    )
                }
                Spacer(Modifier.height(4.dp))
                MacroLegendRow(label = "P", value = state.proteinG.toInt(), color = ProteinColor, pct = (pf * 100).toInt())
                MacroLegendRow(label = "C", value = state.carbsG.toInt(), color = CarbColor,    pct = (cf * 100).toInt())
                MacroLegendRow(label = "F", value = state.fatG.toInt(),   color = FatColor,      pct = (ff * 100).toInt())
            }
        }

        // Over-target warning
        if (isOver) {
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFD08A24).copy(alpha = 0.12f))
                    .border(
                        width = 1.dp,
                        color = Color(0xFFD08A24).copy(alpha = 0.3f),
                        shape = RoundedCornerShape(14.dp),
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(Color(0xFFD08A24), CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("!", fontSize = 13.sp, fontWeight = FontWeight.W700, color = Color.White)
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
                .size(14.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color),
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = CalSnapColors.Muted,
            fontWeight = FontWeight.W600,
            modifier = Modifier.width(16.dp),
        )
        Text(
            text = "${value}g",
            fontSize = 12.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            modifier = Modifier.width(38.dp),
        )
        Text(
            text = "$pct%",
            fontSize = 11.sp,
            color = CalSnapColors.Mute2,
            fontWeight = FontWeight.W600,
        )
    }
}

// ── Macro donut (3-arc canvas) ────────────────────────────────────────────────

@Composable
private fun MacroDonut(
    size: Dp,
    stroke: Dp,
    proteinFrac: Float,
    carbFrac: Float,
    fatFrac: Float,
    isOver: Boolean,
    targetKcal: Int,
) {
    // Animate arc fractions
    val animP by animateFloatAsState(proteinFrac, animationSpec = tween(600, easing = FastOutSlowInEasing))
    val animC by animateFloatAsState(carbFrac,    animationSpec = tween(600, easing = FastOutSlowInEasing))
    val animF by animateFloatAsState(fatFrac,     animationSpec = tween(600, easing = FastOutSlowInEasing))

    Box(modifier = Modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = stroke.toPx()
            val r = (size.toPx() - strokePx) / 2f
            val topLeft = Offset(strokePx / 2f, strokePx / 2f)
            val arcSize = Size(r * 2f, r * 2f)

            // Glow track (soft blur effect via alpha layering)
            drawArc(
                color = Color(0xFFF1ECE2),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Butt),
            )

            var acc = -90f
            fun arc(frac: Float, color: Color) {
                val sweep = frac * 360f
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
                acc += sweep
            }

            arc(animP, ProteinColor)
            arc(animC, CarbColor)
            arc(animF, FatColor)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            if (isOver) {
                Text("Over by", fontSize = 9.sp, fontWeight = FontWeight.W700, color = CalSnapColors.Warn, letterSpacing = 0.6.sp)
                Text("+${((proteinFrac + carbFrac + fatFrac) * targetKcal - targetKcal).toInt().coerceAtLeast(0)}",
                    fontSize = 28.sp, fontWeight = FontWeight.W700, color = CalSnapColors.Warn, letterSpacing = (-0.8).sp)
                Text("kcal vs target", fontSize = 9.sp, color = CalSnapColors.Muted)
            } else {
                Text("Total", fontSize = 9.sp, fontWeight = FontWeight.W700, color = CalSnapColors.Muted, letterSpacing = 0.6.sp)
                Text(targetKcal.toString(), fontSize = 28.sp, fontWeight = FontWeight.W700, color = CalSnapColors.Ink, letterSpacing = (-1).sp)
                Text("kcal target", fontSize = 9.sp, color = CalSnapColors.Muted)
            }
        }
    }
}

// ── Preset cards ──────────────────────────────────────────────────────────────

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

            PresetCard(
                meta = meta,
                preset = preset,
                isSelected = isSelected,
                onClick = { onSelect(preset) },
            )
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
    val borderColor = if (isSelected) meta.accent else Color.White.copy(alpha = 0.7f)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Column(
        modifier = Modifier
            .width(140.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(if (isIosPlatform && !isSelected) Color.White.copy(alpha = 0.55f) else Color.White)
            .border(width = borderWidth, color = borderColor, shape = RoundedCornerShape(22.dp))
            .shadow(
                elevation = if (isSelected) 8.dp else 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = if (isSelected) meta.accent.copy(alpha = 0.2f) else Color(0x08140F08),
                spotColor  = if (isSelected) meta.accent.copy(alpha = 0.15f) else Color(0x10140F08),
            )
            .clip(RoundedCornerShape(22.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(14.dp),
    ) {
        // Selected check
        if (isSelected) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.TopEnd) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .background(meta.accent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "check", size = 12.dp, color = Color.White, strokeWidth = 3f)
                }
            }
            Spacer(Modifier.height(6.dp))
        } else {
            Spacer(Modifier.height(28.dp))
        }

        // Icon container
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(meta.accent.copy(alpha = 0.12f))
                .border(
                    width = 1.dp,
                    color = meta.accent.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                ),
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
            text = "kcal/day",
            fontSize = 10.sp,
            color = CalSnapColors.Mute2,
            fontWeight = FontWeight.W600,
            letterSpacing = 0.5.sp,
        )
    }
}

// ── MacroInput with slider ────────────────────────────────────────────────────

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

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(if (isFocused) Color.White else if (isIosPlatform) Color.White.copy(alpha = 0.55f) else Color.White)
            .border(
                width = if (isFocused) 2.dp else 1.dp,
                color = if (isFocused) accent else Color.White.copy(alpha = 0.7f),
                shape = RoundedCornerShape(18.dp),
            )
            .shadow(
                elevation = if (isFocused) 6.dp else 2.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = if (isFocused) accent.copy(alpha = 0.2f) else Color(0x06140F08),
                spotColor  = if (isFocused) accent.copy(alpha = 0.15f) else Color(0x08140F08),
            )
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(accent, CircleShape),
                )
                Text(
                    text = label.uppercase(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Muted,
                    letterSpacing = 0.6.sp,
                )
            }
            Text(
                text = "${(pct * 100).toInt()}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.W600,
                color = CalSnapColors.Mute2,
            )
        }

        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(
                text = value.toString(),
                fontSize = 32.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-1).sp,
                lineHeight = 32.sp,
            )
            Text(
                text = suffix,
                fontSize = 14.sp,
                color = CalSnapColors.Muted,
                fontWeight = FontWeight.W500,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }

        Spacer(Modifier.height(10.dp))

        // Slider track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(Color(0xFF0E0E0E).copy(alpha = 0.06f)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent),
            )
        }

        Spacer(Modifier.height(4.dp))

        // Thumb indicator
        Box(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(pct)
                    .wrapContentWidth(Alignment.End),
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .shadow(
                            elevation = 2.dp,
                            shape = CircleShape,
                            ambientColor = Color(0x18000000),
                        )
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(width = 2.dp, color = accent, shape = CircleShape),
                )
            }
        }
    }
}

// ── Keypad bottom sheet ───────────────────────────────────────────────────────

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
    val suffix = when (field) {
        EditingField.CALORIES -> "kcal"
        else -> "g"
    }
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
                .background(if (isIosPlatform) CalSnapColors.SurfaceAlt else Color(0xFFF7F4EE))
                .padding(vertical = 22.dp, horizontal = 22.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title.uppercase(),
                fontSize = 11.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Muted,
                letterSpacing = 0.7.sp,
            )
            Spacer(Modifier.height(6.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                // Blinking cursor before value
                val infiniteTransition = rememberInfiniteTransition()
                val cursorAlpha by infiniteTransition.animateFloat(
                    initialValue = 1f, targetValue = 0f,
                    animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                )
                Text(
                    text = if (buffer.isEmpty()) "0" else buffer,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.W700,
                    color = CalSnapColors.Ink,
                    letterSpacing = (-2.5).sp,
                    lineHeight = 64.sp,
                )
                Box(
                    modifier = Modifier
                        .padding(start = 2.dp)
                        .width(2.dp)
                        .height(48.dp)
                        .background(CalSnapColors.Red.copy(alpha = cursorAlpha)),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = suffix,
                    fontSize = 16.sp,
                    color = CalSnapColors.Muted,
                    modifier = Modifier.padding(top = 16.dp),
                )
            }
            Spacer(Modifier.height(10.dp))
            // Quick adjust pills
            if (isCalories) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(-50 to "−50", 50 to "+50", 200 to "+200").forEach { (delta, label) ->
                        GlassPill(text = label, onClick = { onQuickAdjust(delta) })
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // Keypad grid
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isIosPlatform) Color.White.copy(alpha = 0.62f) else Color.White)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            // Done button row at top
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CalSnapColors.Ink)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onDone,
                        )
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                ) {
                    Text("Done", fontSize = 14.sp, fontWeight = FontWeight.W700, color = Color.White)
                }
            }
            Spacer(Modifier.height(4.dp))

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
                        KeypadButton(
                            key = key,
                            modifier = Modifier.weight(1f),
                            onClick = { onKey(key) },
                        )
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
            .height(50.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (isIosPlatform) Color.White.copy(alpha = 0.7f) else CalSnapColors.SurfaceAlt)
            .border(
                width = 1.dp,
                color = if (isIosPlatform) Color.White.copy(alpha = 0.9f) else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
            )
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
            textAlign = TextAlign.Center,
        )
    }
}

// ── Glass pill (small chip) ───────────────────────────────────────────────────

@Composable
private fun GlassPill(text: String, onClick: (() -> Unit)? = null) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isIosPlatform) Color.White.copy(alpha = 0.55f) else CalSnapColors.SurfaceAlt)
            .border(
                width = 1.dp,
                color = if (isIosPlatform) Color.White.copy(alpha = 0.8f) else Color.Transparent,
                shape = CircleShape,
            )
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                ) else Modifier
            )
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            fontWeight = FontWeight.W600,
            color = CalSnapColors.Ink,
        )
    }
}

// ── Glass card surface ────────────────────────────────────────────────────────

@Composable
private fun GlassCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = modifier
            .shadow(
                elevation = if (isIosPlatform) 0.dp else 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color(0x0A140F08),
                spotColor = Color(0x10140F08),
            )
            .clip(RoundedCornerShape(24.dp))
            .background(if (isIosPlatform) Color.White.copy(alpha = 0.78f) else Color.White)
            .border(
                width = 1.dp,
                color = if (isIosPlatform) Color.White.copy(alpha = 0.9f) else Color.Transparent,
                shape = RoundedCornerShape(24.dp),
            )
            .padding(20.dp),
        content = content,
    )
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
            colors = ButtonDefaults.buttonColors(containerColor = CalSnapColors.Red),
        ) {
            Text("Retry")
        }
    }
}
