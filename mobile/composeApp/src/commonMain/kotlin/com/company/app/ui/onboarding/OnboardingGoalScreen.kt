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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.components.CalSnapIcon
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
            .background(CalSnapColors.Surface)
            .padding(horizontal = CalSnapSpacing.screenPad),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(CalSnapSpacing.xxl))

        StepIndicator(current = 1, total = 4, showBack = false)

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

        // Tip callout
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CalSnapRadius.md))
                .background(CalSnapColors.RedSoft)
                .padding(horizontal = CalSnapSpacing.md, vertical = CalSnapSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
        ) {
            Text("✦", fontSize = 14.sp, color = CalSnapColors.Red)
            Text(
                text = "Tip — most people lose 0.5–1 lb / week comfortably.",
                style = CalSnapType.Body,
                color = CalSnapColors.Red,
            )
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
                .size(48.dp)
                .clip(RoundedCornerShape(CalSnapRadius.md))
                .background(CalSnapColors.SurfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = option.emoji, fontSize = 24.sp)
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

        // Radio circle: filled Ink when selected, outlined Border when not
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(if (isSelected) CalSnapColors.Ink else CalSnapColors.Background)
                .border(
                    width = 2.dp,
                    color = if (isSelected) CalSnapColors.Ink else CalSnapColors.Border,
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(CalSnapColors.Background),
                )
            }
        }
    }
}

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

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            repeat(total) { idx ->
                val isFilled = idx + 1 <= current
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(CalSnapRadius.pill))
                        .background(
                            if (isFilled) CalSnapColors.Ink
                            else CalSnapColors.Ink.copy(alpha = 0.08f)
                        )
                )
            }
        }

        // Spacer to balance left side
        if (showBack) Spacer(Modifier.size(36.dp))
    }
}
