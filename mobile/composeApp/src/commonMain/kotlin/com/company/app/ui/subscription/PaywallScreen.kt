package com.company.app.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.components.CalSnapBrandButton
import com.company.app.ui.components.CalSnapTextButton
import com.company.app.ui.theme.*

private val FEATURES = listOf(
    "camera"  to "Unlimited AI food scans",
    "chart"   to "Weekly analytics & trends",
    "flash"   to "Full macro & nutrition tracking",
    "star"    to "Priority support",
)

@Composable
fun PaywallScreen(
    viewModel: SubscriptionViewModel,
    isGuestMode: Boolean = false,
    onEntitled: () -> Unit,
    onRegister: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()

    when (val s = state) {
        is SubscriptionState.Entitled -> onEntitled()
        else -> PaywallContent(
            isPurchasing = s is SubscriptionState.Purchasing,
            errorMessage = (s as? SubscriptionState.Error)?.message,
            isGuestMode = isGuestMode,
            onSubscribe = { viewModel.purchase() },
            onRestore = { viewModel.restore() },
            onRegister = onRegister,
            onBack = onBack,
        )
    }
}

@Composable
private fun PaywallContent(
    isPurchasing: Boolean,
    errorMessage: String?,
    isGuestMode: Boolean,
    onSubscribe: () -> Unit,
    onRestore: () -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CalSnapSpacing.screenPad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(CalSnapSpacing.xxl))

            // Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(CalSnapColors.CarbBg),
                contentAlignment = Alignment.Center,
            ) {
                Text("★", fontSize = 32.sp)
            }

            Spacer(Modifier.height(CalSnapSpacing.lg))

            Text(
                text = if (isGuestMode) buildAnnotatedString {
                    append("Your free scans\nare ")
                    withStyle(SpanStyle(color = CalSnapColors.Red)) { append("used up.") }
                } else buildAnnotatedString {
                    append("Go ")
                    withStyle(SpanStyle(color = CalSnapColors.Red)) { append("Premium") }
                },
                style = CalSnapType.HeadlineLarge,
                color = CalSnapColors.Ink,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(CalSnapSpacing.sm))

            Text(
                text = "Unlock unlimited AI scans,\nmacro tracking, and more.",
                style = CalSnapType.Body,
                color = CalSnapColors.Muted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(CalSnapSpacing.xl))

            // Price card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(CalSnapRadius.xl))
                    .background(CalSnapColors.Surface)
                    .padding(CalSnapSpacing.cardPadLg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.xs),
            ) {
                Text(
                    text = "CalSnap Premium",
                    style = CalSnapType.HeadlineMedium,
                    color = CalSnapColors.Ink,
                )
                Text(
                    text = "Rp 499.000 / year",
                    style = CalSnapType.Display.copy(fontSize = 36.sp),
                    color = CalSnapColors.Ink,
                )
                Text(
                    text = "Rp 41.583/mo  •  7-day free trial",
                    style = CalSnapType.BodySmall,
                    color = CalSnapColors.Muted,
                )
            }

            Spacer(Modifier.height(CalSnapSpacing.lg))

            // Feature list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
            ) {
                FEATURES.forEach { (icon, label) ->
                    FeatureRow(icon = icon, label = label)
                }
            }

            errorMessage?.let {
                Spacer(Modifier.height(CalSnapSpacing.sm))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(CalSnapRadius.md))
                        .background(CalSnapColors.RedSoft)
                        .padding(CalSnapSpacing.md),
                ) {
                    Text(it, style = CalSnapType.BodySmall, color = CalSnapColors.Red)
                }
            }

            Spacer(Modifier.weight(1f))

            if (isPurchasing) {
                CircularProgressIndicator(color = CalSnapColors.Red, modifier = Modifier.size(28.dp))
                Spacer(Modifier.height(CalSnapSpacing.md))
            } else {
                CalSnapBrandButton(
                    text = "Start 7-Day Free Trial",
                    onClick = onSubscribe,
                )
                Spacer(Modifier.height(CalSnapSpacing.xs))
                if (isGuestMode) {
                    CalSnapTextButton(
                        text = "Create free account instead",
                        onClick = onRegister,
                    )
                } else {
                    CalSnapTextButton(
                        text = "Restore Purchase",
                        onClick = onRestore,
                    )
                }
                CalSnapTextButton(
                    text = if (isGuestMode) "← Back to trial" else "← Back",
                    onClick = onBack,
                    color = CalSnapColors.Hint,
                )
            }

            Spacer(Modifier.height(CalSnapSpacing.lg))
        }
    }
}

@Composable
private fun FeatureRow(icon: String, label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(CalSnapRadius.sm))
                .background(CalSnapColors.SurfaceAlt),
            contentAlignment = Alignment.Center,
        ) {
            com.company.app.ui.components.CalSnapIcon(
                name = icon,
                size = 18.dp,
                color = CalSnapColors.Ink,
            )
        }
        Text(
            text = label,
            style = CalSnapType.Body,
            color = CalSnapColors.Ink,
        )
    }
}
