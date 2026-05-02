package com.company.app.ui.platform

import androidx.compose.ui.graphics.ImageBitmap

expect fun ByteArray.decodeImageBitmap(): ImageBitmap?
