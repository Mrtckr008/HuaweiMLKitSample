package com.huawei.example.huaweimlkitsample.example3.camerautil

import android.content.Context
import android.content.res.Configuration
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.ViewGroup
import com.huawei.hms.mlsdk.common.LensEngine
import java.io.IOException

class CameraSourcePreview(private val mContext: Context, attrs: AttributeSet?) :
       ViewGroup(mContext, attrs) {
    private val mSurfaceView: SurfaceView = SurfaceView(mContext)
    private var mStartRequested = false
    private var mSurfaceAvailable = false
    private var mLensEngine: LensEngine? = null
    private var mOverlay: CustomGraphicOverlayView? = null

    private fun start(lensEngine: LensEngine?) {
        if (lensEngine == null) {
            stop()
        }
        mLensEngine = lensEngine
        if (mLensEngine != null) {
            mStartRequested = true
            startIfReady()
        }
    }

    fun start(lensEngine: LensEngine?, overlay: CustomGraphicOverlayView?) {
        mOverlay = overlay
        start(lensEngine)
    }

    fun stop() {
        if (mLensEngine != null) {
            mLensEngine!!.close()
        }
    }

    private fun startIfReady() {
        if (mStartRequested && mSurfaceAvailable) {
            mLensEngine!!.run(mSurfaceView.holder)
            if (mOverlay != null) {
                val size = mLensEngine!!.displayDimension
                val min = size.width.coerceAtMost(size.height)
                val max = size.width.coerceAtLeast(size.height)
                if (isPortraitMode) {
                    mOverlay!!.setCameraInfo(min, max, mLensEngine!!.lensType)
                } else {
                    mOverlay!!.setCameraInfo(max, min, mLensEngine!!.lensType)
                }
                mOverlay!!.clear()
            }
            mStartRequested = false
        }
    }

    private inner class SurfaceCallback : SurfaceHolder.Callback {
        override fun surfaceCreated(surface: SurfaceHolder) {
            mSurfaceAvailable = true
            try {
                startIfReady()
            } catch (e: IOException) {
                Log.e(TAG, "Could not start camera source.", e)
            }
        }

        override fun surfaceDestroyed(surface: SurfaceHolder) {
            mSurfaceAvailable = false
        }

        override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        var previewWidth = 320
        var previewHeight = 240
        if (mLensEngine != null) {
            val size = mLensEngine!!.displayDimension
            if (size != null) {
                previewWidth = size.width
                previewHeight = size.height
            }
        }

        if (isPortraitMode) {
            val tmp = previewWidth
            previewWidth = previewHeight
            previewHeight = tmp
        }
        val viewWidth = right - left
        val viewHeight = bottom - top
        val childWidth: Int
        val childHeight: Int
        var childXOffset = 0
        var childYOffset = 0
        val widthRatio = viewWidth.toFloat() / previewWidth.toFloat()
        val heightRatio = viewHeight.toFloat() / previewHeight.toFloat()

        if (widthRatio > heightRatio) {
            childWidth = viewWidth
            childHeight = (previewHeight.toFloat() * widthRatio).toInt()
            childYOffset = (childHeight - viewHeight) / 2
        } else {
            childWidth = (previewWidth.toFloat() * heightRatio).toInt()
            childHeight = viewHeight
            childXOffset = (childWidth - viewWidth) / 2
        }
        for (i in 0 until childCount) {
            /*
            childCount'a göre viewları uygun en boy oranına göre boyutunu ayarlıyoruz
             */
            getChildAt(i).layout(
                -1 * childXOffset, -1 * childYOffset,
                childWidth - childXOffset, childHeight - childYOffset
            )
        }
        try {
            startIfReady()
        } catch (e: IOException) {
            Log.e(TAG, "Could not start camera source.", e)
        }
    }

    private val isPortraitMode: Boolean
        get() {
            val orientation = mContext.resources.configuration.orientation
            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return false
            }
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                return true
            }
            Log.d(TAG, "isPortraitMode returning false by default")
            return false
        }

    companion object {
        private const val TAG = "CameraSourcePreview"
    }

    init {
        mSurfaceView.holder.addCallback(SurfaceCallback())
        addView(mSurfaceView)
    }
}