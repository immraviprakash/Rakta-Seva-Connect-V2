package com.raktaseva.app.ui.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    var step by remember { mutableStateOf(1) }
    
    // Step 1: Basic Info
    var name by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    
    // Step 2: Medical Info
    var bloodGroup by remember { mutableStateOf("") }
    var lastDonationDate by remember { mutableStateOf("") }
    
    // Step 3: Intent
    var intent by remember { mutableStateOf("") }
    val canContinue = when (step) {
        1 -> name.isNotBlank() && age.isNotBlank() && gender.isNotBlank()
        2 -> bloodGroup.isNotBlank() && lastDonationDate.isNotBlank()
        else -> intent.isNotBlank()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registration") },
                navigationIcon = {
                    IconButton(onClick = { if (step > 1) step-- else onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { if (step < 3) step++ else onComplete() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
                    .height(56.dp),
                enabled = canContinue
            ) {
                Text(if (step < 3) "Next" else "Finish")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            LinearProgressIndicator(
                progress = step / 3f,
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )
            
            when (step) {
                1 -> {
                    Text("Basic Information", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter(Char::isDigit).take(3) },
                        label = { Text("Age") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = gender, onValueChange = { gender = it }, label = { Text("Gender") }, modifier = Modifier.fillMaxWidth())
                }
                2 -> {
                    Text("Medical Details", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(value = bloodGroup, onValueChange = { bloodGroup = it }, label = { Text("Blood Group") }, modifier = Modifier.fillMaxWidth())
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(value = lastDonationDate, onValueChange = { lastDonationDate = it }, label = { Text("Last Donation Date") }, modifier = Modifier.fillMaxWidth())
                }
                3 -> {
                    Text("User Intent", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("How would you like to use the platform?")
                    Spacer(modifier = Modifier.height(16.dp))
                    RadioButtonOption("Donate Only", intent == "Donate") { intent = "Donate" }
                    RadioButtonOption("Receive Only", intent == "Receive") { intent = "Receive" }
                    RadioButtonOption("Both", intent == "Both") { intent = "Both" }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
fun RadioButtonOption(label: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label)
        }
    }
}
