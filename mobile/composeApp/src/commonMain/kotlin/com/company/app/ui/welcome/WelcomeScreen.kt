package com.company.app.ui.welcome

import androidx.compose.foundation.background
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
    Box(modifier = Modifier.fillMaxSize().background(CalSnapColors.Background)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── Hero food photo (460dp) ──────────────────────────────────────
            Box(modifier = Modifier.fillMaxWidth().height(460.dp)) {
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
                            0.00f to Color(0x59000000),  // rgba(0,0,0,0.35)
                            0.25f to Color(0x00000000),
                            0.75f to Color(0x00000000),
                            1.00f to Color(0x66FFFFFF),  // rgba(255,255,255,0.4)
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
                                .background(Color.White),
                            contentAlignment = Alignment.Center,
                        ) {
                            CalSnapIcon(name = "camera", size = 17.dp, color = CalSnapColors.Red, strokeWidth = 2.2f)
                        }
                        Text(
                            text = "CalSnap",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = (-0.5).sp,
                            color = Color.White,
                        )
                    }
                }
            }

            // ── Copy block ───────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 28.dp, end = 28.dp, top = 32.dp),
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("Snap your food.\n")
                        withStyle(SpanStyle(color = CalSnapColors.Red)) {
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
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
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
