package com.jassin.customdrome

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jassin.customdrome.ui.theme.CustomDromeTheme

@Composable
fun LoginScreen(onBack: () -> Unit) {
    CustomDromeTheme {
        // responsible for the themed bg color
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background,
        ) {
            // prevents overlap with the status bar
            Box(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    var instanceUrl by remember { mutableStateOf("") }

                    OutlinedTextField(
                        value = instanceUrl,
                        onValueChange = { instanceUrl = it },
                        // This is the text that floats to the top corner
                        label = { Text("Instance URL") },
                        // This only appears once you click and the label moves up
                        placeholder = { Text("https://example.com") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        leadingIcon = {
                            Icon(Icons.Default.Dns, contentDescription = null)
                        },
                    )

                    Text(
                        text = "Login to CustomDrome",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.headlineMedium,
                    )

                    Button(onClick = { onBack() }) {
                        // Trigger the back action
                        Text("Go Back")
                    }

                    Button(onClick = { /* logic goes here */ }) {
                        Text("Login")
                    }
                }
            }
        }
    }
}
