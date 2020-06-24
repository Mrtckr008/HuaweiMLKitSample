package com.huawei.example.huaweimlkitsample

import android.content.Intent
import android.graphics.Bitmap
import android.location.Criteria
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.text.Html
import androidx.appcompat.app.AppCompatActivity
import com.huawei.example.huaweimlkitsample.MainActivity.Companion.selectedImageBitmap
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmark
import com.huawei.hms.mlsdk.landmark.MLRemoteLandmarkAnalyzerSetting
import kotlinx.android.synthetic.main.activity_landmark_recognization.*
import java.text.DecimalFormat


class LandmarkRecognitionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landmark_recognization)

        landmark_recognition_image.setImageBitmap(selectedImageBitmap)
        selectedImageBitmap?.let { landmarkAnalyzer() }

        open_translate_button.setOnClickListener {
            val intentFromGallery = Intent(this, TranslateActivity::class.java)
            startActivity(intentFromGallery)
        }
    }

    private fun landmarkAnalyzer() {
        val settings = MLRemoteLandmarkAnalyzerSetting.Factory()
            .setLargestNumOfReturns(1)
            .setPatternType(MLRemoteLandmarkAnalyzerSetting.STEADY_PATTERN)
            .create()
        val analyzer = MLAnalyzerFactory.getInstance()
            .getRemoteLandmarkAnalyzer(settings)

        val mlFrame = MLFrame.Creator()
            .setBitmap(selectedImageBitmap).create()
        val task: Task<List<MLRemoteLandmark>> =
            analyzer.asyncAnalyseFrame(mlFrame)

        task.addOnSuccessListener {
            displaySuccess(it[0])
        }.addOnFailureListener {
            println("mcmcmc error is -> ${it.message}, ${it.localizedMessage}")
           displayFailure()
        } }

    fun displaySuccess(landmark: MLRemoteLandmark) {
        if (landmark.landmark.contains("retCode") || landmark.landmark
                .contains("retMsg") || landmark.landmark.contains("fail")
        ) {
            landmark_information_text.text = "The landmark was not recognized."
        } else {
            var longitude = 0.0
            var latitude = 0.0
            var possibility = ""
            var landmarkName = ""
            var result = StringBuilder()
            if (landmark.landmark != null) {
                result = StringBuilder(
                    """
                        Landmark information
                        ${landmark.landmark}
                        """.trimIndent()
                )
                landmarkName = landmark.landmark
            }
            landmark_information_text.text = "LANDMARK INFORMATION : ${landmark.landmark}"

            if (landmark.positionInfos != null) {
                for (coordinate in landmark.positionInfos) {
                    //           setText(Html.fromHtml("<b>" + myText + "</b>");
                    result.append("\nLatitude: ").append(coordinate.lat)
                    result.append("\nLongitude: ").append(coordinate.lng)
                    result.append("\nPossibility: %")
                        .append(DecimalFormat("##.##").format(landmark.possibility * 100))
                    longitude = coordinate.lng
                    latitude = coordinate.lat
                    possibility = DecimalFormat("##.##").format(landmark.possibility * 100)
                }
            }
            landmark_information_text.text = Html.fromHtml("<big><b>Landmark Information</b></big> <br><big><b>$landmarkName</b></big><br><b>Latitude: </b>$latitude<br><b>Longitude: </b>$longitude<br><b>Possibility: </b>%$possibility")
        }
    }
    private fun displayFailure() {

    }
}

