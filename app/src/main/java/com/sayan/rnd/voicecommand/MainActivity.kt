package com.sayan.rnd.voicecommand

import android.Manifest
import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.widget.Toast
import android.content.ActivityNotFoundException
import android.speech.RecognizerIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.View
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.SUCCESS
import android.speech.tts.UtteranceProgressListener
import android.media.AudioManager
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {
    //SpeechRecognizer property
    private lateinit var mSpeechRecognizer: SpeechRecognizer
    private lateinit var textToSpeech: TextToSpeech

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        /**
         * SpeechRecognizer related
         */
        initializeSpeechRecognizer()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::textToSpeech.isInitialized){
            textToSpeech.stop()
            textToSpeech.shutdown()
        }
    }

    /**
     * Button onClick
     */
    fun getVoiceInput(view: View) {
        //use speech recognizer
        openSpeechRecognizer()

        /*
        //use google speech input
        promptSpeechInput()
        */
    }

    /**
     * SpeechRecognizer related
     */
    private fun initializeSpeechRecognizer() {
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        //RECORD_AUDIO permission dynamic checking
        checkPermission()
        mSpeechRecognizer.setRecognitionListener(object : SpeechRecognitionListener(this) {

            override fun onResults(results: Bundle?) {
                this.dialog.dismiss()
                //getting all the matches
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)

                //displaying the first match
                if (matches != null) {
                    val text = matches[0]
                    speechText.text = text
                    val checkIntent = Intent()
                    checkIntent.action = TextToSpeech.Engine.ACTION_CHECK_TTS_DATA
                    startActivityForResult(checkIntent, 1234)
                }
            }
        })
    }

    /**
     * SpeechRecognizer related
     */
    private fun checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + packageName))
                startActivity(intent)
                finish()
            }
        }
    }

    /**
     * SpeechRecognizer related
     */
    private fun openSpeechRecognizer() {
        val mSpeechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mSpeechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        mSpeechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )
        mSpeechRecognizer.startListening(mSpeechRecognizerIntent)
    }


    /**
     * google speech input related
     */
    private fun promptSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            getString(R.string.speech_prompt)
        )
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT)
        } catch (a: ActivityNotFoundException) {
            Toast.makeText(
                applicationContext,
                getString(R.string.speech_not_supported),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun speakNow(speakText: String) {
        if (::textToSpeech.isInitialized) {
            textToSpeech = TextToSpeech(this@MainActivity,
                TextToSpeech.OnInitListener { status ->
                    if (status == SUCCESS) {
                        println("TextToSpeech successfully initiated")
                        textToSpeech.language = Locale.US
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            val bundle = Bundle()
                            bundle.putInt(TextToSpeech.Engine.KEY_PARAM_STREAM, AudioManager.STREAM_MUSIC)
                            val speakResult = textToSpeech.speak(speakText, TextToSpeech.QUEUE_FLUSH, bundle, "123")
                            if (speakResult == SUCCESS) {
                                println("successfully spoke")
                            } else {
                                println("could not speak")
                            }
                            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                                override fun onDone(utteranceId: String?) {
                                    if (utteranceId.equals("123"))
                                        println("textToSpeech DONE")
                                }

                                override fun onError(utteranceId: String?) {
                                    if (utteranceId.equals("123"))
                                        println("textToSpeech error")
                                }

                                override fun onStart(utteranceId: String?) {
                                    if (utteranceId.equals("123"))
                                        println("started To Speech")
                                }

                            })
                        } else {
                            val params: HashMap<String, String> = HashMap()
                            params[TextToSpeech.Engine.KEY_PARAM_STREAM] = AudioManager.STREAM_MUSIC.toString()
                            textToSpeech.speak(speakText, TextToSpeech.QUEUE_ADD, params)
                        }
                    } else {
                        println("TextToSpeech initiation failed")
                    }
                })
        }
    }

    /**
     *
     * google speech input related
     * Receiving speech input
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_CODE_SPEECH_INPUT -> {
                if (resultCode == Activity.RESULT_OK && null != data) {
                    val result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    speechText.text = result[0]
                }
            }
            1234 -> {
                if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                    // success, create the TTS instance
                    println("TTS supported")
                    speakNow(speechText.text.toString())
                } else {
                    println("TTS not supported")
                    // missing data, install it
                    val installIntent: Intent = Intent()
                    installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                    startActivity(installIntent)
                }
            }
        }
    }
}
