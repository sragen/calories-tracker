package com.company.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.company.app.shared.data.model.BmrPreviewResponse
import com.company.app.ui.components.CalSnapBrandButton
import com.company.app.ui.components.CalSnapIcon
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
    ) {
        Spacer(Modifier.height(CalSnapSpacing.xxl))

        StepIndicator(current = 4, total = 4, showBack = true, onBack = onBack)

        Spacer(Modifier.height(CalSnapSpacing.xl))

        // "✦ Your plan" badge
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(CalSnapRadius.pill))
                .background(CalSnapColors.Ink)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            CalSnapIcon(name = "sparkle", size = 12.dp, color = Color.White, strokeWidth = 2.4f)
            Text(
                text = "YOUR PLAN",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.4.sp,
                color = Color.White,
            )
        }

        Spacer(Modifier.height(16.dp))

        Text(
            text = buildAnnotatedString {
                append("Hit this every day\nand you'll reach ")
                withStyle(SpanStyle(color = CalSnapColors.Red)) {
                    append("your goal.")
                }
            },
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = (-1).sp,
            color = CalSnapColors.Ink,
            lineHeight = 33.sp,
        )

        Spacer(Modifier.height(CalSnapSpacing.lg))

        when {
            isLoading || preview == null -> {
                Spacer(Modifier.weight(1f))
                CircularProgressIndicator(
                    color = CalSnapColors.Red,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
                Spacer(Modifier.weight(1f))
            }
            else -> {
                PlanCard(preview = preview)

                Spacer(Modifier.height(CalSnapSpacing.md))

                // Green goal callout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(CalSnapColors.GoodBg)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CalSnapIcon(name = "check", size = 18.dp, color = CalSnapColors.Good, strokeWidth = 2.5f)
                    Text(
                        text = "Your personalized plan is ready — let's start!",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = CalSnapColors.Good,
                    )
                }

                Spacer(Modifier.weight(1f))
            }
        }

        CalSnapBrandButton(
            text = "Let's crush today →",
            onClick = onStart,
            enabled = !isLoading && preview != null,
        )

        Spacer(Modifier.height(CalSnapSpacing.xs))

        CalSnapTextButton(text = "Back", onClick = onBack)

        Spacer(Modifier.height(CalSnapSpacing.lg))
    }
}

@Composable
private fun PlanCard(preview: BmrPreviewResponse) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(Color.White)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "DAILY CALORIES",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.2.sp,
            color = CalSnapColors.Muted,
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text = preview.recommendedCalories.toInt().toString(),
            style = CalSnapType.Display,
            color = CalSnapColors.Ink,
        )

        Text(
            text = "kcal · ${(preview.recommendedCalories - 500).toInt()} below maintenance",
            fontSize = 13.sp,
            color = CalSnapColors.Muted,
        )

        Spacer(Modifier.height(CalSnapSpacing.md))

        HorizontalDivider(color = CalSnapColors.Divider)

        Spacer(Modifier.height(CalSnapSpacing.md))

        // Macro tiles side-by-side with colored backgrounds
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            MacroTile(
                label = "Protein",
                value = preview.recommendedProteinG.toInt().toString(),
                unit = "g",
                tint = CalSnapColors.Protein,
                bg = CalSnapColors.ProteinBg,
                modifier = Modifier.weight(1f),
            )
            MacroTile(
                label = "Carbs",
                value = preview.recommendedCarbsG.toInt().toString(),
                unit = "g",
                tint = CalSnapColors.Carb,
                bg = CalSnapColors.CarbBg,
                modifier = Modifier.weight(1f),
            )
            MacroTile(
                label = "Fat",
                value = preview.recommendedFatG.toInt().toString(),
                unit = "g",
                tint = CalSnapColors.Fat,
                bg = CalSnapColors.FatBg,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MacroTile(
    label: String,
    value: String,
    unit: String,
    tint: Color,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 12.dp),
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            color = CalSnapColors.Muted,
        )
        Spacer(Modifier.height(2.dp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp,
                color = CalSnapColors.Ink,
            )
            Text(
                text = unit,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal,
                color = CalSnapColors.Muted,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
    }
}
