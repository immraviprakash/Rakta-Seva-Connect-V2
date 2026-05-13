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
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.app.data.model.BloodRequest
import com.raktaseva.app.ui.state.LocalUserState
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBloodScreen(onBack: () -> Unit) {
    var bloodGroup by remember { mutableStateOf("") }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    
    var hospital by remember { mutableStateOf("") }
    var units by remember { mutableStateOf("") }
    var unitsExpanded by remember { mutableStateOf(false) }
    val unitOptions = (1..10).map { it.toString() }
    
    var urgency by remember { mutableStateOf("Medium") }
    var notes by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val canSubmit = bloodGroup.isNotBlank() && hospital.trim().isNotBlank() && (units.toIntOrNull() ?: 0) > 0
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
            
            ExposedDropdownMenuBox(
                expanded = bloodGroupExpanded,
                onExpandedChange = { bloodGroupExpanded = !bloodGroupExpanded }
            ) {
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Required Blood Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = bloodGroupExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = bloodGroupExpanded,
                    onDismissRequest = { bloodGroupExpanded = false }
                ) {
                    bloodGroups.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                bloodGroup = selectionOption
                                bloodGroupExpanded = false
                            }
                        )
                    }
                }
            }
            
            OutlinedTextField(
                value = hospital, 
                onValueChange = { hospital = it }, 
                label = { Text("Hospital Name & Location") }, 
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            ExposedDropdownMenuBox(
                expanded = unitsExpanded,
                onExpandedChange = { unitsExpanded = !unitsExpanded }
            ) {
                OutlinedTextField(
                    value = units,
                    onValueChange = { },
                    readOnly = true,
                    label = { Text("Units Required") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitsExpanded) },
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                    modifier = Modifier.fillMaxWidth().menuAnchor(),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = unitsExpanded,
                    onDismissRequest = { unitsExpanded = false }
                ) {
                    unitOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                units = option
                                unitsExpanded = false
                            }
                        )
                    }
                }
            }
            
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
                onValueChange = { if (it.length <= 200) notes = it },
                label = { Text("Additional Notes") },
                supportingText = { Text("${notes.length}/200") },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp)
            )
            
            Surface(
                onClick = { /* AI Generator logic */ },
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
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
                onClick = { 
                    isSubmitting = true
                    coroutineScope.launch {
                        try {
                            val db = FirebaseFirestore.getInstance()
                            
                            // Check for existing active requests
                            val activeRequests = db.collection("requests")
                                .whereEqualTo("requesterUid", LocalUserState.uid.value)
                                .whereEqualTo("requestStatus", "active")
                                .get()
                                .await()

                            if (!activeRequests.isEmpty) {
                                isSubmitting = false
                                snackbarHostState.showSnackbar("You already have an active emergency request.")
                                return@launch
                            }

                            val requestId = UUID.randomUUID().toString()
                            val request = BloodRequest(
                                requestId = requestId,
                                requesterUid = LocalUserState.uid.value,
                                requesterName = LocalUserState.name.value,
                                requesterEmail = LocalUserState.email.value,
                                bloodGroup = bloodGroup,
                                hospitalName = hospital,
                                hospitalLocation = hospital,
                                unitsRequired = units.toIntOrNull() ?: 0,
                                urgencyLevel = urgency,
                                additionalNotes = notes,
                                requestStatus = "active"
                            )
                            db.collection("requests").document(requestId).set(request).await()
                            
                            isSubmitting = false
                            Toast.makeText(context, "Request posted successfully", Toast.LENGTH_SHORT).show()
                            onBack()
                        } catch (e: Exception) {
                            isSubmitting = false
                            Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(60.dp),
                enabled = canSubmit && !isSubmitting,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Post Emergency Request", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary)
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
