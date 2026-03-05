import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.unisense.MainActivity

@Composable
fun SOSScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Emergency SOS", style = MaterialTheme.typography.h4)

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { MainActivity.BlindVoiceController.triggerSOS() }
        ) {
            Text("🚨 Trigger SOS")
        }

        Spacer(Modifier.height(24.dp))

        Button(onClick = onBack) {
            Text("Back")
        }
    }
}
