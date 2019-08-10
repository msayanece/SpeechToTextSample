package com.sayan.rnd.voicecommand

import android.content.Context
import android.os.Bundle
import android.speech.RecognitionListener
import androidx.appcompat.app.AlertDialog



abstract class SpeechRecognitionListener(private val context: Context) : RecognitionListener {
    lateinit var dialog: AlertDialog

    override fun onReadyForSpeech(params: Bundle?) {
        //show dialog that we are listening
        dialog = AlertDialog.Builder(context)
            .setTitle(R.string.ready_to_speak_title)
            .setMessage(R.string.ready_to_speak)
//            .setPositiveButton(R.string.retry_text) { dialog, which ->
//
//            }
//            // A null listener allows the button to dismiss the dialog and take no further action.
//            .setNegativeButton(android.R.string.cancel, null)
            .setIcon(R.drawable.ic_mic_black_24dp)
            .show()
    }

    override fun onRmsChanged(rmsdB: Float) {
    }

    override fun onBufferReceived(buffer: ByteArray?) {
    }

    override fun onPartialResults(partialResults: Bundle?) {
    }

    override fun onEvent(eventType: Int, params: Bundle?) {
    }

    override fun onBeginningOfSpeech() {
    }

    override fun onEndOfSpeech() {
    }

    override fun onError(error: Int) {
    }

    //abstract for subclass to implement
    abstract override fun onResults(results: Bundle?)
}