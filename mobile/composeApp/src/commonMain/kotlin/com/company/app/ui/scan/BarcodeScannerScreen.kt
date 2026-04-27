package com.company.app.ui.scan

import androidx.compose.runtime.Composable

@Composable
expect fun BarcodeScannerScreen(
    onBarcodeDetected: (String) -> Unit,
    onBack: () -> Unit
)
