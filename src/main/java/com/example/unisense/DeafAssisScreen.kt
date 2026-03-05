package com.example.unisense

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun DeafAssistanceScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text("Deaf Assistance Mode", style = MaterialTheme.typography.h5)
        Text("Visual features coming soon")
    }
}
