package com.huawei.example.huaweimlkitsample.example3

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.CompoundButton
import android.widget.ToggleButton
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.huawei.example.huaweimlkitsample.R
import com.huawei.example.huaweimlkitsample.example3.camerautil.CameraSourcePreview
import com.huawei.example.huaweimlkitsample.example3.camerautil.CustomGraphicOverlayView
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.LensEngine
import com.huawei.hms.mlsdk.face.MLFaceAnalyzer
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting
import java.io.IOException

class FaceDetectionStreamActivity : AppCompatActivity(),
    CompoundButton.OnCheckedChangeListener {
    var analyzer: MLFaceAnalyzer? = null
    private var mLensEngine: LensEngine? = null
    private var mPreview: CameraSourcePreview? = null
    private var mOverlay: CustomGraphicOverlayView? = null
    private var lensType = LensEngine.FRONT_LENS
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.setContentView(R.layout.activity_live_image_detection)
        mPreview = findViewById(R.id.preview)
        mOverlay = findViewById(R.id.overlay)
        createFaceAnalyzer()
        val facingSwitch = findViewById<ToggleButton>(R.id.facingSwitch)
        facingSwitch.setOnCheckedChangeListener(this)
        // Checking Camera Permissions
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            createLensEngine()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        val permissions = arrayOf(Manifest.permission.CAMERA)
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)
        ) {
            ActivityCompat.requestPermissions(this, permissions, CAMERA_PERMISSION_CODE)
            return
        }
    }

    override fun onResume() {
        super.onResume()
        startLensEngine()
    }

    override fun onPause() {
        super.onPause()
        mPreview!!.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mLensEngine != null) {
            mLensEngine!!.release()
        }
        if (analyzer != null) {
            analyzer!!.destroy()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode != CAMERA_PERMISSION_CODE) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            return
        }
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            createLensEngine()
            return
        }
    }

    public override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        if (mLensEngine != null) {
            if (isChecked) {
                lensType = LensEngine.FRONT_LENS
            } else {
                lensType = LensEngine.BACK_LENS
            }
        }
        mLensEngine!!.close()
        createLensEngine()
        startLensEngine()
    }

    private fun createFaceAnalyzer(): MLFaceAnalyzer? {
        val setting = MLFaceAnalyzerSetting.Factory()
            .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES)
            .setPerformanceType(MLFaceAnalyzerSetting.TYPE_SPEED)
            .allowTracing()
            .create()
        analyzer = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)
        analyzer!!.setTransactor(FaceAnalyzerTransactor(mOverlay!!))
        return analyzer
    }

    private fun createLensEngine() {
        val context = this.applicationContext
        mLensEngine = LensEngine.Creator(context, analyzer)
            .setLensType(lensType)
            .applyDisplayDimension(1600, 1024)
            .applyFps(25.0f)
            .enableAutomaticFocus(true)
            .create()
    }

    private fun startLensEngine() {
        if (mLensEngine != null) {
            try {
                mPreview!!.start(mLensEngine, mOverlay)
            } catch (e: IOException) {
                Log.e(TAG, "Failed to start lens engine.", e)
                mLensEngine!!.release()
                mLensEngine = null
            }
        }
    }

    companion object {
        private const val TAG = "LiveImageDetection"
        private const val CAMERA_PERMISSION_CODE = 2
    }
}
