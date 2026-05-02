package com.company.app.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.SubscriptionPlanResponse
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.theme.*
import kotlin.math.roundToInt

private val FEATURES = listOf(
    Triple("sparkle", "Unlimited AI food scans", "Free is capped at 5 / day"),
    Triple("chart", "Advanced trends & insights", "Weekly reports, deficit tracking"),
    Triple("star", "Priority barcode lookups", "4M+ products, no waits"),
    Triple("edit", "Custom recipes & meals", "Save your go-tos, sync everywhere"),
)

private val Amber = Color(0xFFF4A23A)

@Composable
fun PaywallScreen(
    viewModel: SubscriptionViewModel,
    isGuestMode: Boolean = false,
    onEntitled: () -> Unit,
    onRegister: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val plans by viewModel.plans.collectAsState()

    when (val s = state) {
        is SubscriptionState.Entitled -> onEntitled()
        else -> PaywallContent(
            plans = plans,
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
    plans: List<SubscriptionPlanResponse>,
    isPurchasing: Boolean,
    errorMessage: String?,
    isGuestMode: Boolean,
    onSubscribe: () -> Unit,
    onRestore: () -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit,
) {
    val monthlyPlan = plans.firstOrNull { it.intervalDays in 28..31 }
    val yearlyPlan = plans.firstOrNull { it.intervalDays >= 300 }
    var selectedYearly by remember(plans) { mutableStateOf(yearlyPlan != null) }
    val ink = CalSnapColors.Ink

    val savePct = if (monthlyPlan != null && yearlyPlan != null) {
        val expected = monthlyPlan.priceIdr * 12.0
        val saved = expected - yearlyPlan.priceIdr
        if (saved > 0) ((saved / expected) * 100).roundToInt() else null
    } else null

    val trialDays = (yearlyPlan ?: monthlyPlan)?.trialDays ?: 7
    val priceFooter = yearlyPlan?.let { "${trialDays}-day free trial, then ${formatIdrCompact(it.priceIdr)}/year. Cancel anytime." }
        ?: monthlyPlan?.let { "${trialDays}-day free trial, then ${formatIdrCompact(it.priceIdr)}/month. Cancel anytime." }
        ?: "Cancel anytime."

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ink),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Hero header with radial gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            ) {
                // Base ink
                Box(modifier = Modifier.fillMaxSize().background(ink))
                // Red radial blob (top-left)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(CalSnapColors.RedDark, Color.Transparent),
                                center = Offset(0.3f * 1000f, 0.2f * 1000f),
                                radius = 480f,
                            ),
                        ),
                )
                // Amber radial blob (bottom-right)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Amber.copy(alpha = 0.7f), Color.Transparent),
                                center = Offset(0.85f * 1000f, 0.7f * 280f),
                                radius = 420f,
                            ),
                        ),
                )

                // Close button
                Box(
                    modifier = Modifier
                        .padding(top = 60.dp, end = 20.dp)
                        .align(Alignment.TopEnd)
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onBack,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    CalSnapIcon(name = "close", size = 18.dp, color = Color.White, strokeWidth = 2.2f)
                }

                // CalSnap Pro pill
                Row(
                    modifier = Modifier
                        .padding(top = 80.dp)
                        .align(Alignment.TopCenter)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.White.copy(alpha = 0.12f))
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CalSnapIcon(name = "sparkle", size = 16.dp, color = Color.White, strokeWidth = 2.4f)
                    Text(
                        text = "CALSNAP PRO",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.W700,
                        letterSpacing = 0.4.sp,
                    )
                }

                // Big headline (bottom-left)
                Text(
                    text = buildAnnotatedString {
                        append("Unlimited\nAI scans &\n")
                        withStyle(SpanStyle(color = Amber)) { append("insights.") }
                    },
                    fontSize = 36.sp,
                    fontWeight = FontWeight.W700,
                    color = Color.White,
                    letterSpacing = (-1.2).sp,
                    lineHeight = 38.sp,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 28.dp, end = 28.dp, bottom = 30.dp),
                )
            }

            // Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp)
                    .padding(top = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Features
                FEATURES.forEach { (icon, title, subtitle) ->
                    FeatureRow(icon = icon, title = title, subtitle = subtitle)
                }

                Spacer(Modifier.height(8.dp))

                // Plans
                if (monthlyPlan != null || yearlyPlan != null) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        if (monthlyPlan != null) {
                            PlanCard(
                                title = "Monthly",
                                price = formatIdrCompact(monthlyPlan.priceIdr),
                                sub = "/ month",
                                selected = !selectedYearly,
                                save = null,
                                onClick = { selectedYearly = false },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        if (yearlyPlan != null) {
                            PlanCard(
                                title = "Yearly",
                                price = formatIdrCompact(yearlyPlan.priceIdr),
                                sub = "/ year",
                                selected = selectedYearly,
                                save = savePct?.let { "Save $it%" },
                                onClick = { selectedYearly = true },
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }

                    Text(
                        text = priceFooter,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    )
                }

                errorMessage?.let {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.08f))
                            .padding(12.dp),
                    ) {
                        Text(it, fontSize = 13.sp, color = Color.White.copy(alpha = 0.9f))
                    }
                }
            }

            // CTA
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 12.dp, bottom = 36.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            enabled = !isPurchasing,
                            onClick = if (isGuestMode) onRegister else onSubscribe,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    if (isPurchasing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = ink,
                        )
                    } else {
                        Text(
                            text = if (isGuestMode) "Create account → " else "Start free trial →",
                            color = ink,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.W700,
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    PaywallTextLink("Restore purchases", onRestore)
                    Dot()
                    PaywallTextLink("Terms", {})
                    Dot()
                    PaywallTextLink("Privacy", {})
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: String, title: String, subtitle: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Amber.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center,
        ) {
            CalSnapIcon(name = icon, size = 18.dp, color = Amber, strokeWidth = 2.2f)
        }
        Column(modifier = Modifier.weight(1f).padding(top = 2.dp)) {
            Text(
                text = title,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.W700,
                letterSpacing = (-0.2).sp,
            )
            Text(
                text = subtitle,
                color = Color.White.copy(alpha = 0.55f),
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
    }
}

@Composable
private fun PlanCard(
    title: String,
    price: String,
    sub: String,
    selected: Boolean,
    save: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(if (selected) Color.White else Color.White.copy(alpha = 0.06f))
                .border(
                    width = if (selected) 0.dp else 1.dp,
                    color = if (selected) Color.Transparent else Color.White.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(18.dp),
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onClick,
                )
                .padding(16.dp),
        ) {
            Text(
                text = title,
                color = if (selected) CalSnapColors.Ink.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                fontWeight = FontWeight.W600,
            )
            Text(
                text = price,
                color = if (selected) CalSnapColors.Ink else Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.W700,
                letterSpacing = (-0.6).sp,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                text = sub,
                color = if (selected) CalSnapColors.Ink.copy(alpha = 0.6f) else Color.White.copy(alpha = 0.6f),
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 1.dp),
            )
        }
        if (save != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-10).dp, y = (-10).dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(CalSnapColors.Red)
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = save,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.W700,
                    letterSpacing = 0.4.sp,
                )
            }
        }
    }
}

@Composable
private fun PaywallTextLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 12.sp,
        modifier = Modifier
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 6.dp, vertical = 4.dp),
    )
}

@Composable
private fun Dot() {
    Text(
        text = "·",
        color = Color.White.copy(alpha = 0.45f),
        fontSize = 12.sp,
        modifier = Modifier.padding(vertical = 4.dp),
    )
}

/**
 * Compact IDR formatting: 99000 → "IDR 99K", 599000 → "IDR 599K", 1500000 → "IDR 1.5M".
 * Falls back to thousands grouping (e.g. "IDR 99,000") for values <1000.
 */
internal fun formatIdrCompact(amount: Long): String = when {
    amount >= 1_000_000 -> {
        val whole = amount / 1_000_000
        val frac = (amount % 1_000_000) / 100_000
        if (frac == 0L) "IDR ${whole}M" else "IDR $whole.${frac}M"
    }
    amount >= 1_000 -> "IDR ${amount / 1_000}K"
    else -> "IDR $amount"
}
