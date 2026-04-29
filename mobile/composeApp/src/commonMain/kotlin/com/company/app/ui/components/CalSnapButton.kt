package com.company.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.company.app.ui.theme.CalSnapColors
import com.company.app.ui.theme.CalSnapRadius
import com.company.app.ui.theme.CalSnapType

/**
 * Primary button — Ink background, white text, pill shape.
 * Full-width by default.
 */
@Composable
fun CalSnapPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(CalSnapRadius.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = CalSnapColors.Ink,
            contentColor = CalSnapColors.Background,
            disabledContainerColor = CalSnapColors.Hint,
            disabledContentColor = CalSnapColors.Mute2,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
    ) {
        Text(text = text, style = CalSnapType.ButtonLarge, color = Color.Unspecified)
    }
}

/**
 * Brand button — Red background, white text, pill shape, red glow shadow.
 * Used for high-emphasis CTAs (Plan Reveal, Paywall).
 */
@Composable
fun CalSnapBrandButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(CalSnapRadius.pill),
        colors = ButtonDefaults.buttonColors(
            containerColor = CalSnapColors.Red,
            contentColor = CalSnapColors.Background,
            disabledContainerColor = CalSnapColors.Hint,
            disabledContentColor = CalSnapColors.Mute2,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(58.dp),
    ) {
        Text(text = text, style = CalSnapType.ButtonLarge, color = Color.Unspecified)
    }
}

/**
 * Text/ghost button — transparent background, Ink text.
 * Used for secondary actions ("Back", "Maybe later").
 */
@Composable
fun CalSnapTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = CalSnapColors.Muted,
) {
    androidx.compose.material3.TextButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp),
    ) {
        Text(text = text, style = CalSnapType.Body, color = color)
    }
}
