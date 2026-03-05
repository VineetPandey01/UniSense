package com.example.unisense

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BrailleTestScreen(
    speak: (String, (() -> Unit)?) -> Unit,
    listen: ((String) -> Unit) -> Unit,
    onFinish: () -> Unit
) {

    var score by remember { mutableStateOf(0) }
    var currentLetter by remember { mutableStateOf("A") }

    LaunchedEffect(Unit) {
        speak("Braille test started. What is the letter for pattern A?") {}
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("📝 Braille Test", style = MaterialTheme.typography.headlineMedium)
        Text("Say the letter for pattern: ⠁")

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            speak("What letter is this?") {
                listen { answer ->
                    if (answer == currentLetter.lowercase()) {
                        score++
                        speak("Correct!") {}
                    } else {
                        speak("Wrong answer") {}
                    }
                }
            }
        }) {
            Text("🎤 Answer")
        }

        Spacer(Modifier.height(16.dp))
        Text("Score: $score")

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            speak("Test complete") { onFinish() }
        }) {
            Text("Finish")
        }
    }
}
