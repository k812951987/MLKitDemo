package com.yjsoft.mlkit

import androidx.camera.core.ImageProxy

interface Decoder {
    fun onDecoder(inputImage: ImageProxy):Data?

    fun getType():ScanType
}