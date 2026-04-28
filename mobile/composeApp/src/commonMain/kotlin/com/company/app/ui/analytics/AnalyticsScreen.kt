package com.company.app.ui.analytics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.shared.data.model.DailyRangeSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel,
    onUpgrade: () -> Unit,
    onBack: () -> Unit
) {
    val state = viewModel.state

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analitik") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } },
                actions = { IconButton(onClick = viewModel::refresh) { Text("↺") } }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (!state.isPremium) {
            PremiumGate(onUpgrade = onUpgrade)
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("7 Hari Terakhir", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }

            item {
                WeeklyCalorieChart(data = state.weeklyData)
            }

            item {
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Rata-rata Harian", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                            AvgMacroItem("Kalori", state.avgCalories.toInt().toString(), "kcal",
                                MaterialTheme.colorScheme.primary)
                            AvgMacroItem("Protein", state.avgProtein.toInt().toString(), "g",
                                Color(0xFF4CAF50))
                            AvgMacroItem("Karbo", state.avgCarbs.toInt().toString(), "g",
                                Color(0xFFFF9800))
                            AvgMacroItem("Lemak", state.avgFat.toInt().toString(), "g",
                                Color(0xFFF44336))
                        }
                    }
                }
            }

            if (state.error != null) {
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(state.error, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        }
    }
}

@Composable
private fun WeeklyCalorieChart(data: List<DailyRangeSummary>) {
    if (data.isEmpty()) {
        Card(Modifier.fillMaxWidth()) {
            Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                Text("Belum ada data log makanan", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        return
    }

    val barColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val maxCalories = data.maxOf { it.totalCalories }.coerceAtLeast(100.0)

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text("Kalori per Hari", style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Canvas(
                modifier = Modifier.fillMaxWidth().height(160.dp)
            ) {
                val chartWidth = size.width
                val chartHeight = size.height - 24.dp.toPx()
                val barWidth = (chartWidth / data.size) * 0.6f
                val gap = (chartWidth / data.size) * 0.4f

                // Horizontal grid lines
                for (i in 0..3) {
                    val y = chartHeight * (1f - i / 3f)
                    drawLine(gridColor, Offset(0f, y), Offset(chartWidth, y), strokeWidth = 1f)
                }

                data.forEachIndexed { idx, day ->
                    val barHeight = (day.totalCalories / maxCalories * chartHeight).toFloat()
                    val x = idx * (chartWidth / data.size) + gap / 2
                    val y = chartHeight - barHeight
                    drawRoundRect(
                        color = barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx())
                    )
                }
            }

            // Day labels
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                data.forEach { day ->
                    val label = day.date.takeLast(5)  // MM-DD
                    Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun AvgMacroItem(label: String, value: String, unit: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = color)
        Text(unit, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun PremiumGate(onUpgrade: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.padding(32.dp)) {
            Text("★", fontSize = 48.sp)
            Text("Fitur Premium", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Analitik mingguan dan grafik kalori tersedia untuk pengguna Premium.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = onUpgrade, modifier = Modifier.fillMaxWidth()) {
                Text("Upgrade ke Premium")
            }
        }
    }
}
