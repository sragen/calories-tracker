package com.company.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

object CalSnapType {
    // Display — plan reveal "2,140" and scan result kcal
    val Display = TextStyle(
        fontSize = 84.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-3).sp,
        lineHeight = 84.sp,
    )

    // Hero — remaining kcal on Home card
    val Hero = TextStyle(
        fontSize = 64.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-2.5).sp,
    )

    // HeadlineLarge — onboarding screen titles
    val HeadlineLarge = TextStyle(
        fontSize = 30.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-1).sp,
        lineHeight = 33.sp,
    )

    // HeadlineMedium — card titles, section headers
    val HeadlineMedium = TextStyle(
        fontSize = 22.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-0.5).sp,
    )

    // TitleLarge — welcome hero copy
    val TitleLarge = TextStyle(
        fontSize = 34.sp,
        fontWeight = FontWeight.W700,
        letterSpacing = (-1.2).sp,
        lineHeight = 36.sp,
    )

    // BodyLarge — standard content text
    val BodyLarge = TextStyle(
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    )

    // Body — secondary content, descriptions
    val Body = TextStyle(
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = (-0.2).sp,
    )

    // BodySmall — captions, meal timestamps
    val BodySmall = TextStyle(
        fontSize = 13.sp,
        lineHeight = 18.sp,
    )

    // Label — uppercase caps labels ("PROTEIN", "DAILY AVERAGE")
    val Label = TextStyle(
        fontSize = 11.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = 0.6.sp,
    )

    // ButtonLarge — primary CTA buttons
    val ButtonLarge = TextStyle(
        fontSize = 17.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = (-0.2).sp,
    )

    // Macro number — "18g" value in macro bars
    val MacroValue = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.W600,
        letterSpacing = (-0.4).sp,
    )
}
