package com.company.app.ui.subscription

import androidx.compose.runtime.Composable

@Composable
expect fun MidtransPaymentScreen(
    snapToken: String,
    onFinished: () -> Unit,
    onError: () -> Unit
)
