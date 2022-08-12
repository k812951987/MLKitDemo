package com.hsmedia.mlkitdemo

import android.app.Application
import org.opencv.android.OpenCVLoader

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        OpenCVLoader.initDebug(false)
    }
}