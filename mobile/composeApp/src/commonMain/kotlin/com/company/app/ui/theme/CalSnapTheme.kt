package com.company.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val CalSnapColorScheme = lightColorScheme(
    primary            = CalSnapColors.Ink,
    onPrimary          = CalSnapColors.Background,
    primaryContainer   = CalSnapColors.SurfaceAlt,
    onPrimaryContainer = CalSnapColors.Ink,

    secondary          = CalSnapColors.Red,
    onSecondary        = CalSnapColors.Background,
    secondaryContainer = CalSnapColors.RedSoft,
    onSecondaryContainer = CalSnapColors.RedDark,

    background         = CalSnapColors.Background,
    onBackground       = CalSnapColors.Ink,

    surface            = CalSnapColors.Surface,
    onSurface          = CalSnapColors.Ink,
    surfaceVariant     = CalSnapColors.SurfaceAlt,
    onSurfaceVariant   = CalSnapColors.Muted,

    outline            = CalSnapColors.Border,
    outlineVariant     = CalSnapColors.Divider,

    error              = CalSnapColors.Red,
    onError            = CalSnapColors.Background,
)

@Composable
fun CalSnapTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CalSnapColorScheme,
        content = content,
    )
}
