package com.company.app.ui.aiscan

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
    onBack: () -> Unit
) {
    val cameraPermission = rememberPermissionState(android.Manifest.permission.CAMERA)
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    var capturing by remember { mutableStateOf(false) }
    var imageCaptureRef by remember { mutableStateOf<ImageCapture?>(null) }

    LaunchedEffect(Unit) {
        if (!cameraPermission.status.isGranted) cameraPermission.launchPermissionRequest()
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
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
                                lifecycleOwner, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
                            )
                        } catch (e: Exception) { /* ignore */ }
                    }, ctx.mainExecutor)
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )

            // Back button
            IconButton(
                onClick = onBack,
                modifier = Modifier.padding(16.dp).statusBarsPadding()
            ) {
                Text("✕", color = Color.White)
            }

            // Capture button
            Box(
                Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                if (capturing) {
                    CircularProgressIndicator(color = Color.White)
                } else {
                    androidx.compose.material3.Button(
                        onClick = {
                            val capture = imageCaptureRef ?: return@Button
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
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape,
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                            containerColor = Color.White
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {}
                }
            }
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Camera permission required", color = Color.White)
                    Spacer(Modifier.height(8.dp))
                    androidx.compose.material3.Button(onClick = { cameraPermission.launchPermissionRequest() }) {
                        Text("Grant Permission")
                    }
                }
            }
        }
    }
}

private suspend fun imageProxyToBytes(image: ImageProxy): ByteArray = withContext(Dispatchers.IO) {
    val buffer = image.planes[0].buffer
    val data = ByteArray(buffer.remaining())
    buffer.get(data)
    // Compress to JPEG at 80% quality to reduce upload size
    val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)
    val out = ByteArrayOutputStream()
    (bitmap ?: return@withContext data).compress(Bitmap.CompressFormat.JPEG, 80, out)
    out.toByteArray()
}
