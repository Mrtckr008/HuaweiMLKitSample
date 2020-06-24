package com.huawei.example.huaweimlkitsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import com.huawei.hmf.tasks.Task
import com.huawei.hms.mlsdk.langdetect.MLLangDetectorFactory
import com.huawei.hms.mlsdk.langdetect.cloud.MLRemoteLangDetector
import com.huawei.hms.mlsdk.translate.MLTranslatorFactory
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslateSetting
import com.huawei.hms.mlsdk.translate.cloud.MLRemoteTranslator
import kotlinx.android.synthetic.main.activity_translate.*

class TranslateActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {
    val countryArray = arrayOf("Chinese", "English", "French", "Arabic", "Thai","Spanish","Turkish","Portuguese","Japanese","German","Italian","Russian")
    var sourceLangCode="en"
    var targetLangCode="zh"
    var mlRemoteTranslator: MLRemoteTranslator?=null
    var translateSetting:MLRemoteTranslateSetting?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_translate)
        language_spinner.onItemSelectedListener = this

        translateSetting = MLRemoteTranslateSetting.Factory() // Set the source language code. The ISO 639-1 standard is used. This parameter is optional. If this parameter is not set, the system automatically detects the language.
            .setSourceLangCode(sourceLangCode) // Set the target language code. The ISO 639-1 standard is used.
            .setTargetLangCode(targetLangCode)
            .create()

        val aa: ArrayAdapter<*> = ArrayAdapter<Any?>(this, android.R.layout.simple_spinner_item, countryArray)
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        language_spinner?.adapter = aa

        mlRemoteTranslator = MLTranslatorFactory.getInstance().getRemoteTranslator(translateSetting)

        translate_button.setOnClickListener {
            val mlRemoteLangDetect: MLRemoteLangDetector = MLLangDetectorFactory.getInstance()
                .remoteLangDetector
            val firstBestDetectTask: Task<String> = mlRemoteLangDetect.firstBestDetect(translate_edittext.text.toString())
            firstBestDetectTask.addOnSuccessListener {
                println("mcmc lang detect is -> $it")
                sourceLangCode=it
                translate()
                // Processing logic for detection success.
            }.addOnFailureListener {
                // Processing logic for detection failure.
            }
        }
    }

    override fun onNothingSelected(p0: AdapterView<*>?) {
       
    }

    override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
        translation_language_text.text=countryArray[p2]
        translateSetting=null
        when {
            countryArray[p2]=="Chinese" -> {
                targetLangCode="zh"
            }
            countryArray[p2]=="Russian" -> {
                targetLangCode="ru"
            }
            countryArray[p2]=="Turkish" -> {
                targetLangCode="tr"
            }
            countryArray[p2]=="Portuguese" -> {
                targetLangCode="pt"
            }
            countryArray[p2]=="Japanese" -> {
                targetLangCode="ja"
            }
            countryArray[p2]=="German" -> {
                targetLangCode="de"
            }
            countryArray[p2]=="Italian" -> {
                targetLangCode="it"
            }
            countryArray[p2]=="English" -> {
                targetLangCode="en"
            }
            countryArray[p2]=="French" -> {
                targetLangCode="fr"
            }
            countryArray[p2]=="Arabic" -> {
                targetLangCode="ar"
            }
            countryArray[p2]=="Thai" -> {
                targetLangCode="th"
            }
            countryArray[p2]=="Spanish" -> {
                targetLangCode="es"
            }
        }
        translateSetting = MLRemoteTranslateSetting.Factory() // Set the source language code. The ISO 639-1 standard is used. This parameter is optional. If this parameter is not set, the system automatically detects the language.
            .setSourceLangCode(sourceLangCode) // Set the target language code. The ISO 639-1 standard is used.
            .setTargetLangCode(targetLangCode)
            .create()
        mlRemoteTranslator = MLTranslatorFactory.getInstance().getRemoteTranslator(translateSetting)
    }

    private fun translate(){
        val task = mlRemoteTranslator?.asyncTranslate(translate_edittext.text.toString())
        task?.addOnSuccessListener {
                text -> println("mcmcmc translation is ->$text")
            translation_text.text=text
        }
            ?.addOnFailureListener { e -> println("mcmcmc translation error is ->" + e.message) }
    }
}