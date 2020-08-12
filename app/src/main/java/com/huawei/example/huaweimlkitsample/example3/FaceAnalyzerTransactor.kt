package com.huawei.example.huaweimlkitsample.example3


import com.huawei.example.huaweimlkitsample.example3.camerautil.GraphicOverlay
import com.huawei.hms.mlsdk.common.MLAnalyzer
import com.huawei.hms.mlsdk.common.MLAnalyzer.MLTransactor
import com.huawei.hms.mlsdk.face.MLFace


class FaceAnalyzerTransactor internal constructor(ocrGraphicOverlay: GraphicOverlay) :
    MLTransactor<MLFace?> {
    private val mGraphicOverlay: GraphicOverlay
    override fun transactResult(result: MLAnalyzer.Result<MLFace?>) {
        mGraphicOverlay.clear()
        val faceSparseArray = result.analyseList
        for (i in 0 until faceSparseArray.size()) {
            // todo step 4: add on-device face graphic
            val graphic =
                MLFaceGraphic(mGraphicOverlay, faceSparseArray.valueAt(i))
            mGraphicOverlay.add(graphic)
            // finish
        }
    }

    override fun destroy() {
        mGraphicOverlay.clear()
    }

    init {
        mGraphicOverlay = ocrGraphicOverlay
    }
}