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
import com.raktaseva.app.ui.theme.Dimens
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(onBack: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val nameFocusRequester = remember { FocusRequester() }
    val ageFocusRequester = remember { FocusRequester() }
    val lastDonationFocusRequester = remember { FocusRequester() }

    var name by remember { mutableStateOf(LocalUserState.name.value) }
    var age by remember { mutableStateOf(LocalUserState.age.value) }
    var bloodGroup by remember { mutableStateOf(LocalUserState.bloodGroup.value) }
    var lastDonationDate by remember { mutableStateOf(LocalUserState.lastDonationDate.value.filter { it.isDigit() }) }

    val isAgeValid = age.toIntOrNull()?.let { it >= 18 } ?: false
    val showAgeError = age.isNotEmpty() && !isAgeValid
    val isDateValid = lastDonationDate.isEmpty() || (lastDonationDate.length == 8 && com.raktaseva.app.ui.state.LocalUserState.tryParseDate(lastDonationDate) != null)
    val showDateError = lastDonationDate.isNotEmpty() && (lastDonationDate.length < 8 || com.raktaseva.app.ui.state.LocalUserState.tryParseDate(lastDonationDate) == null)
    val canSave = name.isNotBlank() && isAgeValid && isDateValid

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
                        val sdf = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
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
                .padding(Dimens.screenHorizontal)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(nameFocusRequester),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { ageFocusRequester.requestFocus() })
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = age,
                onValueChange = { age = it.filter(Char::isDigit).take(3) },
                label = { Text("Age") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { lastDonationFocusRequester.requestFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(ageFocusRequester),
                isError = showAgeError,
                trailingIcon = {
                    if (isAgeValid) {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Valid Age", tint = MaterialTheme.colorScheme.tertiary)
                    } else if (showAgeError) {
                        Icon(Icons.Default.Clear, contentDescription = "Invalid Age", tint = MaterialTheme.colorScheme.error)
                    }
                },
                supportingText = {
                    if (age.isNotEmpty()) {
                        if (isAgeValid) {
                            Text("18+ required for blood donation", color = MaterialTheme.colorScheme.tertiary)
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
                label = { Text("Last Donation Date (Optional)") },
                leadingIcon = { 
                    IconButton(onClick = { showDatePicker = true }, enabled = isEligible) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date") 
                    }
                },
                enabled = isEligible,
                isError = showDateError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(lastDonationFocusRequester),
                visualTransformation = com.raktaseva.app.ui.screens.auth.DateTransformation(),
                supportingText = {
                    if (!isEligible) {
                        Text("Not Eligible. Wait $daysUntilEligible days to update.", color = MaterialTheme.colorScheme.error)
                    } else if (showDateError) {
                        Text("Enter a valid complete date (DD/MM/YYYY) or clear it", color = MaterialTheme.colorScheme.error)
                    } else {
                        Text("Update your last donation date (DD/MM/YYYY).")
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
                    
                    val parsedDate = com.raktaseva.app.ui.state.LocalUserState.tryParseDate(formattedToSave)
                    val isNewDateEligible = if (parsedDate == null) {
                        true
                    } else {
                        if (parsedDate.after(java.util.Date())) {
                            false
                        } else {
                            val diffInMillies = java.util.Date().time - parsedDate.time
                            val days = java.util.concurrent.TimeUnit.DAYS.convert(diffInMillies, java.util.concurrent.TimeUnit.MILLISECONDS)
                            days >= 90
                        }
                    }
                    val updatedIsAvailable = if (!isNewDateEligible) false else LocalUserState.isAvailable.value

                    val uid = LocalUserState.uid.value
                    if (uid.isNotEmpty()) {
                        val updates = mapOf(
                            "fullName" to name,
                            "age" to age,
                            "bloodGroup" to bloodGroup,
                            "lastDonationDate" to formattedToSave,
                            "isAvailable" to updatedIsAvailable
                        )
                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                            .set(updates, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener {
                                isSaving = false
                                LocalUserState.name.value = name
                                LocalUserState.age.value = age
                                LocalUserState.bloodGroup.value = bloodGroup
                                LocalUserState.isAvailable.value = updatedIsAvailable
                                
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
                                LocalUserState.isAvailable.value = updatedIsAvailable
                                
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
                        LocalUserState.isAvailable.value = updatedIsAvailable
                        
                        if (formattedToSave != LocalUserState.lastDonationDate.value && formattedToSave.isNotBlank()) {
                            LocalUserState.donationHistory.add(0, DonationRecord(formattedToSave, "Self Updated", "Completed"))
                        }
                        LocalUserState.lastDonationDate.value = formattedToSave
                        
                        onBack()
                    }
                },
                enabled = canSave && !isSaving,
                modifier = Modifier.fillMaxWidth().height(Dimens.buttonHeight)
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
