package com.example.unisense

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(nav: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 48.dp, start = 24.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text("UniSense", style = MaterialTheme.typography.h4)

        Button(onClick = { nav.navigate("blind") }, modifier = Modifier.fillMaxWidth()) {
            Text("Blind Mode")
        }

        Button(onClick = { nav.navigate("deaf") }, modifier = Modifier.fillMaxWidth()) {
            Text("Deaf Mode")
        }
    }
}
