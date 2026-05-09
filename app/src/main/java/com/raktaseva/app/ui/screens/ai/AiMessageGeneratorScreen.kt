package com.raktaseva.app.ui.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiMessageGeneratorScreen(onBack: () -> Unit) {
    var generatedMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI Message Generator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
        ) {
            Text(
                "Need to send a quick emergency message?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Our AI will craft a professional and urgent request based on your emergency details.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (generatedMessage.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    if (isLoading) {
                        CircularProgressIndicator()
                    } else {
                        Button(onClick = { 
                            isLoading = true
                            // Mocking AI response
                            generatedMessage = "URGENT BLOOD REQUIREMENT!\n\nPatient at City Hospital needs O+ blood immediately. Units needed: 2. Please contact +91 9876543210 if you can donate or help spread the word. Every second counts! #BloodDonation #RescueLife"
                            isLoading = false
                        }) {
                            Text("Generate Message")
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(generatedMessage, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { /* Copy logic */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Copy")
                    }
                    Button(
                        onClick = { /* Share logic */ },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Share")
                    }
                }
                
                TextButton(
                    onClick = { generatedMessage = "" },
                    modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally).padding(top = 8.dp)
                ) {
                    Text("Regenerate")
                }
            }
        }
    }
}
