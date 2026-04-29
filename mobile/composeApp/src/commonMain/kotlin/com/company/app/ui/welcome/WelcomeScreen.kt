package com.company.app.ui.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.components.CalSnapTextButton
import com.company.app.ui.theme.*

@Composable
fun WelcomeScreen(
    guestScansRemaining: Int,
    onTryFree: () -> Unit,
    onLogin: () -> Unit,
    onRestore: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CalSnapColors.Background),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = CalSnapSpacing.screenPad),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(56.dp))

            Text(
                text = "CALSNAP",
                style = CalSnapType.Label.copy(letterSpacing = 2.sp),
                color = CalSnapColors.Muted,
            )

            Spacer(Modifier.height(CalSnapSpacing.xxl))

            FoodIllustration()

            Spacer(Modifier.height(CalSnapSpacing.xl))

            Text(
                text = buildAnnotatedString {
                    append("Snap your food.\n")
                    withStyle(SpanStyle(color = CalSnapColors.Red)) {
                        append("Track your goals.")
                    }
                },
                style = CalSnapType.TitleLarge,
                color = CalSnapColors.Ink,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.height(CalSnapSpacing.sm))

            Text(
                text = "AI-powered nutrition tracking.\nNo calorie counting needed.",
                style = CalSnapType.Body,
                color = CalSnapColors.Muted,
                textAlign = TextAlign.Center,
            )

            Spacer(Modifier.weight(1f))

            CalSnapPrimaryButton(
                text = "Get Started Free",
                onClick = onTryFree,
            )

            Spacer(Modifier.height(CalSnapSpacing.xs))

            CalSnapTextButton(
                text = "Already have an account? Sign In",
                onClick = onLogin,
            )

            Spacer(Modifier.height(CalSnapSpacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                listOf("Terms", "Privacy", "Restore").forEach { label ->
                    TextButton(
                        onClick = { if (label == "Restore") onRestore() },
                        contentPadding = PaddingValues(0.dp),
                    ) {
                        Text(
                            text = label,
                            style = CalSnapType.BodySmall,
                            color = CalSnapColors.Hint,
                        )
                    }
                }
            }

            Spacer(Modifier.height(CalSnapSpacing.lg))
        }
    }
}

@Composable
private fun FoodIllustration() {
    Box(
        modifier = Modifier.size(190.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(190.dp)
                .clip(CircleShape)
                .background(CalSnapColors.SurfaceAlt),
        )
        Box(
            modifier = Modifier
                .size(148.dp)
                .clip(CircleShape)
                .background(Color.White),
        )
        Canvas(modifier = Modifier.size(118.dp)) {
            val cx = size.width / 2
            val cy = size.height / 2
            drawCircle(Color(0xFF7BC67E), radius = 46f, center = Offset(cx, cy))
            drawCircle(Color(0xFFE63946), radius = 20f, center = Offset(cx - 18f, cy - 10f))
            drawCircle(Color(0xFFF4A23A), radius = 15f, center = Offset(cx + 18f, cy + 12f))
            drawCircle(Color(0xFF5A8DEF), radius = 11f, center = Offset(cx + 10f, cy - 22f))
        }
    }
}
