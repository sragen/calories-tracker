package com.company.app.ui.subscription

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
actual fun MidtransPaymentScreen(
    snapToken: String,
    onFinished: () -> Unit,
    onError: () -> Unit
) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Pembayaran via browser eksternal")
            Button(onClick = onFinished) { Text("Selesai") }
        }
    }
}
