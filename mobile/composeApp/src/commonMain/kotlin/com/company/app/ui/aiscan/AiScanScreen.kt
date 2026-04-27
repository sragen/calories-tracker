package com.company.app.ui.aiscan

import androidx.compose.runtime.Composable

@Composable
expect fun AiScanScreen(
    onPhotoCaptured: (ByteArray) -> Unit,
    onBack: () -> Unit
)
