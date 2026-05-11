package com.raktaseva.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.raktaseva.app.ui.state.LocalUserState
import com.raktaseva.app.ui.state.DonationRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf(LocalUserState.name.value) }
    var age by remember { mutableStateOf(LocalUserState.age.value) }
    var bloodGroup by remember { mutableStateOf(LocalUserState.bloodGroup.value) }
    var lastDonationDate by remember { mutableStateOf(LocalUserState.lastDonationDate.value.filter { it.isDigit() }) }

    val isAgeValid = age.toIntOrNull()?.let { it >= 18 } ?: false
    val showAgeError = age.isNotEmpty() && !isAgeValid
    val canSave = name.isNotBlank() && isAgeValid

    val isEligible = LocalUserState.isEligibleToDonate()
    val daysUntilEligible = LocalUserState.getDaysUntilEligible()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        lastDonationDate = sdf.format(Date(it))
                    }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", fontWeight = FontWeight.Bold) },
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
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter(Char::isDigit).take(3) },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                isError = showAgeError,
                trailingIcon = {
                    if (isAgeValid) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid Age", tint = Color(0xFF4CAF50))
                    } else if (showAgeError) {
                        Icon(Icons.Default.Clear, contentDescription = "Invalid Age", tint = MaterialTheme.colorScheme.error)
                    }
                },
                supportingText = {
                    if (age.isNotEmpty()) {
                        if (isAgeValid) {
                            Text("18+ required for blood donation", color = Color(0xFF4CAF50))
                        } else {
                            Text("18+ required for blood donation", color = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        Text("18+ required for blood donation")
                    }
                }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Blood Group dropdown
            var expanded by remember { mutableStateOf(false) }
            val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Blood Group") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    bloodGroups.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                bloodGroup = selectionOption
                                expanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = lastDonationDate,
                onValueChange = { input -> 
                    val filtered = input.filter { it.isDigit() }
                    if (filtered.length <= 8) lastDonationDate = filtered
                },
                label = { Text("Last Donation Date") },
                leadingIcon = { 
                    IconButton(onClick = { showDatePicker = true }, enabled = isEligible) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date") 
                    }
                },
                enabled = isEligible,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = com.raktaseva.app.ui.screens.auth.DateTransformation(),
                supportingText = {
                    if (!isEligible) {
                        Text("Not Eligible. Wait $daysUntilEligible days to update.", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Update your last donation date.")
                    }
                }
            )
            Spacer(modifier = Modifier.height(32.dp))

            var isSaving by remember { mutableStateOf(false) }

            Button(
                onClick = {
                    isSaving = true
                    val formattedToSave = buildString {
                        val trimmed = lastDonationDate.take(8)
                        for (i in trimmed.indices) {
                            append(trimmed[i])
                            if (i == 1 || i == 3) append("/")
                        }
                    }
                    val uid = LocalUserState.uid.value
                    if (uid.isNotEmpty()) {
                        val updates = mapOf(
                            "fullName" to name,
                            "age" to age,
                            "bloodGroup" to bloodGroup,
                            "lastDonationDate" to formattedToSave
                        )
                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                            .update(updates)
                            .addOnSuccessListener {
                                isSaving = false
                                LocalUserState.name.value = name
                                LocalUserState.age.value = age
                                LocalUserState.bloodGroup.value = bloodGroup
                                
                                if (formattedToSave != LocalUserState.lastDonationDate.value && formattedToSave.isNotBlank()) {
                                    LocalUserState.donationHistory.add(0, DonationRecord(formattedToSave, "Self Updated", "Completed"))
                                }
                                LocalUserState.lastDonationDate.value = formattedToSave
                                
                                onBack()
                            }
                            .addOnFailureListener { _ ->
                                isSaving = false
                                LocalUserState.name.value = name
                                LocalUserState.age.value = age
                                LocalUserState.bloodGroup.value = bloodGroup
                                
                                if (formattedToSave != LocalUserState.lastDonationDate.value && formattedToSave.isNotBlank()) {
                                    LocalUserState.donationHistory.add(0, DonationRecord(formattedToSave, "Self Updated", "Completed"))
                                }
                                LocalUserState.lastDonationDate.value = formattedToSave
                                
                                onBack()
                            }
                    } else {
                        isSaving = false
                        LocalUserState.name.value = name
                        LocalUserState.age.value = age
                        LocalUserState.bloodGroup.value = bloodGroup
                        
                        if (formattedToSave != LocalUserState.lastDonationDate.value && formattedToSave.isNotBlank()) {
                            LocalUserState.donationHistory.add(0, DonationRecord(formattedToSave, "Self Updated", "Completed"))
                        }
                        LocalUserState.lastDonationDate.value = formattedToSave
                        
                        onBack()
                    }
                },
                enabled = canSave && !isSaving,
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                if (isSaving) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Save Changes")
                }
            }
        }
    }
}
