package com.raktaseva.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OtpScreen(
    phone: String,
    onVerified: () -> Unit,
    onBack: () -> Unit
) {
    var otp by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verification") },
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
                "Verify OTP",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Enter the 6-digit code sent to +91 $phone",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            OutlinedTextField(
                value = otp,
                onValueChange = { otp = it.filter(Char::isDigit).take(6) },
                label = { Text("OTP Code") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                placeholder = { Text("******") }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = onVerified,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = otp.length == 6
            ) {
                Text("Verify & Continue", fontSize = 16.sp)
            }
            
            TextButton(
                onClick = { /* Resend logic */ },
                modifier = Modifier.align(androidx.compose.ui.Alignment.CenterHorizontally).padding(top = 16.dp)
            ) {
                Text("Resend Code")
            }
        }
    }
}
