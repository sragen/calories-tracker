package com.company.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.theme.*

private data class GoalOption(val key: String, val emoji: String, val title: String, val subtitle: String)

private val GOAL_OPTIONS = listOf(
    GoalOption("LOSE", "🔥", "Lose Weight", "Burn fat, feel lighter"),
    GoalOption("MAINTAIN", "⚖️", "Stay in Shape", "Keep your current weight"),
    GoalOption("GAIN", "💪", "Build Muscle", "Gain strength and size"),
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
            .background(CalSnapColors.Background)
            .padding(horizontal = CalSnapSpacing.screenPad),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(CalSnapSpacing.xxl))

        StepIndicator(current = 1, total = 4)

        Spacer(Modifier.height(CalSnapSpacing.lg))

        Text(
            text = "What's your goal?",
            style = CalSnapType.HeadlineLarge,
            color = CalSnapColors.Ink,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(CalSnapSpacing.sm))

        Text(
            text = "We'll build your personal nutrition plan.",
            style = CalSnapType.Body,
            color = CalSnapColors.Muted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(CalSnapSpacing.xl))

        Column(verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md)) {
            GOAL_OPTIONS.forEach { option ->
                GoalCard(
                    option = option,
                    isSelected = selectedGoal == option.key,
                    onClick = { onGoalSelected(option.key) },
                )
            }
        }

        Spacer(Modifier.weight(1f))

        CalSnapPrimaryButton(
            text = "Continue",
            onClick = onContinue,
        )

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
            .background(if (isSelected) CalSnapColors.Surface else CalSnapColors.Background)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) CalSnapColors.Ink else CalSnapColors.Border,
                shape = RoundedCornerShape(CalSnapRadius.card),
            )
            .clickable(onClick = onClick)
            .padding(CalSnapSpacing.cardPad),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(CalSnapRadius.md))
                .background(CalSnapColors.SurfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = option.emoji, fontSize = 26.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
            )
            Text(
                text = option.subtitle,
                style = CalSnapType.Body,
                color = CalSnapColors.Muted,
            )
        }

        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(CalSnapRadius.pill))
                    .background(CalSnapColors.Ink),
                contentAlignment = Alignment.Center,
            ) {
                Text("✓", color = CalSnapColors.Background, fontSize = 12.sp)
            }
        }
    }
}

@Composable
internal fun StepIndicator(current: Int, total: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        repeat(total) { idx ->
            val isActive = idx + 1 == current
            val isDone = idx + 1 < current
            Box(
                modifier = Modifier
                    .height(4.dp)
                    .then(if (isActive) Modifier.width(24.dp) else Modifier.size(4.dp))
                    .clip(RoundedCornerShape(CalSnapRadius.pill))
                    .background(
                        when {
                            isActive || isDone -> CalSnapColors.Ink
                            else -> CalSnapColors.Divider
                        }
                    )
            )
        }
    }
}
