package com.example.unisense



import android.speech.tts.TextToSpeech
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BlindVoiceScreen(tts: TextToSpeech) {

    LaunchedEffect(Unit) {
        tts.speak(
            "You are now in blind assistance mode. Say read text, sos, or help.",
            TextToSpeech.QUEUE_FLUSH,
            null,
            null
        )
    }

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text("Blind Mode — Voice Commands Active")
            Text("Say: Read Text • SOS • Help")
        }
    }
}
