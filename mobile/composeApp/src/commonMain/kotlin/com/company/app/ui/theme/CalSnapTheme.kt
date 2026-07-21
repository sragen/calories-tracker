package com.company.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// darkColorScheme (not light) — Material uses this to pick defaults for system
// components: sheet scrims, text-field containers, ripple and elevation tints.
private val CalSnapColorScheme = darkColorScheme(
    primary            = CalSnapColors.Ink,
    onPrimary          = CalSnapColors.Background,
    primaryContainer   = CalSnapColors.SurfaceAlt,
    onPrimaryContainer = CalSnapColors.Ink,

    secondary          = CalSnapColors.Accent,
    onSecondary        = CalSnapColors.OnAccent,
    secondaryContainer = CalSnapColors.AccentSoft,
    onSecondaryContainer = CalSnapColors.Ink,

    background         = CalSnapColors.Background,
    onBackground       = CalSnapColors.Ink,

    surface            = CalSnapColors.Surface,
    onSurface          = CalSnapColors.Ink,
    surfaceVariant     = CalSnapColors.SurfaceAlt,
    onSurfaceVariant   = CalSnapColors.Muted,

    outline            = CalSnapColors.Border,
    outlineVariant     = CalSnapColors.Divider,

    error              = CalSnapColors.Bad,
    onError            = CalSnapColors.OnAccent,
)

@Composable
fun CalSnapTheme(content: @Composable () -> Unit) {
    CompositionLocalProvider(LocalNumeralFont provides spaceGroteskFamily()) {
        MaterialTheme(
            colorScheme = CalSnapColorScheme,
            content = content,
        )
    }
}
