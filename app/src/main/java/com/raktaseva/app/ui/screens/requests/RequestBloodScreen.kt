package com.raktaseva.app.ui.screens.requests

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBloodScreen(onBack: () -> Unit) {
    var bloodGroup by remember { mutableStateOf("") }
    var hospital by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("") }
    var urgency by remember { mutableStateOf("Medium") }
    var notes by remember { mutableStateOf("") }
    val canSubmit = bloodGroup.isNotBlank() && hospital.isNotBlank() && (units.toIntOrNull() ?: 0) > 0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Request Blood", style = MaterialTheme.typography.titleLarge) },
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
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Emergency Details", style = MaterialTheme.typography.headlineMedium)
            
            OutlinedTextField(
                value = bloodGroup, 
                onValueChange = { bloodGroup = it }, 
                label = { Text("Required Blood Group") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = hospital, 
                onValueChange = { hospital = it }, 
                label = { Text("Hospital Name & Location") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            OutlinedTextField(
                value = units, 
                onValueChange = { units = it.filter(Char::isDigit).take(2) },
                label = { Text("Units Required") }, 
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                shape = RoundedCornerShape(12.dp)
            )
            
            Column {
                Text("Urgency Level", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    listOf("Low", "Medium", "High", "Critical").forEach { level ->
                        FilterChip(
                            selected = urgency == level,
                            onClick = { urgency = level },
                            label = { Text(level) },
                            modifier = Modifier.padding(end = 8.dp),
                            shape = RoundedCornerShape(100.dp)
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Additional Notes") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )
            
            Surface(
                onClick = { /* AI Generator logic */ },
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFE3F2FD),
                contentColor = Color(0xFF1976D2),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Urgent Message with AI", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = { /* Submit logic */ },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = canSubmit,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Post Emergency Request", style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
