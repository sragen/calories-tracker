package com.company.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.components.CalSnapTextButton
import com.company.app.ui.theme.*

private data class ActivityOption(
    val key: String,
    val number: String,
    val title: String,
    val description: String,
)

private val ACTIVITY_OPTIONS = listOf(
    ActivityOption("SEDENTARY",        "1", "Sedentary",       "Mostly sitting, little exercise"),
    ActivityOption("LIGHTLY_ACTIVE",   "2", "Lightly active",  "Light walks, 1–2 workouts / week"),
    ActivityOption("MODERATELY_ACTIVE","3", "Moderate",        "3–5 workouts / week"),
    ActivityOption("VERY_ACTIVE",      "4", "Very active",     "Daily training"),
    ActivityOption("EXTRA_ACTIVE",     "5", "Athlete",         "Multiple sessions / day"),
)

@Composable
fun OnboardingActivityScreen(
    selectedActivity: String,
    onActivitySelected: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background)
            .padding(horizontal = CalSnapSpacing.screenPad),
    ) {
        Spacer(Modifier.height(CalSnapSpacing.xxl))

        StepIndicator(current = 3, total = 4, showBack = true, onBack = onBack)

        Spacer(Modifier.height(CalSnapSpacing.lg))

        Text(
            text = "How active are you?",
            style = CalSnapType.HeadlineLarge,
            color = CalSnapColors.Ink,
        )

        Spacer(Modifier.height(CalSnapSpacing.sm))

        Text(
            text = "Outside of intentional workouts.",
            style = CalSnapType.Body,
            color = CalSnapColors.Muted,
        )

        Spacer(Modifier.height(CalSnapSpacing.xl))

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
        ) {
            ACTIVITY_OPTIONS.forEach { option ->
                ActivityCard(
                    option = option,
                    isSelected = selectedActivity == option.key,
                    onClick = { onActivitySelected(option.key) },
                )
            }
        }

        Spacer(Modifier.height(CalSnapSpacing.md))

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
private fun ActivityCard(
    option: ActivityOption,
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
                .size(40.dp)
                .clip(RoundedCornerShape(CalSnapRadius.md))
                .background(if (isSelected) CalSnapColors.Ink else CalSnapColors.SurfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = option.number,
                style = CalSnapType.HeadlineMedium,
                color = if (isSelected) CalSnapColors.Background else CalSnapColors.Ink,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = option.title,
                style = CalSnapType.BodyLarge,
                color = CalSnapColors.Ink,
            )
            Text(
                text = option.description,
                style = CalSnapType.BodySmall,
                color = CalSnapColors.Muted,
            )
        }

        if (isSelected) {
            CalSnapIcon(name = "check", size = 18.dp, color = CalSnapColors.Ink, strokeWidth = 2.5f)
        }
    }
}
