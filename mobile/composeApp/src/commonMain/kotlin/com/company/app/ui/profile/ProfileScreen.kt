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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.BodyProfileResponse
import com.company.app.shared.data.model.DailyGoalResponse
import com.company.app.shared.data.model.EntitlementResponse
import com.company.app.ui.components.CalSnapBottomTabBar
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.components.CalSnapTab
import com.company.app.ui.theme.*

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    streak: Int = 0,
    onTabSelected: (CalSnapTab) -> Unit = {},
    onSnapTap: () -> Unit = {},
    onSubscription: () -> Unit = {},
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
                        .verticalScroll(rememberScrollState())
                        .padding(bottom = 100.dp),
                ) {
                    ProfilePageHeader()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = CalSnapSpacing.screenPad),
                        verticalArrangement = Arrangement.spacedBy(0.dp),
                    ) {
                        UserCard(streak = streak)
                        Spacer(Modifier.height(12.dp))
                        MiniStatsRow(goal = state.goal)
                        Spacer(Modifier.height(22.dp))
                        AccountSection(profile = state.profile, goal = state.goal)
                        Spacer(Modifier.height(22.dp))
                        AppSection(
                            entitlement = state.entitlement,
                            onSubscription = onSubscription,
                            onLogout = viewModel::logout,
                        )
                        Spacer(Modifier.height(22.dp))
                    }
                }
            }
        }

        CalSnapBottomTabBar(
            selectedTab = CalSnapTab.PROFILE,
            onTabSelected = { tab ->
                if (tab != CalSnapTab.PROFILE) onTabSelected(tab)
            },
            onSnapTap = onSnapTap,
            modifier = Modifier.align(Alignment.BottomCenter),
        )
    }
}

// ─── Page header ────────────────────────────────────────────────────────────

@Composable
private fun ProfilePageHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad)
            .padding(top = 60.dp, bottom = 16.dp),
    ) {
        Text(
            text = "Profile",
            fontSize = 26.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.6).sp,
        )
    }
}

// ─── User card ───────────────────────────────────────────────────────────────

@Composable
private fun UserCard(streak: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(22.dp), ambientColor = Color(0x0A140F08), spotColor = Color(0x0F140F08))
            .clip(RoundedCornerShape(22.dp))
            .background(CalSnapColors.Background)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        colors = listOf(CalSnapColors.Carb, CalSnapColors.Red),
                        start = Offset(0f, 0f),
                        end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "S",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.W700,
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "My Profile",
                fontSize = 18.sp,
                fontWeight = FontWeight.W700,
                color = CalSnapColors.Ink,
                letterSpacing = (-0.3).sp,
            )
            if (streak >= 1) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(CalSnapRadius.pill))
                        .background(CalSnapColors.RedSoft)
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    CalSnapIcon(name = "streak", size = 11.dp, color = CalSnapColors.Red, strokeWidth = 2.4f)
                    Text(
                        text = "$streak-day streak",
                        color = CalSnapColors.Red,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.W700,
                    )
                }
            }
        }
    }
}

// ─── Mini stats ──────────────────────────────────────────────────────────────

@Composable
private fun MiniStatsRow(goal: DailyGoalResponse?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MiniStatCard(
            label = "Target",
            value = goal?.targetCalories?.toInt()?.toString() ?: "—",
            unit = "kcal",
            modifier = Modifier.weight(1f),
        )
        MiniStatCard(
            label = "Protein",
            value = goal?.targetProteinG?.toInt()?.toString() ?: "—",
            unit = "g/day",
            modifier = Modifier.weight(1f),
        )
        MiniStatCard(
            label = "Fat",
            value = goal?.targetFatG?.toInt()?.toString() ?: "—",
            unit = "g/day",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, unit: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp), ambientColor = Color(0x0A140F08), spotColor = Color(0x0A140F08))
            .clip(RoundedCornerShape(16.dp))
            .background(CalSnapColors.Background)
            .padding(vertical = 14.dp, horizontal = 12.dp),
    ) {
        Text(
            text = label.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.W600,
            color = CalSnapColors.Muted,
            letterSpacing = 0.5.sp,
        )
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.W700,
            color = CalSnapColors.Ink,
            letterSpacing = (-0.4).sp,
            modifier = Modifier.padding(top = 2.dp),
        )
        Text(
            text = unit,
            fontSize = 11.sp,
            color = CalSnapColors.Muted,
            modifier = Modifier.padding(top = 1.dp),
        )
    }
}

// ─── Settings sections ────────────────────────────────────────────────────────

@Composable
private fun AccountSection(profile: BodyProfileResponse?, goal: DailyGoalResponse?) {
    val activityLabel = profile?.activityLevel
        ?.replace("_", " ")
        ?.lowercase()
        ?.replaceFirstChar { it.uppercase() } ?: "—"

    val goalLabel = when (profile?.goal) {
        "LOSE" -> "Lose Weight"
        "GAIN" -> "Gain Muscle"
        else -> "Maintain"
    }

    SectionGroup(title = "Account") {
        SettingsRow(
            icon = "profile",
            label = "Personal info",
            detail = "Name, email",
        )
        SettingsRow(
            icon = "weight",
            label = "Body profile",
            detail = if (profile != null) "${profile.weightKg.toInt()} kg · ${profile.heightCm.toInt()} cm" else "Not set",
        )
        SettingsRow(
            icon = "flame",
            label = "Daily targets",
            detail = if (goal != null) "${goal.targetCalories.toInt()} kcal · $goalLabel" else "Not set",
            isLast = true,
        )
    }
}

@Composable
private fun AppSection(
    entitlement: EntitlementResponse?,
    onSubscription: () -> Unit,
    onLogout: () -> Unit,
) {
    SectionGroup(title = "App") {
        SettingsRow(
            icon = "bell",
            label = "Reminders",
            detail = "Breakfast, lunch, dinner",
        )
        SettingsRow(
            icon = "sparkle",
            label = "CalSnap Pro",
            detail = if (entitlement?.entitled == true) "Active" else "Manage subscription",
            iconBg = CalSnapColors.Red,
            iconTint = Color.White,
            onClick = onSubscription,
        )
        SettingsRow(
            icon = "lock",
            label = "Privacy",
            detail = "Data & permissions",
        )
        SettingsRow(
            icon = "close",
            label = "Sign Out",
            detail = "",
            labelColor = CalSnapColors.Red,
            isLast = true,
            onClick = onLogout,
        )
    }
}

@Composable
private fun SectionGroup(title: String, content: @Composable () -> Unit) {
    Text(
        text = title.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.W700,
        color = CalSnapColors.Muted,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp), ambientColor = Color(0x0A140F08), spotColor = Color(0x0F140F08))
            .clip(RoundedCornerShape(18.dp))
            .background(CalSnapColors.Background),
    ) {
        content()
    }
}

@Composable
private fun SettingsRow(
    icon: String,
    label: String,
    detail: String,
    iconBg: Color = CalSnapColors.SurfaceAlt,
    iconTint: Color = CalSnapColors.Ink,
    labelColor: Color = CalSnapColors.Ink,
    isLast: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (onClick != null) Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    ) else Modifier
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center,
            ) {
                CalSnapIcon(name = icon, size = 18.dp, color = iconTint, strokeWidth = 2f)
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    color = labelColor,
                    letterSpacing = (-0.2).sp,
                )
                if (detail.isNotEmpty()) {
                    Text(
                        text = detail,
                        fontSize = 12.sp,
                        color = CalSnapColors.Muted,
                        modifier = Modifier.padding(top = 1.dp),
                    )
                }
            }

            CalSnapIcon(name = "chev-r", size = 16.dp, color = CalSnapColors.Mute2)
        }

        if (!isLast) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 66.dp)
                    .height(1.dp)
                    .background(CalSnapColors.Divider),
            )
        }
    }
}
