package com.company.app.ui.platform

import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap

actual fun ByteArray.decodeImageBitmap(): ImageBitmap? =
    runCatching { BitmapFactory.decodeByteArray(this, 0, this.size)?.asImageBitmap() }.getOrNull()
