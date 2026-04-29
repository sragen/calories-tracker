package com.company.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.theme.CalSnapColors
import com.company.app.ui.theme.CalSnapElevation

enum class CalSnapTab { HOME, STATS, LOG, PROFILE }

private data class TabItem(
    val tab: CalSnapTab,
    val icon: String,
    val label: String,
)

private val TABS = listOf(
    TabItem(CalSnapTab.HOME,    "home",    "Home"),
    TabItem(CalSnapTab.STATS,   "chart",   "Stats"),
    TabItem(CalSnapTab.LOG,     "fork",    "Log"),
    TabItem(CalSnapTab.PROFILE, "profile", "Profile"),
)

/**
 * Bottom tab bar with an elevated center Snap FAB.
 *
 * Layout: [Home] [Stats] [● Snap ●] [Log] [Profile]
 * The Snap button is a raised circular FAB that opens the camera.
 *
 * Animated indicator: active tab label turns Ink, inactive is Mute2.
 * Tab bar morph animation (A8): spring-animated active indicator underline.
 */
@Composable
fun CalSnapBottomTabBar(
    selectedTab: CalSnapTab,
    onTabSelected: (CalSnapTab) -> Unit,
    onSnapTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(CalSnapColors.Background),
    ) {
        // Top border
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(CalSnapColors.Divider)
                .align(Alignment.TopStart),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp, bottom = 28.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // First 2 tabs
            TABS.take(2).forEach { item ->
                TabButton(
                    item = item,
                    isSelected = selectedTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                )
            }

            // Center Snap FAB
            SnapFab(onSnapTap)

            // Last 2 tabs
            TABS.drop(2).forEach { item ->
                TabButton(
                    item = item,
                    isSelected = selectedTab == item.tab,
                    onClick = { onTabSelected(item.tab) },
                )
            }
        }
    }
}

@Composable
private fun TabButton(
    item: TabItem,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val iconColor = if (isSelected) CalSnapColors.Ink else CalSnapColors.Mute2
    val labelColor = if (isSelected) CalSnapColors.Ink else CalSnapColors.Mute2

    // A8 spring animation: indicator underline width
    val indicatorWidth by animateDpAsState(
        targetValue = if (isSelected) 20.dp else 0.dp,
        animationSpec = spring(stiffness = 600f, dampingRatio = 0.6f),
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        CalSnapIcon(name = item.icon, size = 22.dp, color = iconColor, strokeWidth = 2f)
        Spacer(Modifier.height(3.dp))
        Text(
            text = item.label,
            fontSize = 10.sp,
            fontWeight = FontWeight.W600,
            color = labelColor,
        )
        Spacer(Modifier.height(3.dp))
        // A8 active indicator underline
        Box(
            modifier = Modifier
                .width(indicatorWidth)
                .height(2.dp)
                .clip(RoundedCornerShape(1.dp))
                .background(CalSnapColors.Ink),
        )
    }
}

@Composable
private fun SnapFab(onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(56.dp)
            .offset(y = (-22).dp)
            .shadow(elevation = CalSnapElevation.sheet, shape = CircleShape, spotColor = CalSnapColors.Ink.copy(alpha = 0.25f))
            .clip(CircleShape)
            .background(CalSnapColors.Ink)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
    ) {
        CalSnapIcon(
            name = "camera",
            size = 26.dp,
            color = CalSnapColors.Background,
            strokeWidth = 2.2f,
        )
    }
}
