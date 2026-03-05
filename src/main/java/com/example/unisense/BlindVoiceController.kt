package com.example.unisense

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class BlindVoiceController(
    val activity: Activity,   // Public on purpose

    private val speak: (String, (() -> Unit)?) -> Unit,
    private val goHome: () -> Unit,
    private val goDeafMode: () -> Unit,
    private val goBrailleMenu: () -> Unit,
    private val triggerSOS: () -> Unit
) {

    private val recognizer = SpeechRecognizer.createSpeechRecognizer(activity)

    // ----------------------------------------------------
    // DEFAULT LISTENING MODE (for blind navigation)
    // ----------------------------------------------------
    fun startListening() {
        startRecognizer { result ->
            handleCommand(result?.lowercase(Locale.getDefault()))
        }
    }

    // ----------------------------------------------------
    // SPECIAL LISTEN MODE (Braille lessons, tests, etc.)
    // Returns result to caller instead of acting on it
    // ----------------------------------------------------
    fun listen(callback: (String) -> Unit) {
        startRecognizer { result ->
            if (result != null) callback(result.lowercase(Locale.getDefault()))
            else speak("Please speak again.") { listen(callback) }
        }
    }

    // ----------------------------------------------------
    // Shared recognizer implementation
    // ----------------------------------------------------
    private fun startRecognizer(onResult: (String?) -> Unit) {

        val intent = android.content.Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        recognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}

            override fun onError(error: Int) {
                speak("I didn't catch that. Please say again.") {
                    startRecognizer(onResult)
                }
            }

            override fun onResults(results: Bundle?) {
                val command = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()

                onResult(command)
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        recognizer.startListening(intent)
    }

    // ----------------------------------------------------
    // Blind Mode Command Handling
    // ----------------------------------------------------
    private fun handleCommand(cmd: String?) {
        when {
            cmd == null -> speak("Please repeat.") { startListening() }

            cmd.contains("home") ->
                speak("Opening home.") { goHome() }

            cmd.contains("deaf") ->
                speak("Switching to deaf mode.") { goDeafMode() }

            cmd.contains("braille") ||
                    cmd.contains("learn") ->
                speak("Opening braille learning.") { goBrailleMenu() }

            cmd.contains("help") ||
                    cmd.contains("sos") ->
                performSOS()

            else ->
                speak("Say home, deaf, braille or sos.") { startListening() }
        }
    }

    // ----------------------------------------------------
    // SOS vibration + callback
    // ----------------------------------------------------
    @SuppressLint("MissingPermission", "ServiceCast", "DEPRECATION")
    private fun performSOS() {

        val vibrator = activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        val pattern = longArrayOf(0, 700, 300, 700, 300, 700)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        else
            vibrator.vibrate(pattern, -1)

        speak("SOS activated. Help is being contacted.") {
            triggerSOS()
            startListening()
        }
    }

    fun stopListening() {
        recognizer.stopListening()
        recognizer.destroy()
    }
}
