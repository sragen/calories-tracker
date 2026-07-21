package com.company.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Single source of truth for every colour in the app — "Dark Athletic".
 *
 * Nothing outside this file may declare a literal colour — no `Color(0x…)`,
 * no `Color.White` / `Color.Black`. Swapping the values here is what flips the
 * whole app between themes.
 *
 * Depth comes from *layering lighter surfaces*, never from drop shadows:
 * Background → Surface → Card, plus an accent-bordered "lift" for emphasis.
 * Shadows are invisible against a dark background, so they are disabled here.
 */
object CalSnapColors {
    // Surfaces — each step up the ladder is one level "closer" to the viewer
    val Background  = Color(0xFF0A0D11)   // E0
    val Surface     = Color(0xFF10151B)   // E1
    val SurfaceAlt  = Color(0xFF161D25)
    val Card        = Color(0xFF1B242E)   // E2
    val Border      = Color(0xFF2B3743)
    val Divider     = Color(0xFF202932)

    // Ink — contrast ratios measured against Background
    val Ink         = Color(0xFFF2F6FA)   // 17.6:1  AAA
    val Ink2        = Color(0xFFBFC8D2)   // 11.2:1  AAA
    val Muted       = Color(0xFF838E9B)   //  5.4:1  AA
    // Nudged up from the design's #5A6470: that value only clears 3:1 against
    // Background, and drops to 2.6:1 on Card — too dim for the deselected
    // labels it is used for. #6A7583 clears 3:1 on every surface.
    val Mute2       = Color(0xFF6A7583)   //  4.2:1 on Background, 3.4:1 on Card
    val Hint        = Color(0xFF3B444E)   // decorative only

    // Brand accent — volt lime. Reserve for actions, highlights and progress;
    // never for long-form text or large fills.
    val Accent      = Color(0xFFC8F45D)
    val AccentDim   = Color(0xFF96C22F)
    val AccentSoft  = Color(0x21C8F45D)   // 13% — tinted chip backgrounds
    // Content drawn ON TOP of a solid accent/semantic fill. Near-black, because
    // the accent is light: white here would fall to ~1.5:1 and be unreadable.
    val OnAccent    = Color(0xFF07090C)

    // Macro accents (+ 14% tint used for chip/card backgrounds)
    val Carb        = Color(0xFFFFB84D)
    val CarbBg      = Color(0x24FFB84D)
    val Protein     = Color(0xFF58A6FF)
    val ProteinBg   = Color(0x2458A6FF)
    val Fat         = Color(0xFFC08BFF)
    val FatBg       = Color(0x24C08BFF)

    // Semantic
    val Good        = Color(0xFF35D6A4)
    val GoodBg      = Color(0x2435D6A4)
    val Warn        = Color(0xFFFFC24B)
    val Bad         = Color(0xFFFF5F6B)   // errors, destructive actions
    val BadSoft     = Color(0x24FF5F6B)

    // Always-dark surfaces — camera viewfinder, analyzing overlay, photo
    // fallbacks. These must NOT invert with the theme or the camera UI breaks.
    val CameraBg         = Color(0xFF07090C)
    val AnalyzingPanel   = Color(0xFF0E141A)
    val PhotoPlaceholder = Color(0xFF18212A)
    val OnDark           = Color(0xFFFFFFFF)  // content over camera / photos
    val Scrim            = Color(0xFF000000)  // darkening overlay on photos

    // Elevation. Shadows do not read against a dark background, so they are
    // switched off — depth comes from the surface ladder above instead.
    val ShadowAmbient = Color.Transparent
    val ShadowSpot    = Color.Transparent

    // Third-party brand colour — fixed by Google, never themed.
    val GoogleBlue  = Color(0xFF4285F4)

    // Translucent bottom-sheet container used on iOS.
    val SheetIos    = Color(0xE610151B)
}
