package com.yjsoft.mlkit

import android.annotation.SuppressLint
import android.graphics.Rect
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import java.util.*
import kotlin.concurrent.schedule

class HandleImageAnalyser(
    private val listener: (DecoderResult) -> Unit
) :
    ImageAnalysis.Analyzer {
    private var isWait: Boolean = false
    var decoders: ArrayList<Decoder> = ArrayList()
    var decoderResult: DecoderResult = DecoderResult()

    companion object {
        const val TAG = "扫描梯度处理"
    }

    lateinit var telListener: ((rect: Rect) -> Unit)

    private fun addDecoder(d: Decoder) {
        decoders.add(d)
    }

    fun setMode(mode: Int) {
        decoderResult.scanMode = mode
        if (decoders.size != 0) decoders.clear()
        when (mode) {
            MODE_CODE_FREETEL,
            MODE_CODE_TEL -> {
                addDecoder(CodeDecoder())
                addDecoder(TelDecoder())
            }
            MODE_CODE -> {
                addDecoder(CodeDecoder())
            }
            MODE_TEL -> {
                addDecoder(TelDecoder())
            }
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (!isWait) {
            decoderResult.clear()
            decoders?.let {
                it.forEach { decoder ->
                    val data = decoder.onDecoder(imageProxy)
                    data?.let { result ->
                        if (data.isTel && this::telListener.isInitialized) telListener(result.rect!!)
                        decoderResult.addData(data)
                    }
                }
                checkMode()
            }
        }
        imageProxy.close()
    }

    private fun checkMode() {
        var isCall = false
        when (decoderResult.scanMode) {
            MODE_CODE,
            MODE_CODE_FREETEL -> {
                isCall = decoderResult.hasCode()
            }
            MODE_TEL -> {
                isCall = decoderResult.hasTel()
            }
            MODE_CODE_TEL -> {
                isCall = decoderResult.hasCode() && decoderResult.hasTel()
            }
        }
        if (isCall) {
            listener(decoderResult)
            isWait = true
            Timer().schedule(600) {
                isWait = false
            }
        }
    }
}