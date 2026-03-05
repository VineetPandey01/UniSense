package com.example.unisense

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BlindAssistanceScreen(
    speak: (String, (() -> Unit)?) -> Unit,
    onHome: () -> Unit,
    onBrailleMenu: () -> Unit,
    onTest: () -> Unit,
    onLesson: () -> Unit,
    startListening: () -> Unit
) {

    LaunchedEffect(Unit) {
        speak(
            "Blind assistance mode active. " +
                    "Say home, learn braille, braille test, or SOS."
        ) {
            startListening()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            "Blind Assistance Mode",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(20.dp))

        Button(onClick = onLesson, modifier = Modifier.fillMaxWidth()) {
            Text("📚 Learn Braille Letters")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onTest, modifier = Modifier.fillMaxWidth()) {
            Text("🧠 Braille Test")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onBrailleMenu, modifier = Modifier.fillMaxWidth()) {
            Text("🔤 Braille Menu")
        }

        Spacer(Modifier.height(12.dp))

        Button(onClick = onHome, modifier = Modifier.fillMaxWidth()) {
            Text("🏠 Back to Home")
        }
    }
}

