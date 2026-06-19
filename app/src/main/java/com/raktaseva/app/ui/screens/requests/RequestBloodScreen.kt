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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.raktaseva.app.ui.theme.Dimens
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.raktaseva.app.data.api.GroqMessage
import com.raktaseva.app.data.api.GroqRepository
import com.raktaseva.app.data.model.BloodRequest
import com.raktaseva.app.ui.state.LocalUserState
import java.util.UUID
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestBloodScreen(onBack: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val hospitalFocusRequester = remember { FocusRequester() }
    val notesFocusRequester = remember { FocusRequester() }

    var bloodGroup by rememberSaveable { mutableStateOf("") }
    var bloodGroupExpanded by remember { mutableStateOf(false) }
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
    
    var hospital by rememberSaveable { mutableStateOf("") }
    var units by rememberSaveable { mutableStateOf("") }
    var unitsExpanded by remember { mutableStateOf(false) }
    val unitOptions = (1..10).map { it.toString() }
    
    var urgency by rememberSaveable { mutableStateOf("Medium") }
    var notes by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var isGeneratingAi by remember { mutableStateOf(false) }
    var showAiOverwriteDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val canSubmit = bloodGroup.isNotBlank() && hospital.trim().isNotBlank() && (units.toIntOrNull() ?: 0) > 0
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    fun triggerAiGeneration() {
        if (bloodGroup.isBlank()) {
            coroutineScope.launch { snackbarHostState.showSnackbar("Please select a blood group first.") }
            return
        }
        isGeneratingAi = true
        coroutineScope.launch {
            val toneDescription = when (urgency.lowercase()) {
                "low" -> "calm and informative. Explain the situation and request matching donors calmly."
                "medium" -> "encouraging and supportive. Motivate voluntary donors to help out."
                "high" -> "strong call to action. Emphasize the urgent need and prompt immediate response."
                "critical" -> "immediate life-saving appeal. Stress the extreme critical nature of the emergency and appeal for help immediately."
                else -> "encouraging and supportive."
            }

            val prompt = buildString {
                appendLine("Generate a natural, concise, human-sounding emergency blood request appeal paragraph of about 50-60 words.")
                appendLine("Integrate the following medical details naturally into the narrative:")
                appendLine("- Required Blood Group: $bloodGroup")
                if (hospital.trim().isNotEmpty()) {
                    appendLine("- Hospital: ${hospital.trim()}")
                }
                if (units.trim().isNotEmpty()) {
                    appendLine("- Units Required: $units")
                }
                appendLine("- Urgency level: $urgency")
                appendLine()
                appendLine("Instructions:")
                appendLine("1. Tone: The appeal tone must be $toneDescription")
                appendLine("2. Layout: Output ONLY a single, continuous paragraph. Do NOT use emojis, section headers, bullet points, numbered lists, line breaks, or hashtags.")
                appendLine("3. Format: Avoid repeating raw key-value pairs or duplicating labels like 'Patient Name:', 'Hospital:', 'Blood Group:', or 'Units:'. Instead, integrate these facts smoothly and naturally into human sentences.")
                appendLine("4. Output constraint: Do NOT include any conversational introduction, notes, greetings, or explanations. Only return the appeal text itself.")
            }

            val messages = listOf(
                GroqMessage(
                    role = "system",
                    content = "You are a medical emergency assistant. Generate a natural, cohesive appeal paragraph based on the inputs and tone requested. Do not include emojis, bullet points, headers, or any duplicate fields. Output ONLY the paragraph text itself without any introduction or markdown formatting."
                ),
                GroqMessage(role = "user", content = prompt)
            )

            val result = GroqRepository.chat(
                messages = messages,
                temperature = 0.7,
                maxTokens = 256
            )

            result.onSuccess { generatedText ->
                notes = generatedText.take(500)
            }.onFailure { error ->
                snackbarHostState.showSnackbar(error.message ?: "Failed to generate message. Please try again.")
            }

            isGeneratingAi = false
        }
    }

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
                .padding(horizontal = Dimens.screenHorizontal)
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
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(hospitalFocusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { notesFocusRequester.requestFocus() }),
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
                onValueChange = { if (it.length <= 500) notes = it },
                label = { Text("Additional Notes") },
                supportingText = { Text("${notes.length}/500") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .focusRequester(notesFocusRequester),
                maxLines = 5,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                shape = RoundedCornerShape(12.dp)
            )
            
            if (showAiOverwriteDialog) {
                AlertDialog(
                    onDismissRequest = { showAiOverwriteDialog = false },
                    title = { Text("Overwrite Notes?") },
                    text = { Text("You have typed some notes manually. Generating an AI message will overwrite your existing text. Do you want to proceed?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showAiOverwriteDialog = false
                                triggerAiGeneration()
                            }
                        ) {
                            Text("Overwrite", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showAiOverwriteDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            Surface(
                onClick = {
                    if (isGeneratingAi) return@Surface
                    if (bloodGroup.isBlank()) {
                        coroutineScope.launch { snackbarHostState.showSnackbar("Please select a blood group first.") }
                        return@Surface
                    }
                    if (notes.trim().isNotEmpty()) {
                        showAiOverwriteDialog = true
                    } else {
                        triggerAiGeneration()
                    }
                },
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
                    if (isGeneratingAi) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isGeneratingAi) "Generating..." else "Generate Urgent Message with AI",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
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
                            val requestMap = hashMapOf(
                                "requestId" to requestId,
                                "requesterUid" to LocalUserState.uid.value,
                                "requesterName" to LocalUserState.name.value,
                                "requesterEmail" to LocalUserState.email.value,
                                "bloodGroup" to bloodGroup,
                                "hospitalName" to hospital,
                                "hospitalLocation" to hospital,
                                "unitsRequired" to (units.toIntOrNull() ?: 0),
                                "urgencyLevel" to urgency,
                                "additionalNotes" to notes,
                                "requestStatus" to "active",
                                "status" to "pending",
                                "donorResponsesCount" to 0,
                                "responders" to emptyList<String>(),
                                "contactedDonors" to emptyList<String>(),
                                "completedDonors" to emptyList<String>(),
                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                            )
                            db.collection("requests").document(requestId).set(requestMap).await()
                            
                            isSubmitting = false
                            Toast.makeText(context, "Request posted successfully", Toast.LENGTH_SHORT).show()
                            onBack()
                        } catch (e: Exception) {
                            isSubmitting = false
                            Toast.makeText(context, "Failed: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight),
                enabled = canSubmit && !isSubmitting,
                shape = RoundedCornerShape(Dimens.buttonRadius),
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
