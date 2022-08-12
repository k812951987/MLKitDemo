package com.yjsoft.mlkit

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.text.TextUtils
import android.util.Log
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage

class CodeDecoder : Decoder {
    //配置当前扫码格式
    private val options by lazy {
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(
                Barcode.FORMAT_CODE_128,
                Barcode.TYPE_PHONE
            )
            .build()
    }
    var result: String? = null


    @SuppressLint("UnsafeExperimentalUsageError")
    override fun onDecoder(imageProxy: ImageProxy): Data? {
        val mediaImage = imageProxy.image
        val image = mediaImage?.let {
            InputImage.fromMediaImage(
                it,
                imageProxy.imageInfo.rotationDegrees
            )
        }
        val task = image?.let {
            BarcodeScanning.getClient(options).process(it)
        } ?: return null
        val barCodes = Tasks.await(task)
        if (barCodes.size > 0) {
            var bitmap = BitmapUtils.getBitmap(imageProxy)
            bitmap?.run {
                try {
                    val newBitmap = Bitmap.createBitmap(
                        this,
                        barCodes[0].boundingBox?.left!!,
                        barCodes[0].boundingBox?.bottom!! - barCodes[0].boundingBox?.height()!!,
                        barCodes[0].boundingBox?.width()!!,
                        barCodes[0].boundingBox?.height()!!
                    )
                    val squareDeviation = OpenCvUtil.getSquareDeviation(
                        newBitmap
                    )
                    //清晰度<1000时或上次扫描结果<1000时进行2次比对
                    if (squareDeviation < 1000 || result != null) {
                        if (result == null) {
                            result = barCodes[0].displayValue
                            return@run
                        }
                        if (result != barCodes[0].displayValue) {
                            result = barCodes[0].displayValue
                            return@run
                        }
                    }
                    return Data(getType(), barCodes[0].displayValue)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        return null
    }

    override fun getType(): ScanType {
        return ScanType.RESULT_CODE
    }
}