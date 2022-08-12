package com.yjsoft.mlkit

import android.graphics.Rect

class DecoderResult {
    var scanMode: Int = 101
    var telData: Data? = null
    var codeData: Data? = null

    fun hasTel(): Boolean {
        return telData != null
    }

    fun hasCode(): Boolean {
        return codeData != null
    }

    fun addData(data: Data) {
        when (data.scanType) {
            ScanType.RESULT_CODE -> {
                codeData = data
            }
            ScanType.RESULT_TEL -> {
                telData = data
            }
        }
    }

    override fun toString(): String {
        return "手机识别结果：${telData?.content},条码识别结果：${codeData?.content}"
    }

    fun clear() {
        telData = null
        codeData = null
    }
}

//code优先，手机号可有可无
const val MODE_CODE_FREETEL = 101

//code+手机号
const val MODE_CODE_TEL = 102

//code
const val MODE_CODE = 103

//手机号
const val MODE_TEL = 104

enum class ScanType {
    RESULT_CODE, RESULT_TEL
}

class Data(
    var scanType: ScanType,
    var content: String?,
    var isTrust: Boolean = false,
    var rect: Rect? = null
) {
    var isTel: Boolean = scanType == ScanType.RESULT_TEL
    var isCode: Boolean = scanType == ScanType.RESULT_CODE
}