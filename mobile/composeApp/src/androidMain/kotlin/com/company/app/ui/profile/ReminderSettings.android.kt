package com.company.app.ui.profile

import android.Manifest
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.company.app.notification.MealReminderScheduler
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun ReminderSettingsCard() {
    val context = LocalContext.current
    var enabled by remember { mutableStateOf(MealReminderScheduler.isScheduled(context)) }
    val (savedH, savedM) = remember { MealReminderScheduler.getSavedTime(context) }
    var hour by remember { mutableIntStateOf(savedH) }
    var minute by remember { mutableIntStateOf(savedM) }

    val notifPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    } else null

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pengingat Makan", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Switch(
                    checked = enabled,
                    onCheckedChange = { on ->
                        if (on) {
                            if (notifPermission?.status?.isGranted == false) {
                                notifPermission.launchPermissionRequest()
                            }
                            MealReminderScheduler.schedule(context, hour, minute)
                        } else {
                            MealReminderScheduler.cancel(context)
                        }
                        enabled = on
                    }
                )
            }

            if (enabled) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Waktu:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = "%02d".format(hour),
                        onValueChange = { v -> v.toIntOrNull()?.coerceIn(0, 23)?.let { hour = it } },
                        label = { Text("Jam") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Text(":", style = MaterialTheme.typography.bodyLarge)
                    OutlinedTextField(
                        value = "%02d".format(minute),
                        onValueChange = { v -> v.toIntOrNull()?.coerceIn(0, 59)?.let { minute = it } },
                        label = { Text("Menit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    TextButton(onClick = {
                        MealReminderScheduler.schedule(context, hour, minute)
                    }) { Text("Simpan") }
                }
            }
        }
    }
}
