package com.company.app.ui.aiscan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import platform.CoreGraphics.CGSize
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSData
import platform.UIKit.*
import platform.darwin.NSObject
import platform.posix.memcpy

// iOS camera: auto-launch UIImagePickerController the moment this screen enters.
// UIImagePickerControllerDelegateProtocol uses required ObjC methods — fully
// compatible with Kotlin/Native (unlike AVCapturePhotoCaptureDelegate which is
// all-optional and cannot be overridden from Kotlin).
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun AiScanScreen(
    onPhotoCaptured: (ByteArray) -> Unit,
    onBack: () -> Unit,
) {
    val currentOnPhotoCaptured by rememberUpdatedState(onPhotoCaptured)
    val currentOnBack by rememberUpdatedState(onBack)

    // Hold delegate in state to keep it alive for the duration of the session.
    var pickerDelegate by remember { mutableStateOf<CameraPickerDelegate?>(null) }

    LaunchedEffect(Unit) {
        val delegate = CameraPickerDelegate(
            onImageCaptured = { bytes -> currentOnPhotoCaptured(bytes) },
            onCancel = { currentOnBack() },
        )
        pickerDelegate = delegate

        val vc = topViewController()
        if (vc != null) launchPicker(vc, delegate) else currentOnBack()
    }

    // Compose layer is hidden behind the full-screen picker.
    Box(modifier = Modifier.fillMaxSize().background(Color.Black))
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

@Suppress("DEPRECATION")
private fun topViewController(): UIViewController? {
    var vc = UIApplication.sharedApplication.keyWindow?.rootViewController
    while (vc?.presentedViewController != null) vc = vc.presentedViewController
    return vc
}

private fun launchPicker(presentingVC: UIViewController, delegate: CameraPickerDelegate) {
    val picker = UIImagePickerController()
    picker.sourceType = if (
        UIImagePickerController.isSourceTypeAvailable(
            UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
        )
    ) {
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypeCamera
    } else {
        UIImagePickerControllerSourceType.UIImagePickerControllerSourceTypePhotoLibrary
    }
    picker.allowsEditing = false
    picker.delegate = delegate
    presentingVC.presentViewController(picker, animated = true, completion = null)
}

// ─── Delegate ────────────────────────────────────────────────────────────────

private class CameraPickerDelegate(
    private val onImageCaptured: (ByteArray) -> Unit,
    private val onCancel: () -> Unit,
) : NSObject(), UIImagePickerControllerDelegateProtocol, UINavigationControllerDelegateProtocol {

    @OptIn(ExperimentalForeignApi::class)
    override fun imagePickerController(
        picker: UIImagePickerController,
        didFinishPickingMediaWithInfo: Map<Any?, *>,
    ) {
        @Suppress("UNCHECKED_CAST")
        val image = didFinishPickingMediaWithInfo[UIImagePickerControllerOriginalImage] as? UIImage
        picker.dismissViewControllerAnimated(true) {
            if (image != null) {
                val resized = image.resizedToMaxDimension(1280.0)
                val data = UIImageJPEGRepresentation(resized, 0.7)
                if (data != null) onImageCaptured(data.toByteArray()) else onCancel()
            } else {
                onCancel()
            }
        }
    }

    override fun imagePickerControllerDidCancel(picker: UIImagePickerController) {
        picker.dismissViewControllerAnimated(true) { onCancel() }
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun NSData.toByteArray(): ByteArray {
    val length = this.length.toInt()
    if (length == 0) return ByteArray(0)
    return ByteArray(length).also { array ->
        array.usePinned { pinned ->
            memcpy(pinned.addressOf(0), this.bytes, this.length)
        }
    }
}

// Downscale so the longest edge is <= maxDimension (preserves aspect ratio).
// Keeps payload well under the backend 10MB multipart cap and speeds up Gemini.
@OptIn(ExperimentalForeignApi::class)
private fun UIImage.resizedToMaxDimension(maxDimension: Double): UIImage {
    val w = this.size.useContents { width }
    val h = this.size.useContents { height }
    val longest = maxOf(w, h)
    if (longest <= maxDimension) return this

    val scale = maxDimension / longest
    val newSize: CValue<CGSize> = CGSizeMake(w * scale, h * scale)

    UIGraphicsBeginImageContextWithOptions(newSize, false, 1.0)
    this.drawInRect(
        platform.CoreGraphics.CGRectMake(0.0, 0.0, w * scale, h * scale)
    )
    val resized = UIGraphicsGetImageFromCurrentImageContext()
    UIGraphicsEndImageContext()
    return resized ?: this
}
