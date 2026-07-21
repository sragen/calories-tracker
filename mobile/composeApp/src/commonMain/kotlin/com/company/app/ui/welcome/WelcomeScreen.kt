package com.company.app.ui.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import calsnap.composeapp.generated.resources.Res
import calsnap.composeapp.generated.resources.veg_hero
import com.company.app.ui.components.CalSnapIcon
import com.company.app.ui.components.CalSnapPrimaryButton
import com.company.app.ui.theme.*
import org.jetbrains.compose.resources.painterResource
import androidx.compose.foundation.Image

@Composable
fun WelcomeScreen(
    guestScansRemaining: Int,
    onTryFree: () -> Unit,
    onLogin: () -> Unit,
    onRestore: () -> Unit = {},
) {
    Box(modifier = Modifier.fillMaxSize().background(CalSnapColors.Background).statusBarsPadding().navigationBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Hero food photo ──────────────────────────────────────────────
            // Flexible, not a fixed 460dp: the photo absorbs the height
            // difference between devices so the copy below is never clipped.
            Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                Image(
                    painter = painterResource(Res.drawable.veg_hero),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
                // Gradient overlay: dark at top → transparent → light white at bottom
                Box(
                    modifier = Modifier.fillMaxSize().background(
                        Brush.verticalGradient(
                            0.00f to CalSnapColors.Scrim.copy(alpha = 0.35f),  // rgba(0,0,0,0.35)
                            0.25f to Color.Transparent,
                            0.75f to Color.Transparent,
                            1.00f to CalSnapColors.Background.copy(alpha = 0.4f),  // rgba(255,255,255,0.4)
                        )
                    )
                )
                // Logo centered at top
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 70.dp),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(30.dp)
                                .clip(RoundedCornerShape(9.dp))
                                .background(CalSnapColors.Card),
                            contentAlignment = Alignment.Center,
                        ) {
                            CalSnapIcon(name = "camera", size = 17.dp, color = CalSnapColors.Accent, strokeWidth = 2.2f)
                        }
                        Text(
                            text = "CalSnap",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            color = CalSnapColors.OnDark,
                        )
                    }
                }
            }

            // ── Copy block ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .padding(start = 28.dp, end = 28.dp, top = 28.dp),
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Snap your food.\n")
                        withStyle(SpanStyle(color = CalSnapColors.Accent)) {
                            append("Track your goals.")
                        }
                    },
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                    letterSpacing = (-1.2).sp,
                    color = CalSnapColors.Ink,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Point your camera at any meal — get instant calories & macros. No more guessing.",
                    fontSize = 16.sp,
                    color = CalSnapColors.Muted,
                    lineHeight = 23.sp,
                    letterSpacing = (-0.2).sp,
                )
            }

            // ── CTA ─────────────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 40.dp)) {
                CalSnapPrimaryButton(
                    text = "Get started  →",
                    onClick = onTryFree,
                )
                Spacer(Modifier.height(16.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onLogin),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("Already have an account? ")
                            withStyle(SpanStyle(color = CalSnapColors.Ink, fontWeight = FontWeight.SemiBold)) {
                                append("Sign in")
                            }
                        },
                        fontSize = 14.sp,
                        color = CalSnapColors.Muted,
                    )
                }
            }
        }
    }
}
