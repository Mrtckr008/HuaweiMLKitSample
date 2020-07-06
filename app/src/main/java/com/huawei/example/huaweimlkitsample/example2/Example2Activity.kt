package com.huawei.example.huaweimlkitsample.example2

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.huawei.example.huaweimlkitsample.R
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.MLAnalyzerFactory
import com.huawei.hms.mlsdk.classification.MLImageClassification
import com.huawei.hms.mlsdk.classification.MLLocalClassificationAnalyzerSetting
import com.huawei.hms.mlsdk.classification.MLRemoteClassificationAnalyzerSetting
import com.huawei.hms.mlsdk.common.MLException
import com.huawei.hms.mlsdk.common.MLFrame
import com.huawei.hms.mlsdk.face.MLFace
import com.huawei.hms.mlsdk.face.MLFaceAnalyzerSetting
import com.huawei.hms.mlsdk.text.MLLocalTextSetting
import com.huawei.hms.mlsdk.text.MLRemoteTextSetting
import com.huawei.hms.mlsdk.text.MLText
import com.huawei.hms.mlsdk.tts.*
import kotlinx.android.synthetic.main.activity_example2.*
import kotlinx.android.synthetic.main.activity_example2.pick_image_icon
import kotlinx.android.synthetic.main.activity_example2.take_picture_icon
import java.io.IOException


class Example2Activity : AppCompatActivity() {
    private val myCameraRequestCode = 100
    private val myStorageRequestCode = 300
    private var selectedImageBitmap:Bitmap?=null
    private var mlTtsEngine: MLTtsEngine?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_example2)
        take_picture_icon.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), myCameraRequestCode)
            } else {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 100)
            }
        }

        pick_image_icon.setOnClickListener {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), myStorageRequestCode)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 200)
            }
        }

        microphone_device_icon.setOnClickListener {
            textToSpeech(text_recognition_device_text.text.toString())
        }

        microphone_cloud_icon.setOnClickListener {
            textToSpeech(text_recognition_cloud_text.text.toString())
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) { //  resim seçildiğinde yapılacaklar
                selectedImageBitmap = data?.extras?.get("data") as Bitmap
                selected_image.setImageBitmap(selectedImageBitmap)
                performTextRecognitionOnCloud(selectedImageBitmap!!)
                performTextRecognitionOnDevice(selectedImageBitmap!!)
                performStaticImageClassificationOnDevice(selectedImageBitmap!!)
                performStaticImageClassificationOnCloud(selectedImageBitmap!!)
                performFaceDetection(selectedImageBitmap!!)

            }
        }
        if (requestCode == 200) {
            if (resultCode == Activity.RESULT_OK) {
                val pickedImage: Uri? = data?.data
                try {
                    selected_image.setImageBitmap(MediaStore.Images.Media.getBitmap(this.contentResolver, pickedImage))
                    performTextRecognitionOnCloud(MediaStore.Images.Media.getBitmap(this.contentResolver, pickedImage))
                    performTextRecognitionOnDevice(MediaStore.Images.Media.getBitmap(this.contentResolver, pickedImage))
                    performStaticImageClassificationOnDevice(MediaStore.Images.Media.getBitmap(this.contentResolver, pickedImage))
                    performStaticImageClassificationOnCloud(MediaStore.Images.Media.getBitmap(this.contentResolver, pickedImage))
                    performFaceDetection(MediaStore.Images.Media.getBitmap(this.contentResolver, pickedImage))
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == myCameraRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                startActivityForResult(intent, 100)
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
        else if(requestCode == myStorageRequestCode){
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, 200)
            } else {
                Toast.makeText(this, "Storage permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    private fun performTextRecognitionOnCloud(bitmap: Bitmap){
        // Method 1: Use customized parameter settings.

        val languageList: MutableList<String> = ArrayList()
        languageList.add("zh")
        languageList.add("en")  // added Chinese & English
        // Set parameters.
        val setting =
            MLRemoteTextSetting.Factory() // Set the on-cloud text detection mode.
                // MLRemoteTextSetting.OCR_COMPACT_SCENE: dense text recognition
                // MLRemoteTextSetting.OCR_LOOSE_SCENE: sparse text recognition
                .setTextDensityScene(MLRemoteTextSetting.OCR_LOOSE_SCENE) // Specify the languages that can be recognized, which should comply with ISO 639-1.
                .setLanguageList(languageList) // Set the format of the returned text border box.
                // MLRemoteTextSetting.NGON: Return the coordinates of the four corner points of the quadrilateral.
                // MLRemoteTextSetting.ARC: Return the corner points of a polygon border in an arc. The coordinates of up to 72 corner points can be returned.
                .setBorderType(MLRemoteTextSetting.ARC)
                .create()
        val analyzer1 =
            MLAnalyzerFactory.getInstance().getRemoteTextAnalyzer(setting)
// Method 2: Use the default parameter settings to automatically detect languages for text recognition. This method is applicable to sparse text scenarios. The format of the returned text box is MLRemoteTextSetting.NGON.
        val frame = MLFrame.fromBitmap(bitmap)

        val task: Task<MLText> = analyzer1.asyncAnalyseFrame(frame)
        task.addOnSuccessListener {
            // Recognition success.
            text_recognition_cloud_text.text = it.stringValue
        }.addOnFailureListener { e ->
            // If the recognition fails, obtain related exception information.
            try {
                val mlException = e as MLException
                // Obtain the result code. You can process the result code and customize respective messages displayed to users.
                val errorCode = mlException.errCode
                // Obtain the error information. You can quickly locate the fault based on the result code.
                val errorMessage = mlException.message
            } catch (error: Exception) {
                // Handle the conversion error.
            }
        }
    }

    private fun performTextRecognitionOnDevice(bitmap: Bitmap){
        val setting = MLLocalTextSetting.Factory()
            .setOCRMode(MLLocalTextSetting.OCR_DETECT_MODE) // Specify languages that can be recognized.
            .setLanguage("en")
            .create()
        val analyzer = MLAnalyzerFactory.getInstance().getLocalTextAnalyzer(setting)
        val frame = MLFrame.fromBitmap(bitmap)

        val task: Task<MLText> = analyzer.asyncAnalyseFrame(frame)
        task.addOnSuccessListener {
            text_recognition_device_text.text = it.stringValue
        }.addOnFailureListener {
            Toast.makeText(this,"Text recognition failed, error code ->${it.message}",Toast.LENGTH_SHORT).show()
        }
    }

    private fun performStaticImageClassificationOnDevice(bitmap: Bitmap){
        // Method 1: Use customized parameter settings for on-device recognition.
        val deviceSetting =
            MLLocalClassificationAnalyzerSetting.Factory()
                .setMinAcceptablePossibility(0.8f)
                .create()
        val analyzer1 =
            MLAnalyzerFactory.getInstance().getLocalImageClassificationAnalyzer(deviceSetting)

        val frame = MLFrame.fromBitmap(bitmap)

        val task: Task<List<MLImageClassification>> = analyzer1.asyncAnalyseFrame(frame)
        task.addOnSuccessListener { it ->
            image_classification_device_text.text=""
            it.forEach {
                image_classification_device_text.append("Name: "+it.name+ " - Possibility: %" + it.possibility*100+"\n")
            }
        }.addOnFailureListener { e ->
            // Recognition failure.
            try {
                val mlException = e as MLException
                // Obtain the result code. You can process the result code and customize respective messages displayed to users.
                val errorCode = mlException.errCode
                // Obtain the error information. You can quickly locate the fault based on the result code.
                val errorMessage = mlException.message
            } catch (error: Exception) {
                // Handle the conversion error.
            }
        }
    }

    private fun performStaticImageClassificationOnCloud(bitmap: Bitmap){
        val cloudSetting =
            MLRemoteClassificationAnalyzerSetting.Factory()
                .setMinAcceptablePossibility(0.8f)
                .create()
        val analyzer3 =
            MLAnalyzerFactory.getInstance().getRemoteImageClassificationAnalyzer(cloudSetting)

        // Create an MLFrame object using the bitmap, which is the image data in bitmap format.

        val frame = MLFrame.fromBitmap(bitmap)

        val task: Task<List<MLImageClassification>> = analyzer3.asyncAnalyseFrame(frame)
        task.addOnSuccessListener { it ->
            image_classification_cloud_text.text=""
            it.forEach {
                image_classification_cloud_text.append("Name: "+it.name+ " - Possibility: %" + it.possibility*100+"\n")
            }
        }.addOnFailureListener { e ->
            // Recognition failure.
            try {
                val mlException = e as MLException
                // Obtain the result code. You can process the result code and customize respective messages displayed to users.
                val errorCode = mlException.errCode
                // Obtain the error information. You can quickly locate the fault based on the result code.
                val errorMessage = mlException.message
            } catch (error: Exception) {
                // Handle the conversion error.
            }
        }
    }

    private fun textToSpeech(text:String){
// Use customized parameter settings to create a TTS engine.
        // Use customized parameter settings to create a TTS engine.
        val mlConfigs: MLTtsConfig = MLTtsConfig() // Set the text converted from speech to Chinese.
            // MLTtsConstants.TTS_EN_US: converts text to English.
            // MLTtsConstants.TTS_ZH_HANS: converts text to Chinese.
            // MLTtsConstants.TTS_LAN_ES_ES: converts text to Spanish.
            // MLTtsConstants.TTS_LAN_FR_FR: converts text to French.
            .setLanguage(MLTtsConstants.TTS_EN_US)
            .setPerson(MLTtsConstants.TTS_SPEAKER_FEMALE_EN)
            .setSpeed(1.0f) // Set the volume. Range: 0–2. 1.0 indicates normal volume.
            .setVolume(1.0f)
        mlTtsEngine = MLTtsEngine(mlConfigs)
// Update the configuration when the engine is running.
        mlTtsEngine?.updateConfig(mlConfigs)

        if(text.length>500)
            mlTtsEngine?.speak(text.substring(0,500), MLTtsEngine.QUEUE_APPEND)
        else{
            mlTtsEngine?.speak(text, MLTtsEngine.QUEUE_APPEND)
        }
    }

    private fun performFaceDetection(bitmap: Bitmap){
        // Method 1: Use customized parameter settings.
// If the Full SDK mode is used for integration, set parameters based on the integrated model package.
        val setting =
            MLFaceAnalyzerSetting.Factory() // Set whether to detect key face points.
                .setKeyPointType(MLFaceAnalyzerSetting.TYPE_KEYPOINTS) // Set whether to detect facial features.
                .setFeatureType(MLFaceAnalyzerSetting.TYPE_FEATURES) // Set whether to detect face contour points.
                .setShapeType(MLFaceAnalyzerSetting.TYPE_SHAPES) // Set whether to enable face tracking.
                .setTracingAllowed(true) // Set the speed and precision of the detector.
                .setPerformanceType(MLFaceAnalyzerSetting.TYPE_SPEED)
                .create()
        val analyzer1 = MLAnalyzerFactory.getInstance().getFaceAnalyzer(setting)

        // Create an MLFrame object using the bitmap.

        val frame = MLFrame.fromBitmap(bitmap)

        val task: Task<List<MLFace>> =
            analyzer1.asyncAnalyseFrame(frame)
        task.addOnSuccessListener {
            if(it.isEmpty()){
                face_detection_device_text.text="No face detected"
            }
            else{
                face_detection_device_text.text = ""
                face_detection_device_text.append("Angry possibility: "+(it[0].emotions.angryProbability)*100+ " - Age: " + it[0].features.age+"\n")
                face_detection_device_text.append("Smiling possibility: "+(it[0].emotions.smilingProbability)*100+ " - Moustache: %" + (it[0].features.moustacheProbability)*100+"\n")
                face_detection_device_text.append("Sad possibility: "+(it[0].emotions.sadProbability)*100+ " - Sun glass: %" + (it[0].features.sunGlassProbability)*100+"\n")
            }
        }.addOnFailureListener { e ->
            // Detection failure.
            // Recognition failure.
            try {
                val mlException = e as MLException
                // Obtain the result code. You can process the result code and customize respective messages displayed to users.
                val errorCode = mlException.errCode
                // Obtain the error information. You can quickly locate the fault based on the result code.
                val errorMessage = mlException.message
            } catch (error: java.lang.Exception) {
                // Handle the conversion error.
            }
        }
    }

    private var callback: MLTtsCallback = object : MLTtsCallback {
        override fun onError(taskId: String, err: MLTtsError) {
            println("error -> ${err.errorMsg}")
        }

        override fun onWarn(taskId: String, warn: MLTtsWarn) {
            println("warm -> ${warn.warnMsg}")
        }

        override fun onRangeStart(taskId: String, start: Int, end: Int) {
            println("start -> ${start}")
        }

        override fun onAudioAvailable(p0: String?, p1: MLTtsAudioFragment?, p2: Int, p3: android.util.Pair<Int, Int>?, p4: Bundle?) {
            println("avaliable -> ${p0}")
        }

        override fun onEvent(taskId: String, eventName: Int, bundle: Bundle?) {
            if (eventName == MLTtsConstants.EVENT_PLAY_STOP) {
                Toast.makeText(applicationContext, "Service Stopped", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onPause() {
        mlTtsEngine?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        mlTtsEngine?.shutdown()
        super.onDestroy()
    }
}