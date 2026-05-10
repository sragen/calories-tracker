package com.company.app.ui.platform

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.company.app.ui.theme.CalSnapColors

/**
 * Edge-to-edge screen wrapper.
 *
 * Background ([containerColor]) extends behind the system status bar and home indicator.
 * Foreground [content] is padded to clear the status bar at the top.
 * [bottomBar] is padded to sit above the home indicator / nav bar.
 *
 * The keyboard (IME inset) is intentionally not consumed here so input fields scroll/resize
 * naturally; opt in per-field via Modifier.imePadding() where needed.
 */
@Composable
fun EdgeToEdgeScreen(
    bottomBar: @Composable (() -> Unit)? = null,
    containerColor: Color = CalSnapColors.Background,
    content: @Composable () -> Unit,
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        containerColor = containerColor,
        bottomBar = {
            if (bottomBar != null) {
                Box(Modifier.windowInsetsPadding(WindowInsets.navigationBars)) {
                    bottomBar()
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .windowInsetsPadding(WindowInsets.statusBars),
        ) {
            content()
        }
    }
}
