package com.yjsoft.mlkit

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.util.Size
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewTreeObserver
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import java.util.concurrent.Executors

class ScanKit(var mContext: Context, private val scanListener: (DecoderResult) -> Unit) {
    private var surfaceHolderUI: SurfaceHolder? = null
    private var mPreviewBuild: Preview? = null
    private var mImageAnalysis: ImageAnalysis? = null
    private var mCameraSelector: CameraSelector? = null
    private lateinit var mCameraProvider: ProcessCameraProvider
    private lateinit var mCamera: Camera
    private lateinit var mCameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var mPreview: PreviewView
    private var mOverlay: SurfaceView? = null
    private var listener: OverlayListener? = null
    private lateinit var handleImageAnalyser: HandleImageAnalyser
    private var mode: Int = 0
    private var mSize: Size? = null
        get() {
            return if (field == null) {
                requestSize()
            } else {
                field
            }
        }

    private fun requestSize(): Size {
        return if (mOverlay != null) {
            Size(mOverlay!!.width, mOverlay!!.height)
        } else {
            val resources: Resources = mContext.resources
            val dm: DisplayMetrics = resources.displayMetrics
            val screenWidth: Int = dm.widthPixels
            val screenHeight: Int = dm.heightPixels
            Size(screenWidth, screenHeight)
        }
    }

    fun setSize(size: Size) {
        mSize = size
    }

    fun setMode(mode: Int) {
        this.mode = mode
    }

    /**
     * 需要在bindpreview之前调用
     */
    fun bindOverlay(overlay: SurfaceView) {
        if (this::mPreview.isInitialized) throw RuntimeException("\"bindOverlay\" must be called before \"bindPreView\"")
        mOverlay = overlay
        surfaceHolderUI = mOverlay?.holder
        surfaceHolderUI?.setFormat(PixelFormat.TRANSLUCENT)
        mOverlay?.setZOrderMediaOverlay(true)
    }

    fun bindPreView(preview: PreviewView) {
        mPreview = preview
        mCameraProviderFuture = ProcessCameraProvider.getInstance(mContext)
    }

    fun start() {
        bindPre()
        if (mOverlay == null) {
            bindLifecycle()
        } else {
            listener = OverlayListener()
            mOverlay!!.viewTreeObserver.addOnGlobalLayoutListener(listener)
        }
    }

    inner class OverlayListener : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            mCameraProviderFuture.addListener({
                bindLifecycle()
            }, ContextCompat.getMainExecutor(mContext))
            mOverlay?.viewTreeObserver?.removeOnGlobalLayoutListener(listener)
        }
    }

    private fun bindPre() {
        mPreviewBuild = Preview.Builder()
            .build()
        //绑定预览
        mPreviewBuild?.setSurfaceProvider(mPreview.surfaceProvider)
        //使用后置相机
        mCameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        //配置图片扫描
        mImageAnalysis = ImageAnalysis.Builder()
            .setTargetResolution(mSize!!)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()

        handleImageAnalyser = HandleImageAnalyser {
            Handler(Looper.getMainLooper()).post {
                scanListener(it)
            }
        }
        handleImageAnalyser.setMode(mode)
        handleImageAnalyser.telListener = {
            drawGraphics(it)
        }

        //绑定图片扫描解析
        mImageAnalysis?.setAnalyzer(
            Executors.newSingleThreadExecutor(),
            handleImageAnalyser
        )
        mCameraProvider = mCameraProviderFuture.get()
    }

    // 锁住surface画图
    private fun drawGraphics(rect: Rect) {
        surfaceHolderUI?.run {
            val canvas: Canvas = lockCanvas(null)
            canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
            val paint = Paint(Paint.ANTI_ALIAS_FLAG)
            paint.style = Paint.Style.STROKE
            paint.color = Color.RED
            paint.strokeWidth = 5f
            canvas.drawRect(rect, paint)
            unlockCanvasAndPost(canvas)
        }
    }

    private fun bindLifecycle() {
        //将相机绑定到当前控件的生命周期
        mCamera = mCameraProvider.bindToLifecycle(
            mContext as LifecycleOwner,
            mCameraSelector!!,
            mImageAnalysis,
            mPreviewBuild
        )
    }

    fun resume() {
        mCameraProvider.unbindAll()
    }

    fun pause() {
        bindLifecycle()
    }
}