package com.company.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.theme.*

@Composable
internal fun StepIndicator(
    current: Int,
    total: Int,
    showBack: Boolean = true,
    onBack: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
    ) {
        if (showBack && onBack != null) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(CalSnapColors.Ink.copy(alpha = 0.05f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onBack,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = "chev-l", size = 18.dp, color = CalSnapColors.Ink)
            }
        } else if (showBack) {
            Spacer(Modifier.size(36.dp))
        }
        Row(modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(total) { idx ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(CalSnapRadius.pill))
                        .background(
                            if (idx + 1 <= current) CalSnapColors.Ink
                            else CalSnapColors.Ink.copy(alpha = 0.08f)
                        )
                )
            }
        }
        if (showBack) Spacer(Modifier.size(36.dp))
    }
}

private data class GoalOption(
    val key: String,
    val symbol: String,
    val title: String,
    val subtitle: String,
)

private val GOAL_OPTIONS = listOf(
    GoalOption("LOSE",     "↓", "Lose weight",   "Sustainable deficit"),
    GoalOption("MAINTAIN", "=", "Maintain",       "Keep current weight"),
    GoalOption("GAIN",     "↑", "Build muscle",   "Lean surplus + protein"),
)

@Composable
fun OnboardingGoalScreen(
    selectedGoal: String,
    onGoalSelected: (String) -> Unit,
    onContinue: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Surface)
            .padding(horizontal = CalSnapSpacing.screenPad),
    ) {
        Spacer(Modifier.height(CalSnapSpacing.xxl))

        StepIndicator(current = 1, total = 4, showBack = false)

        Spacer(Modifier.height(CalSnapSpacing.lg))

        Text(
            text = "What's your goal?",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp,
            color = CalSnapColors.Ink,
            lineHeight = 33.sp,
        )

        Spacer(Modifier.height(CalSnapSpacing.sm))

        Text(
            text = "We'll calibrate your daily calories and macros around this.",
            style = CalSnapType.Body,
            color = CalSnapColors.Muted,
        )

        Spacer(Modifier.height(CalSnapSpacing.xl))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            GOAL_OPTIONS.forEach { option ->
                GoalCard(
                    option = option,
                    isSelected = selectedGoal == option.key,
                    onClick = { onGoalSelected(option.key) },
                )
            }
        }

        Spacer(Modifier.height(CalSnapSpacing.md))

        // Tip callout — sparkle icon in white circle on redSoft bg
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(CalSnapColors.RedSoft)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = "sparkle", size = 16.dp, color = CalSnapColors.Red)
            }
            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = CalSnapColors.Red, fontWeight = FontWeight.Bold)) {
                        append("Tip")
                    }
                    append(" — most people lose 0.5–1 lb / week comfortably. We'll set a safe deficit.")
                },
                style = CalSnapType.Body,
                color = CalSnapColors.Ink2,
                lineHeight = 19.sp,
            )
        }

        Spacer(Modifier.weight(1f))

        CalSnapPrimaryButton(text = "Continue", onClick = onContinue)

        Spacer(Modifier.height(CalSnapSpacing.lg))
    }
}

@Composable
private fun GoalCard(
    option: GoalOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CalSnapRadius.card))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = if (isSelected) CalSnapColors.Ink else Color.Transparent,
                shape = RoundedCornerShape(CalSnapRadius.card),
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(CalSnapSpacing.cardPad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Symbol icon box
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isSelected) CalSnapColors.Ink else CalSnapColors.SurfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.symbol,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else CalSnapColors.Ink,
            )
        }

        // Title + subtitle
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.3).sp,
                color = CalSnapColors.Ink,
            )
            Text(
                text = option.subtitle,
                fontSize = 13.sp,
                color = CalSnapColors.Muted,
            )
        }

        // Radio — ink filled with check when selected
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (isSelected) CalSnapColors.Ink else Color.Transparent)
                .border(
                    width = 2.dp,
                    color = if (isSelected) CalSnapColors.Ink else Color(0xFFD8D2C5),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                CalSnapIcon(name = "check", size = 12.dp, color = Color.White, strokeWidth = 3f)
            }
        }
    }
}
