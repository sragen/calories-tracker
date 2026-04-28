package com.company.app.ui.welcome

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Black = Color(0xFF0A0A0A)

@Composable
fun WelcomeScreen(
    guestScansRemaining: Int,
    onTryFree: () -> Unit,
    onLogin: () -> Unit,
    onRestore: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(56.dp))

            // App name
            Text(
                text = "CalSnap",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Black.copy(alpha = 0.5f),
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(28.dp))

            // Headline
            Text(
                text = "We want you to\ntry CalSnap for free.",
                fontSize = 30.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Black,
                textAlign = TextAlign.Center,
                lineHeight = 36.sp
            )

            Spacer(Modifier.height(32.dp))

            // Phone mockup with scan UI inside
            PhoneMockup()

            Spacer(Modifier.height(28.dp))

            // "No Credit Card" badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("✓", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Black)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "No Credit Card Required",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Black
                )
            }

            Spacer(Modifier.height(28.dp))

            // Primary CTA
            Button(
                onClick = onTryFree,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Black,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Try $guestScansRemaining Free AI Scans",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(14.dp))

            // Login link
            TextButton(onClick = onLogin) {
                Text(
                    text = "Already have an account? Login",
                    color = Black.copy(alpha = 0.55f),
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.weight(1f))

            // Footer
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Just Rp 499.000 / year (Rp 41.583/mo)",
                    fontSize = 13.sp,
                    color = Black.copy(alpha = 0.4f)
                )
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    listOf("Terms", "Privacy", "Restore").forEach { label ->
                        TextButton(
                            onClick = { if (label == "Restore") onRestore() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text(label, fontSize = 12.sp, color = Black.copy(alpha = 0.4f))
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PhoneMockup() {
    // Outer phone shell
    Box(
        modifier = Modifier
            .width(200.dp)
            .height(260.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(Color(0xFF1A1A1A))
            .border(3.dp, Color(0xFF333333), RoundedCornerShape(28.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Screen content (camera viewfinder simulation)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF2C2C2E)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Scan frame corners
                ScanFrame()

                Spacer(Modifier.height(12.dp))

                // Food emoji placeholder
                Text("🍽️", fontSize = 36.sp)
            }

            // Top notch
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
                    .width(40.dp)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFF1A1A1A))
            )
        }
    }
}

@Composable
private fun ScanFrame() {
    Canvas(modifier = Modifier.size(100.dp)) {
        val corner = 22.dp.toPx()
        val stroke = 2.5.dp.toPx()
        val w = size.width
        val h = size.height

        // Top-left
        drawLine(Color.White, Offset(0f, 0f), Offset(corner, 0f), stroke)
        drawLine(Color.White, Offset(0f, 0f), Offset(0f, corner), stroke)
        // Top-right
        drawLine(Color.White, Offset(w, 0f), Offset(w - corner, 0f), stroke)
        drawLine(Color.White, Offset(w, 0f), Offset(w, corner), stroke)
        // Bottom-left
        drawLine(Color.White, Offset(0f, h), Offset(corner, h), stroke)
        drawLine(Color.White, Offset(0f, h), Offset(0f, h - corner), stroke)
        // Bottom-right
        drawLine(Color.White, Offset(w, h), Offset(w - corner, h), stroke)
        drawLine(Color.White, Offset(w, h), Offset(w, h - corner), stroke)
    }
}
