package com.company.app.ui.subscription

import androidx.compose.runtime.Composable

@Composable
actual fun MidtransPaymentScreen(
    snapToken: String,
    onFinished: () -> Unit,
    onError: () -> Unit
) {
    MidtransWebViewScreen(
        snapToken = snapToken,
        onFinished = onFinished,
        onError = onError
    )
}
