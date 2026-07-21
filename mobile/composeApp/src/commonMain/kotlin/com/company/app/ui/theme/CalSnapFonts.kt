package com.company.app.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import calsnap.composeapp.generated.resources.Res
import calsnap.composeapp.generated.resources.space_grotesk_bold
import calsnap.composeapp.generated.resources.space_grotesk_medium
import org.jetbrains.compose.resources.Font

/**
 * Space Grotesk — used for numerals and display text only. Its tight, squared
 * figures are what give the dark theme its athletic character. Body copy stays
 * on the platform font (SF on iOS, Roboto on Android).
 *
 * Licensed under the SIL Open Font License; see
 * `composeResources/font/OFL-SpaceGrotesk.txt`.
 */
@Composable
fun spaceGroteskFamily(): FontFamily = FontFamily(
    Font(Res.font.space_grotesk_medium, FontWeight.W500),
    Font(Res.font.space_grotesk_bold, FontWeight.W700),
)

/**
 * The numeral typeface for the current theme. Falls back to the platform
 * default so previews and any composable outside [CalSnapTheme] still render.
 */
val LocalNumeralFont = staticCompositionLocalOf<FontFamily> { FontFamily.Default }

/** Shorthand for `LocalNumeralFont.current`. */
val numeralFont: FontFamily
    @Composable @ReadOnlyComposable get() = LocalNumeralFont.current
