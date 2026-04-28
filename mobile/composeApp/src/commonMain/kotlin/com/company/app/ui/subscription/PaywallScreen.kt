package com.company.app.ui.subscription

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PaywallBlack = Color(0xFF0A0A0A)

@Composable
fun PaywallScreen(
    viewModel: SubscriptionViewModel,
    isGuestMode: Boolean = false,
    onEntitled: () -> Unit,
    onRegister: () -> Unit = {},
    onBack: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    when (val s = state) {
        is SubscriptionState.Entitled -> onEntitled()
        else -> PaywallContent(
            isLoading = s is SubscriptionState.Purchasing,
            errorMessage = (s as? SubscriptionState.Error)?.message,
            isGuestMode = isGuestMode,
            onSubscribe = { viewModel.purchase() },
            onRestore = { viewModel.restore() },
            onRegister = onRegister,
            onBack = onBack
        )
    }
}

@Composable
private fun PaywallContent(
    isLoading: Boolean,
    errorMessage: String?,
    isGuestMode: Boolean,
    onSubscribe: () -> Unit,
    onRestore: () -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(52.dp))

            // Header
            Text(
                text = if (isGuestMode) "Your free scans are up." else "Go Premium",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = PaywallBlack,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Unlock unlimited AI scans,\nmacro tracking, and more.",
                fontSize = 16.sp,
                color = PaywallBlack.copy(alpha = 0.55f),
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(Modifier.height(32.dp))

            // Price card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFFF5F5F5))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "CalSnap Premium",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = PaywallBlack
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Rp 499.000 / year",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = PaywallBlack
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Rp 41.583 / month  •  7-day free trial",
                        fontSize = 13.sp,
                        color = PaywallBlack.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Feature list
            listOf(
                "Unlimited AI food scans",
                "Full nutrition & macro tracking",
                "Weekly analytics & trends",
                "Priority support"
            ).forEach { feature ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✓", fontWeight = FontWeight.Bold, color = PaywallBlack, fontSize = 14.sp)
                    Spacer(Modifier.width(10.dp))
                    Text(feature, fontSize = 14.sp, color = PaywallBlack.copy(alpha = 0.75f))
                }
            }

            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.weight(1f))

            // Subscribe CTA
            Button(
                onClick = onSubscribe,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PaywallBlack,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                } else {
                    Text("Start 7-Day Free Trial", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Register option (guest) or Restore (existing user)
            if (isGuestMode) {
                TextButton(onClick = onRegister) {
                    Text(
                        "Create free account instead",
                        color = PaywallBlack.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            } else {
                TextButton(onClick = onRestore, enabled = !isLoading) {
                    Text("Restore Purchase", color = PaywallBlack.copy(alpha = 0.5f), fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Back / footer
            TextButton(onClick = onBack) {
                Text(
                    if (isGuestMode) "← Back to trial" else "← Back",
                    color = PaywallBlack.copy(alpha = 0.35f),
                    fontSize = 13.sp
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}
