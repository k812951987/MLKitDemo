package com.yjsoft.mlkit

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.util.Size
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.android.synthetic.main.activity_barcode_scanning.*
import java.util.concurrent.Executors


class BarcodeScanningActivity : AppCompatActivity() {

    private val TAG = "扫描梯度处理"

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>

    private var camera: Camera? = null

    private var scaleX = 0f

    private var scaleY = 0f
    private lateinit var mScanKit:ScanKit

    companion object {
        const val SCAN_RESULT = "BarcodeScanningActivity.scan_result"
        const val REQUEST_PERMISSION = 12345
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_barcode_scanning)
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            REQUEST_PERMISSION
        )
        cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        initScan()
    }

    private fun initScan() {
        mScanKit = ScanKit(this){
            Log.e(TAG,it.toString())
            tv_tips.text = it.codeData?.content
            tv_tips2.text = it.telData?.content
        }
        mScanKit.bindOverlay(overlay)
        mScanKit.bindPreView(previewView)
        mScanKit.setMode(MODE_CODE_FREETEL)
        mScanKit.start()
    }
}