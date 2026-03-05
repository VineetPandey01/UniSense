package com.example.unisense

import kotlin.math.sqrt
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.telephony.SmsManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import java.util.*

class MainActivity : ComponentActivity() {

    lateinit var navController: NavHostController

    private lateinit var sensorManager: SensorManager
    private var accel = 0f
    private var accelCurrent = 0f
    private var accelLast = 0f

    private var tts: TextToSpeech? = null
    private var recognizer: SpeechRecognizer? = null

    private var blindModeActive = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestMicPermission()
        requestSmsPermission()
        setupShakeSensor()

        tts = TextToSpeech(this) {
            if (it == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US

                BlindVoiceController.attach(this, tts!!)
                speak("Hello. Are you blind or deaf?") {
                    startListeningForMode()
                }
            }
        }

        setContent {
            navController = rememberNavController()
            MaterialTheme { AppNavHost(navController) }
        }
    }

    // ---------------- NAVIGATION ----------------
    @Composable
    private fun AppNavHost(nav: NavHostController) {

        NavHost(nav, startDestination = "home") {

            composable("home") { HomeScreen(nav) }

            composable("blind") {
                blindModeActive = true
                BlindVoiceController.startListeningLoop()

                BlindAssistanceScreen(
                    speak = ::speak,
                    onHome = { navController.navigate("home") },
                    onBrailleMenu = { navController.navigate("brailleMenu") },
                    onLesson = { navController.navigate("brailleLesson") },
                    onTest = { navController.navigate("brailleTest") },
                    startListening = { BlindVoiceController.startListeningLoop() }
                )
            }

            composable("deaf") {
                blindModeActive = false
                DeafAssistanceScreen()
            }

            composable("brailleMenu") {
                blindModeActive = true
                BrailleMenuScreen(nav)
                BlindVoiceController.startListeningLoop()
            }

            composable("brailleLesson") {
                blindModeActive = true
                BrailleLessonScreen(
                    speak = ::speak,
                    listen = ::startListening,
                    onFinish = { navController.navigate("brailleMenu") }
                )
            }

            composable("brailleTest") {
                blindModeActive = true
                BrailleTestScreen(
                    speak = ::speak,
                    listen = ::startListening,
                    onFinish = { nav.navigate("brailleMenu") }
                )
            }
        }
    }

    // ---------------- SPEAK ----------------
    fun speak(text: String, after: (() -> Unit)? = null) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "APP_TTS")

        Handler(Looper.getMainLooper()).postDelayed({
            after?.invoke()
        }, 4000)
    }

    // ---------------- MODE SELECTION ----------------
    private fun startListeningForMode() {
        startListening { cmd ->
            when (cmd) {
                "blind" -> speak("Blind mode activated") {
                    blindModeActive = true
                    navController.navigate("blind")
                }
                "deaf" -> speak("Deaf mode activated") {
                    blindModeActive = false
                    navController.navigate("deaf")
                }
                else -> speak("Please say blind or deaf") {
                    startListeningForMode()
                }
            }
        }
    }

    // ---------------- SPEECH LISTENER ----------------
    fun startListening(onResult: (String) -> Unit) {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) return
        if (recognizer == null)
            recognizer = SpeechRecognizer.createSpeechRecognizer(this)

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        }

        recognizer?.setRecognitionListener(object : RecognitionListener {

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.lowercase(Locale.getDefault())

                if (text != null) onResult(text)
                else startListening(onResult)
            }

            override fun onError(error: Int) {
                speak("Please repeat.") { startListening(onResult) }
            }

            override fun onReadyForSpeech(p: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(r: Float) {}
            override fun onBufferReceived(b: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onPartialResults(p: Bundle?) {}
            override fun onEvent(t: Int, p: Bundle?) {}
        })

        recognizer?.startListening(intent)
    }

    // ---------------- PERMISSIONS ----------------
    private fun requestMicPermission() {
        val launcher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            if (!it)
                Toast.makeText(this, "Microphone permission required", Toast.LENGTH_LONG).show()
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) launcher.launch(Manifest.permission.RECORD_AUDIO)
    }

    private fun requestSmsPermission() {
        requestPermissions(arrayOf(Manifest.permission.SEND_SMS), 101)
    }

    // ---------------- SHAKE SOS ----------------
    private fun setupShakeSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        accel = 10f
        accelCurrent = SensorManager.GRAVITY_EARTH
        accelLast = SensorManager.GRAVITY_EARTH

        sensorManager.registerListener(
            shakeListener,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_NORMAL
        )
    }

    private val shakeListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            accelLast = accelCurrent
            accelCurrent = sqrt((x * x + y * y + z * z))
            val delta = accelCurrent - accelLast
            accel = accel * 0.9f + delta

            if (accel > 15 && blindModeActive) {
                BlindVoiceController.triggerSOS()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    // ---------------- SEND SMS ----------------
    fun sendEmergencySMS() {
        val phone = "918858735150"
        val message = "🚨 SOS Alert from UniSense. I need help!"

        try {
            SmsManager.getDefault()
                .sendTextMessage(phone, null, message, null, null)

            BlindVoiceController.speak("Emergency message sent.")
        } catch (_: Exception) {
            BlindVoiceController.speak("Failed to send emergency message.")
        }
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(shakeListener)
        recognizer?.destroy()
        tts?.shutdown()
        super.onDestroy()
    }

    // ---------------- BLIND VOICE CONTROLLER ----------------
    object BlindVoiceController {

        private var tts: TextToSpeech? = null
        private lateinit var activity: MainActivity
        private var loopStarted = false
        private var isSpeaking = false

        fun attach(a: MainActivity, t: TextToSpeech) {
            activity = a
            tts = t
        }

        fun startListeningLoop() {
            if (loopStarted) return
            loopStarted = true

            speak("Blind mode active. Say home, sos, exit or learn braille.") {
                listen()
            }
        }

        private fun listen() {
            if (isSpeaking) return
            activity.startListening { handleCommand(it) }
        }

        private fun handleCommand(cmd: String) {
            when (cmd) {

                "home" -> speak("Returning to home.") {
                    loopStarted = false
                    activity.navController.navigate("home")
                }

                "help" -> speak("You can say home, sos, exit or learn braille.") {
                    listen()
                }

                "sos" -> triggerSOS()

                "exit" -> speak("Exiting blind mode.") {
                    loopStarted = false
                }

                "learn" -> speak("Opening Braille learning section.") {
                    activity.navController.navigate("brailleMenu")
                }

                else -> speak("Unknown command. Please repeat.") {
                    listen()
                }
            }
        }

        @SuppressLint("MissingPermission", "DEPRECATION")
        fun triggerSOS() {

            val vib =
                activity.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

            val pattern = longArrayOf(0, 600, 300, 600, 300, 600)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                vib.vibrate(VibrationEffect.createWaveform(pattern, -1))
            else vib.vibrate(pattern, -1)

            speak("SOS activated. Sending emergency message.") {
                activity.sendEmergencySMS()
                listen()
            }
        }

        fun speak(text: String, after: (() -> Unit)? = null) {
            isSpeaking = true
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "APP_TTS")
            Handler(Looper.getMainLooper()).postDelayed({
                isSpeaking = false
                after?.invoke()
            }, 4000)
        }
    }
}
