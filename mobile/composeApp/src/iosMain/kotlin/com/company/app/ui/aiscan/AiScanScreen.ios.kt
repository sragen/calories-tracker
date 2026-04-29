package com.company.app.ui.aiscan

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// iOS camera integration via UIImagePickerController is wired in the Swift layer.
// This Compose screen displays a styled dark overlay while the native picker is active,
// or a gallery-pick fallback button if the picker is unavailable.
@Composable
actual fun AiScanScreen(
    onPhotoCaptured: (ByteArray) -> Unit,
    onBack: () -> Unit,
) {
    val scanLineAnim = rememberInfiniteTransition(label = "scanLine")
    val scanLineY by scanLineAnim.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scanLineY",
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        // Top scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f)
                .background(Color.Black.copy(alpha = 0.55f)),
        )

        // Scan frame placeholder
        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 40.dp)
                .aspectRatio(1f),
        ) {
            val frameSize = maxWidth
            Box(modifier = Modifier.fillMaxSize()) {
                // Corner brackets
                ScanFrameCorners()

                // Animated scan line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.dp)
                        .offset(y = frameSize * scanLineY)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color(0xFFE63946).copy(alpha = 0.8f),
                                    Color(0xFFE63946),
                                    Color(0xFFE63946).copy(alpha = 0.8f),
                                    Color.Transparent,
                                )
                            )
                        ),
                )
            }
        }

        // Close button
        Box(
            modifier = Modifier
                .statusBarsPadding()
                .padding(16.dp)
                .size(40.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onBack,
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("✕", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.W600)
        }

        // Hint
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 260.dp),
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 16.dp, vertical = 6.dp),
            ) {
                Text(
                    text = "Point at your food",
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Bottom controls
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Camera coming to iOS — use gallery for now",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
            // Shutter placeholder — wired to native UIImagePickerController by the Swift host
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
            }
        }
    }
}

@Composable
private fun ScanFrameCorners() {
    val white = Color.White
    val cornerLen = 28.dp
    val strokeW = 3.dp

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            Box(Modifier.width(cornerLen).height(strokeW).background(white))
            Box(Modifier.width(strokeW).height(cornerLen).background(white))
        }
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            Box(Modifier.width(cornerLen).height(strokeW).align(Alignment.TopEnd).background(white))
            Box(Modifier.width(strokeW).height(cornerLen).align(Alignment.TopEnd).background(white))
        }
        Box(modifier = Modifier.align(Alignment.BottomStart)) {
            Box(Modifier.width(cornerLen).height(strokeW).align(Alignment.BottomStart).background(white))
            Box(Modifier.width(strokeW).height(cornerLen).align(Alignment.BottomStart).background(white))
        }
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            Box(Modifier.width(cornerLen).height(strokeW).align(Alignment.BottomEnd).background(white))
            Box(Modifier.width(strokeW).height(cornerLen).align(Alignment.BottomEnd).background(white))
        }
    }
}
