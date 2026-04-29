package com.company.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.BodyProfileResponse
import com.company.app.shared.data.model.DailyGoalResponse
import com.company.app.shared.data.model.EntitlementResponse
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.components.CalSnapTextButton
import com.company.app.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Surface),
    ) {
        when {
            state.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CalSnapColors.Red,
                )
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    ProfileHeader()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = CalSnapSpacing.screenPad),
                        verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
                    ) {
                        state.profile?.let { BodyProfileCard(it) }
                        state.goal?.let { DailyTargetsCard(it) }
                        state.entitlement?.let { SubscriptionCard(it) }

                        Spacer(Modifier.height(CalSnapSpacing.sm))

                        CalSnapTextButton(
                            text = "Sign Out",
                            onClick = viewModel::logout,
                            color = CalSnapColors.Red,
                        )

                        Spacer(Modifier.height(CalSnapSpacing.xl))
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad)
            .padding(top = CalSnapSpacing.lg, bottom = CalSnapSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(CalSnapColors.SurfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "profile", size = 36.dp, color = CalSnapColors.Muted)
        }
        Text(
            text = "Your Profile",
            style = CalSnapType.HeadlineMedium,
            color = CalSnapColors.Ink,
        )
    }
}

@Composable
private fun BodyProfileCard(profile: BodyProfileResponse) {
    SectionCard(title = "Body Profile", icon = "weight") {
        val activityLabel = profile.activityLevel
            .replace("_", " ")
            .lowercase()
            .replaceFirstChar { it.uppercase() }

        val goalLabel = when (profile.goal) {
            "LOSE" -> "Lose Weight"
            "GAIN" -> "Gain Muscle"
            else -> "Maintain"
        }

        StatRow("Height", "${profile.heightCm.toInt()} cm")
        StatRow("Weight", "${profile.weightKg.toInt()} kg")
        StatRow("Gender", profile.gender.lowercase().replaceFirstChar { it.uppercase() })
        StatRow("Activity", activityLabel)
        StatRow("Goal", goalLabel)
        profile.bmrKcal?.let { StatRow("BMR", "${it.toInt()} kcal") }
        profile.tdeeKcal?.let { StatRow("TDEE", "${it.toInt()} kcal") }
        profile.recommendedCalories?.let { StatRow("Recommended", "${it.toInt()} kcal/day") }
    }
}

@Composable
private fun DailyTargetsCard(goal: DailyGoalResponse) {
    SectionCard(title = "Daily Targets", icon = "flash") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            MacroTargetCell(value = "${goal.targetCalories.toInt()}", label = "kcal", accent = CalSnapColors.Ink)
            MacroTargetCell(value = "${goal.targetProteinG.toInt()}g", label = "Protein", accent = CalSnapColors.Protein)
            MacroTargetCell(value = "${goal.targetCarbsG.toInt()}g", label = "Carbs", accent = CalSnapColors.Carb)
            MacroTargetCell(value = "${goal.targetFatG.toInt()}g", label = "Fat", accent = CalSnapColors.Fat)
        }
    }
}

@Composable
private fun SubscriptionCard(entitlement: EntitlementResponse) {
    SectionCard(title = "Subscription", icon = "star") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = if (entitlement.entitled) "Premium" else "Free plan",
                style = CalSnapType.BodyLarge,
                color = CalSnapColors.Ink,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(CalSnapRadius.pill))
                    .background(
                        if (entitlement.entitled) CalSnapColors.CarbBg else CalSnapColors.SurfaceAlt
                    )
                    .padding(horizontal = 10.dp, vertical = 3.dp),
            ) {
                Text(
                    text = if (entitlement.entitled) "ACTIVE" else "FREE",
                    style = CalSnapType.Label,
                    color = if (entitlement.entitled) CalSnapColors.Warn else CalSnapColors.Muted,
                )
            }
        }

        if (entitlement.entitled) {
            Spacer(Modifier.height(CalSnapSpacing.sm))
            entitlement.status?.let { StatRow("Status", it.replaceFirstChar { c -> c.uppercase() }) }
            entitlement.expiresAt?.let { StatRow("Expires", it.take(10)) }
            entitlement.source?.let { StatRow("Source", it) }
        } else {
            Spacer(Modifier.height(CalSnapSpacing.sm))
            Text(
                text = "Upgrade to unlock AI Scan history, Analytics, and more.",
                style = CalSnapType.Body,
                color = CalSnapColors.Muted,
            )
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: String,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(CalSnapRadius.card))
            .background(CalSnapColors.Background)
            .padding(CalSnapSpacing.cardPad),
        verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
        ) {
            CalSnapIcon(name = icon, size = 18.dp, color = CalSnapColors.Muted)
            Text(
                text = title,
                style = CalSnapType.HeadlineMedium,
                color = CalSnapColors.Ink,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CalSnapColors.Divider),
        )
        content()
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = CalSnapType.Body, color = CalSnapColors.Muted)
        Text(value, style = CalSnapType.Body, color = CalSnapColors.Ink)
    }
}

@Composable
private fun MacroTargetCell(
    value: String,
    label: String,
    accent: androidx.compose.ui.graphics.Color,
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = CalSnapType.HeadlineMedium, color = accent)
        Text(label, style = CalSnapType.Label, color = CalSnapColors.Muted)
    }
}
