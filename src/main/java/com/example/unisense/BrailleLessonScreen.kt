package com.example.unisense

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * listen = function that starts speech recognition and
 * returns the recognized text through callback
 *
 * Example type:
 *      listen { result -> ... }
 */
private fun startLessonVoiceLoop(
    speak: (String, (() -> Unit)?) -> Unit,
    listen: ((String) -> Unit) -> Unit,
    onNext: () -> Unit
) {
    listen { cmd ->

        val command = cmd.lowercase()

        when {
            command.contains("next") ->
                speak("Moving to next letter") { onNext() }

            command.contains("repeat") ->
                speak("Repeating current letter") {
                    startLessonVoiceLoop(speak, listen, onNext)
                }

            command.contains("exit") ->
                speak("Exiting Braille lesson") {}

            else ->
                speak("Say next, repeat, or exit") {
                    startLessonVoiceLoop(speak, listen, onNext)
                }
        }
    }
}

@Composable
fun BrailleLessonScreen(
    speak: (String, (() -> Unit)?) -> Unit,
    listen: (((String) -> Unit)) -> Unit,
    onFinish: () -> Unit
) {
    var index by remember { mutableStateOf(0) }

    val letters = listOf("A","B","C","D","E")
    val patterns = listOf("⠁","⠃","⠉","⠙","⠑")

    val letter = letters[index]
    val pattern = patterns[index]

    // 🔊 Start lesson automatically with voice guidance
    LaunchedEffect(index) {
        speak("Letter $letter. Braille pattern $pattern. Say next, repeat or exit.") {
            startLessonVoiceLoop(
                speak = speak,
                listen = listen,
                onNext = {
                    index++
                    if (index >= letters.size)
                        onFinish()
                }
            )
        }
    }

    // 👁️ Visual UI (for presenters or non-blind users)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("Learning Letter: $letter", style = MaterialTheme.typography.headlineMedium)
        Text("Braille Pattern: $pattern", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.height(24.dp))

        Button(onClick = {
            speak("Letter $letter. Pattern $pattern") {}
        }) { Text("🔊 Speak Again") }

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            index++
            if (index >= letters.size) onFinish()
        }) { Text("➡ Next Letter") }

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            onFinish()
        }) { Text("⏹ Exit Lesson") }

        Spacer(Modifier.height(18.dp))

        Text("Voice Commands: next • repeat • exit")
    }
}
