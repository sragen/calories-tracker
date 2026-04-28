package com.company.app.ui.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.app.shared.data.model.EntitlementResponse

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isLoggedOut) {
        if (state.isLoggedOut) onLogout()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile") })
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        state.profile?.let { profile ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Body Profile", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    ProfileRow("Height", "${profile.heightCm} cm")
                                    ProfileRow("Weight", "${profile.weightKg} kg")
                                    ProfileRow("Gender", profile.gender)
                                    ProfileRow("Activity", profile.activityLevel.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() })
                                    ProfileRow("Goal", profile.goal)
                                    profile.bmrKcal?.let { ProfileRow("BMR", "${it.toInt()} kcal") }
                                    profile.tdeeKcal?.let { ProfileRow("TDEE", "${it.toInt()} kcal") }
                                    profile.recommendedCalories?.let { ProfileRow("Recommended", "${it.toInt()} kcal/day") }
                                }
                            }
                        }

                        state.goal?.let { goal ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("Daily Targets", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                                    ProfileRow("Calories", "${goal.targetCalories.toInt()} kcal")
                                    ProfileRow("Protein", "${goal.targetProteinG.toInt()} g")
                                    ProfileRow("Carbs", "${goal.targetCarbsG.toInt()} g")
                                    ProfileRow("Fat", "${goal.targetFatG.toInt()} g")
                                }
                            }
                        }

                        state.entitlement?.let { entitlement ->
                            SubscriptionCard(entitlement)
                        }

                        ReminderSettingsCard()

                        Spacer(Modifier.weight(1f))

                        Button(
                            onClick = viewModel::logout,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Logout")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun SubscriptionCard(entitlement: EntitlementResponse) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Subscription", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                val (label, color) = if (entitlement.entitled)
                    "PREMIUM" to Color(0xFFFFB300)
                else
                    "FREE" to MaterialTheme.colorScheme.onSurfaceVariant
                Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            }
            if (entitlement.entitled) {
                entitlement.status?.let { ProfileRow("Status", it) }
                entitlement.expiresAt?.let { ProfileRow("Expires", it.take(10)) }
                entitlement.source?.let { ProfileRow("Source", it) }
            } else {
                Text(
                    "Upgrade to Premium untuk fitur AI Scan dan Analytics",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
