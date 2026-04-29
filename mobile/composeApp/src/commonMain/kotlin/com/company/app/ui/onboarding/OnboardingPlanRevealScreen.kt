package com.company.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.company.app.shared.data.model.BmrPreviewResponse
import com.company.app.ui.components.CalSnapBrandButton
import com.company.app.ui.components.CalSnapMacroBar
import com.company.app.ui.components.CalSnapTextButton
import com.company.app.ui.theme.*

@Composable
fun OnboardingPlanRevealScreen(
    preview: BmrPreviewResponse?,
    isLoading: Boolean,
    onStart: () -> Unit,
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

        StepIndicator(current = 4, total = 4, showBack = true, onBack = onBack)

        Spacer(Modifier.height(CalSnapSpacing.xl))

        Text(
            text = "Your plan is ready",
            style = CalSnapType.HeadlineLarge,
            color = CalSnapColors.Ink,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(CalSnapSpacing.sm))

        Text(
            text = "Personalized just for you.",
            style = CalSnapType.Body,
            color = CalSnapColors.Muted,
            textAlign = TextAlign.Center,
        )

        Spacer(Modifier.height(CalSnapSpacing.xl))

        when {
            isLoading || preview == null -> {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator(color = CalSnapColors.Red)
                Spacer(Modifier.weight(1f))
            }
            else -> {
                PlanCard(preview = preview)
                Spacer(Modifier.weight(1f))
            }
        }

        CalSnapBrandButton(
            text = "Start Tracking →",
            onClick = onStart,
            enabled = !isLoading && preview != null,
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
private fun PlanCard(preview: BmrPreviewResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CalSnapRadius.xl))
            .background(CalSnapColors.Surface)
            .padding(CalSnapSpacing.cardPadLg),
    ) {
        Text(
            text = "DAILY CALORIES",
            style = CalSnapType.Label,
            color = CalSnapColors.Muted,
        )

        Spacer(Modifier.height(CalSnapSpacing.xs))

        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = preview.recommendedCalories.toInt().toString(),
                style = CalSnapType.Display,
                color = CalSnapColors.Ink,
            )
            Text(
                text = "kcal",
                style = CalSnapType.BodyLarge,
                color = CalSnapColors.Muted,
                modifier = Modifier.padding(bottom = 14.dp),
            )
        }

        Spacer(Modifier.height(CalSnapSpacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md)) {
            CalSnapMacroBar(
                label = "Protein",
                current = preview.recommendedProteinG.toFloat(),
                target = preview.recommendedProteinG.toFloat(),
                color = CalSnapColors.Protein,
                modifier = Modifier.fillMaxWidth(),
            )
            CalSnapMacroBar(
                label = "Carbs",
                current = preview.recommendedCarbsG.toFloat(),
                target = preview.recommendedCarbsG.toFloat(),
                color = CalSnapColors.Carb,
                modifier = Modifier.fillMaxWidth(),
            )
            CalSnapMacroBar(
                label = "Fat",
                current = preview.recommendedFatG.toFloat(),
                target = preview.recommendedFatG.toFloat(),
                color = CalSnapColors.Fat,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
