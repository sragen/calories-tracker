package com.company.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.company.app.shared.billing.ActivityProvider
import com.company.app.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }

    override fun onResume() {
        super.onResume()
        ActivityProvider.set(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) ActivityProvider.set(this)
    }
}
