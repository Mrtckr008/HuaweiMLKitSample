package com.huawei.example.huaweimlkitsample.example3


import com.huawei.example.huaweimlkitsample.example3.camerautil.CustomGraphicOverlayView
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLAnalyzer.MLTransactor
import com.huawei.hms.mlsdk.face.MLFace


class FaceAnalyzerTransactor internal constructor(ocrGraphicOverlay: CustomGraphicOverlayView) :
    MLTransactor<MLFace?> {
    private val mCustomGraphicOverlayView: CustomGraphicOverlayView = ocrGraphicOverlay
    override fun transactResult(result: MLAnalyzer.Result<MLFace?>) {
        mCustomGraphicOverlayView.clear()
        val faceSparseArray = result.analyseList
        for (i in 0 until faceSparseArray.size()) {
            val graphic =
                DrawFaceGraphic(mCustomGraphicOverlayView, faceSparseArray.valueAt(i))
            mCustomGraphicOverlayView.add(graphic)
        }
    }

    override fun destroy() {
        mCustomGraphicOverlayView.clear()
    }

}