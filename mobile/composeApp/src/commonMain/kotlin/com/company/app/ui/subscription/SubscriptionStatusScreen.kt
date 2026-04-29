package com.company.app.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.theme.*

@Composable
fun SubscriptionStatusScreen(
    viewModel: SubscriptionViewModel,
    onBack: () -> Unit,
) {
    val state by viewModel.state.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Surface),
    ) {
        // Back button
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(CalSnapSpacing.screenPad)
                .size(36.dp)
                .clip(CircleShape)
                .background(CalSnapColors.SurfaceAlt)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = "chev-l", size = 18.dp, color = CalSnapColors.Ink)
        }

        when (val s = state) {
            is SubscriptionState.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = CalSnapColors.Red,
                )
            }
            is SubscriptionState.Entitled -> {
                EntitledContent(state = s, modifier = Modifier.align(Alignment.Center))
            }
            is SubscriptionState.Paywall -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.md),
                ) {
                    Text("No active subscription", style = CalSnapType.HeadlineMedium, color = CalSnapColors.Ink)
                    CalSnapPrimaryButton(text = "Go Back", onClick = onBack, modifier = Modifier.width(200.dp))
                }
            }
            is SubscriptionState.Error -> {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(CalSnapSpacing.xl)
                        .clip(RoundedCornerShape(CalSnapRadius.md))
                        .background(CalSnapColors.RedSoft)
                        .padding(CalSnapSpacing.md),
                ) {
                    Text(s.message, style = CalSnapType.Body, color = CalSnapColors.Red, textAlign = TextAlign.Center)
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun EntitledContent(
    state: SubscriptionState.Entitled,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = CalSnapSpacing.screenPad),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.lg),
    ) {
        // Active badge
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(CalSnapColors.GoodBg),
            contentAlignment = Alignment.Center,
        ) {
            Text("★", fontSize = 36.sp)
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.xs),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(CalSnapRadius.pill))
                    .background(CalSnapColors.GoodBg)
                    .padding(horizontal = CalSnapSpacing.md, vertical = 4.dp),
            ) {
                Text(
                    text = "ACTIVE",
                    style = CalSnapType.Label,
                    color = CalSnapColors.Good,
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Premium Access",
                style = CalSnapType.HeadlineLarge,
                color = CalSnapColors.Ink,
            )
        }

        // Detail card
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CalSnapRadius.card))
                .background(CalSnapColors.Background)
                .padding(CalSnapSpacing.cardPad),
            verticalArrangement = Arrangement.spacedBy(CalSnapSpacing.sm),
        ) {
            state.expiresAt?.let {
                DetailRow("Renews", it.take(10))
            }
            state.status?.let {
                DetailRow("Status", it.replaceFirstChar { c -> c.uppercase() })
            }
            DetailRow("Source", state.source)
        }

        Text(
            text = "To cancel, visit your device subscription settings\n(Google Play or App Store).",
            style = CalSnapType.BodySmall,
            color = CalSnapColors.Muted,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = CalSnapType.Body, color = CalSnapColors.Muted)
        Text(value, style = CalSnapType.Body, color = CalSnapColors.Ink)
    }
}
