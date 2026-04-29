package com.company.app.ui.aiscan

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
actual fun AiScanScreen(
    onPhotoCaptured: (ByteArray) -> Unit,
    onBack: () -> Unit,
) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var capturing by remember { mutableStateOf(false) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (cameraPermission.status.isGranted) {
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val imageCapture = ImageCapture.Builder()
                            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                            .build()
                        imageCaptureRef = imageCapture
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageCapture,
                            )
                        } catch (_: Exception) {}
                    }, ctx.mainExecutor)
                    previewView
                },
                modifier = Modifier.fillMaxSize(),
            )

            ScanOverlay(
                capturing = capturing,
                onBack = onBack,
                onCapture = {
                    val capture = imageCaptureRef ?: return@ScanOverlay
                    capturing = true
                    val executor = Executors.newSingleThreadExecutor()
                    capture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            scope.launch {
                                val bytes = imageProxyToBytes(image)
                                image.close()
                                executor.shutdown()
                                capturing = false
                                onPhotoCaptured(bytes)
                            }
                        }
                        override fun onError(exception: ImageCaptureException) {
                            executor.shutdown()
                            scope.launch { capturing = false }
                        }
                    })
                },
            )
        } else {
            PermissionDeniedContent(onRequest = { cameraPermission.launchPermissionRequest() })
        }
    }
}

@Composable
private fun ScanOverlay(
    capturing: Boolean,
    onBack: () -> Unit,
    onCapture: () -> Unit,
) {
    // A3 scan line animation — sweeps top→bottom, 2s loop
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

    Box(modifier = Modifier.fillMaxSize()) {
        // Top scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.15f)
                .background(Color.Black.copy(alpha = 0.55f)),
        )

        // Bottom scrim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.55f)),
        )

        // Back / close button
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

        // Scan frame
        BoxWithConstraints(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 40.dp)
                .aspectRatio(1f),
        ) {
            val frameSize = maxWidth
            Box(modifier = Modifier.fillMaxSize()) {
                ScanFrameCorners()

                // Animated scan line inside frame
                if (!capturing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                            .offset(y = (frameSize * scanLineY))
                            .background(
                                androidx.compose.ui.graphics.Brush.horizontalGradient(
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
        }

        // Hint label
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
                    text = if (capturing) "Analyzing…" else "Point at your food",
                    color = Color.White,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                )
            }
        }

        // Shutter button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp),
            contentAlignment = Alignment.Center,
        ) {
            if (capturing) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(56.dp),
                    strokeWidth = 3.dp,
                )
            } else {
                ShutterButton(onClick = onCapture)
            }
        }
    }
}

@Composable
private fun ShutterButton(onClick: () -> Unit) {
    // Outer ring
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.25f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
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

@Composable
private fun ScanFrameCorners() {
    val cornerColor = Color.White
    val cornerLen = 28.dp
    val strokeW = 3.dp

    Box(modifier = Modifier.fillMaxSize()) {
        // Top-left
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            Box(Modifier.width(cornerLen).height(strokeW).background(cornerColor))
            Box(Modifier.width(strokeW).height(cornerLen).background(cornerColor))
        }
        // Top-right
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            Box(Modifier.width(cornerLen).height(strokeW).align(Alignment.TopEnd).background(cornerColor))
            Box(Modifier.width(strokeW).height(cornerLen).align(Alignment.TopEnd).background(cornerColor))
        }
        // Bottom-left
        Box(modifier = Modifier.align(Alignment.BottomStart)) {
            Box(Modifier.width(cornerLen).height(strokeW).align(Alignment.BottomStart).background(cornerColor))
            Box(Modifier.width(strokeW).height(cornerLen).align(Alignment.BottomStart).background(cornerColor))
        }
        // Bottom-right
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            Box(Modifier.width(cornerLen).height(strokeW).align(Alignment.BottomEnd).background(cornerColor))
            Box(Modifier.width(strokeW).height(cornerLen).align(Alignment.BottomEnd).background(cornerColor))
        }
    }
}

@Composable
private fun PermissionDeniedContent(onRequest: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp),
        ) {
            Text("📷", fontSize = 48.sp)
            Text(
                "Camera access needed to scan food",
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(Color.White)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onRequest,
                    )
                    .padding(horizontal = 24.dp, vertical = 12.dp),
            ) {
                Text("Allow Camera", color = Color.Black, fontWeight = FontWeight.W600)
            }
        }
    }
}

private suspend fun imageProxyToBytes(image: ImageProxy): ByteArray = withContext(Dispatchers.IO) {
    val buffer = image.planes[0].buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)
    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
    val out = ByteArrayOutputStream()
    (bitmap ?: return@withContext data).compress(Bitmap.CompressFormat.JPEG, 80, out)
    out.toByteArray()
}
