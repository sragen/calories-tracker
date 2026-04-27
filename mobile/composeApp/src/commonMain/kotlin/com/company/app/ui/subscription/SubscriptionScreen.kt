package com.company.app.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.SubscriptionPlan

@Composable
fun SubscriptionScreen(
    viewModel: SubscriptionViewModel,
    onSnapToken: (String) -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel.state

    LaunchedEffect(state.snapToken) {
        state.snapToken?.let { onSnapToken(it.snapToken) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Premium") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Text("←") }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active subscription status
            if (state.status?.isPremium == true) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("✓ Premium Aktif", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            state.status.subscription?.expiresAt?.let { expiry ->
                                Text("Berlaku hingga: ${expiry.take(10)}", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            // Feature highlights
            item {
                Card {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Fitur Premium", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        FeatureRow("📸", "AI Scan Makanan — foto langsung catat kalori")
                        FeatureRow("📊", "Analitik Lanjutan — tren mingguan & bulanan")
                        FeatureRow("📤", "Export Data — CSV & PDF")
                        FeatureRow("∞", "Log Tanpa Batas — tidak ada limit harian")
                    }
                }
            }

            // Plans
            item {
                Text("Pilih Paket", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            items(state.plans) { plan ->
                PlanCard(
                    plan = plan,
                    isLoading = state.isPurchasing,
                    onSelect = { viewModel.purchase(plan.id) }
                )
            }

            if (state.error != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            state.error,
                            Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureRow(icon: String, text: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(icon, fontSize = 18.sp)
        Text(text, fontSize = 14.sp)
    }
}

@Composable
private fun PlanCard(plan: SubscriptionPlan, isLoading: Boolean, onSelect: () -> Unit) {
    val priceFormatted = "Rp ${plan.priceIdr / 1000}.000"
    val isYearly = plan.durationDays >= 365

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = if (isYearly)
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        else CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(plan.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    plan.description?.let { Text(it, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                if (isYearly) {
                    Surface(
                        color = MaterialTheme.colorScheme.primary,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text("HEMAT 57%", Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = MaterialTheme.colorScheme.onPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(priceFormatted, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp)
                Text(
                    if (isYearly) "/tahun" else "/bulan",
                    Modifier.padding(start = 4.dp, bottom = 3.dp),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (isYearly) {
                Text("≈ Rp 12.400/bulan", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = onSelect,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Pilih ${plan.name}")
                }
            }
        }
    }
}
