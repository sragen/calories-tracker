package com.company.app.ui.subscription

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MidtransWebViewScreen(
    snapToken: String,
    onFinished: () -> Unit,
    onError: () -> Unit
) {
    // Midtrans Snap URL with the token
    val snapUrl = "https://app.sandbox.midtrans.com/snap/v2/vtweb/$snapToken"

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                        when {
                            url?.contains("payment/finish") == true ||
                            url?.startsWith("caloriestracker://payment/finish") == true -> onFinished()
                            url?.contains("payment/error") == true ||
                            url?.startsWith("caloriestracker://payment/error") == true -> onError()
                        }
                        return false
                    }
                }
                loadUrl(snapUrl)
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
