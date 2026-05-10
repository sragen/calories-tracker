package com.company.app

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.company.app.shared.billing.ActivityProvider
import com.company.app.ui.App

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Draw under status + nav bars; transparent so app content shows behind them.
        // SystemBarStyle.light() = dark icons on the bar (counter-intuitive Android naming).
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
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
