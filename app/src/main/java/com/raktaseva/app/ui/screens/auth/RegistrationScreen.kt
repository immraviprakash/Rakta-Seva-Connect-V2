package com.raktaseva.app.ui.screens.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.raktaseva.app.ui.state.LocalUserState
import com.raktaseva.app.ui.state.DonationRecord
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val nameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val phoneFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val confirmPasswordFocusRequester = remember { FocusRequester() }
    val ageFocusRequester = remember { FocusRequester() }
    val lastDonationDateFocusRequester = remember { FocusRequester() }

    var step by remember { mutableIntStateOf(1) }
    
    // Step 1: Account Info
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Step 2: Personal & Medical Info
    var age by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("") }
    var lastDonationDate by remember { mutableStateOf("") }
    
    // Step 3: Intent
    var intent by remember { mutableStateOf("") }

    val isEmailValid = email.contains("@") && email.contains(".")
    val showEmailError = email.isNotEmpty() && !isEmailValid
    val isPhoneValid = phone.length >= 10
    val isPasswordValid = password.length >= 6
    val isConfirmPasswordValid = password == confirmPassword && password.isNotEmpty()
    val showConfirmPasswordError = confirmPassword.isNotEmpty() && password != confirmPassword

    val isAgeValid = age.toIntOrNull()?.let { it >= 18 } ?: false
    val showAgeError = age.isNotEmpty() && !isAgeValid
    val isDateValid = lastDonationDate.isEmpty() || (lastDonationDate.length == 8 && com.raktaseva.app.ui.state.LocalUserState.tryParseDate(lastDonationDate) != null)
    val showDateError = lastDonationDate.isNotEmpty() && (lastDonationDate.length < 8 || com.raktaseva.app.ui.state.LocalUserState.tryParseDate(lastDonationDate) == null)

    val canContinue = when (step) {
        1 -> name.isNotBlank() && isEmailValid && isPhoneValid && isPasswordValid && isConfirmPasswordValid
        2 -> age.isNotBlank() && gender.isNotBlank() && isAgeValid && bloodGroup.isNotBlank() && isDateValid
        else -> intent.isNotBlank()
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoading by remember { mutableStateOf(false) }

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
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = { if (step > 1) step-- else onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { 
                        if (step == 1) {
                            isLoading = true
                            @Suppress("DEPRECATION")
                            FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email)
                                .addOnCompleteListener { task ->
                                    isLoading = false
                                    if (task.isSuccessful) {
                                        val isNewUser = task.result?.signInMethods?.isEmpty() ?: true
                                        if (isNewUser) {
                                            step++
                                        } else {
                                            scope.launch {
                                                snackbarHostState.showSnackbar("This email is already registered. Try logging in instead.")
                                            }
                                        }
                                    } else {
                                        scope.launch {
                                            snackbarHostState.showSnackbar(task.exception?.message ?: "Failed to verify email")
                                        }
                                    }
                                }
                        } else if (step < 3) {
                            step++ 
                        } else {
                            isLoading = true
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val user = FirebaseAuth.getInstance().currentUser
                                        val uid = user?.uid ?: ""
                                        user?.updateProfile(
                                            com.google.firebase.auth.UserProfileChangeRequest.Builder()
                                                .setDisplayName(name)
                                                .build()
                                        )
                                        
                                        val formattedDate = buildString {
                                            val trimmed = lastDonationDate.take(8)
                                            for (i in trimmed.indices) {
                                                append(trimmed[i])
                                                if (i == 1 || i == 3) append("/")
                                            }
                                        }
                                        
                                        val userData = hashMapOf(
                                            "uid" to uid,
                                            "fullName" to name,
                                            "email" to email,
                                            "mobileNumber" to phone,
                                            "bloodGroup" to bloodGroup,
                                            "gender" to gender,
                                            "age" to age,
                                            "lastDonationDate" to formattedDate,
                                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                                        )
                                        
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                                            .set(userData)
                                            .addOnCompleteListener { firestoreTask ->
                                                isLoading = false
                                                if (firestoreTask.isSuccessful) {
                                                    LocalUserState.uid.value = uid
                                                    LocalUserState.email.value = email
                                                    LocalUserState.phone.value = phone
                                                    LocalUserState.name.value = name
                                                    LocalUserState.age.value = age
                                                    LocalUserState.gender.value = gender
                                                    LocalUserState.bloodGroup.value = bloodGroup
                                                    LocalUserState.lastDonationDate.value = formattedDate
                                                    LocalUserState.intent.value = intent
                                                    
                                                    if (formattedDate.isNotBlank()) {
                                                        LocalUserState.donationHistory.add(DonationRecord(formattedDate, "Registered Record", "Completed"))
                                                    }
                                                    
                                                    LocalUserState.isLoggedIn.value = true
                                                    onComplete()
                                                } else {
                                                    scope.launch {
                                                        snackbarHostState.showSnackbar(
                                                            firestoreTask.exception?.message ?: "Registration completed but profile save failed"
                                                        )
                                                    }
                                                    LocalUserState.uid.value = uid
                                                    LocalUserState.email.value = email
                                                    LocalUserState.phone.value = phone
                                                    LocalUserState.name.value = name
                                                    LocalUserState.age.value = age
                                                    LocalUserState.gender.value = gender
                                                    LocalUserState.bloodGroup.value = bloodGroup
                                                    LocalUserState.lastDonationDate.value = formattedDate
                                                    LocalUserState.isLoggedIn.value = true
                                                    onComplete()
                                                }
                                            }
                                    } else {
                                        isLoading = false
                                        scope.launch {
                                            snackbarHostState.showSnackbar(
                                                task.exception?.message ?: "Registration failed"
                                            )
                                        }
                                    }
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = canContinue && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    } else {
                        Text(if (step < 3) "Next Step" else "Complete Profile", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Step $step of 3",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = when(step) {
                        1 -> "Account Setup"
                        2 -> "Personal Details"
                        else -> "Preferences"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = step / 3f,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            when (step) {
                1 -> {
                    Text("Account Setup", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(nameFocusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { emailFocusRequester.requestFocus() }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(emailFocusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { phoneFocusRequester.requestFocus() }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = showEmailError,
                        supportingText = if (showEmailError) { { Text("Enter a valid email address", color = MaterialTheme.colorScheme.error) } } else null
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it.filter(Char::isDigit).take(10) },
                        label = { Text("Mobile Number") },
                        prefix = { Text("+91 ", fontWeight = FontWeight.Bold) },
                        leadingIcon = { Icon(Icons.Rounded.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(phoneFocusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { passwordFocusRequester.requestFocus() }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val description = if (passwordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(passwordFocusRequester),
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { confirmPasswordFocusRequester.requestFocus() }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            val image = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                            val description = if (confirmPasswordVisible) "Hide password" else "Show password"
                            IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                                Icon(imageVector = image, contentDescription = description)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(confirmPasswordFocusRequester),
                        visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        isError = showConfirmPasswordError,
                        supportingText = if (showConfirmPasswordError) { { Text("Passwords do not match", color = MaterialTheme.colorScheme.error) } } else null
                    )
                }
                2 -> {
                    Text("Personal Details", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it.filter(Char::isDigit).take(3) },
                        label = { Text("Age") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(ageFocusRequester),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { lastDonationDateFocusRequester.requestFocus() }),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
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
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Text("Gender", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        listOf("Male", "Female", "Other").forEach { option ->
                            SelectableChip(
                                text = option,
                                selected = gender == option,
                                onClick = { gender = option },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text("Blood Group", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
                    Column {
                        for (i in bloodGroups.indices step 4) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                for (j in 0 until 4) {
                                    if (i + j < bloodGroups.size) {
                                        val bg = bloodGroups[i + j]
                                        SelectableChip(
                                            text = bg,
                                            selected = bloodGroup == bg,
                                            onClick = { bloodGroup = bg },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
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
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date") 
                            }
                        },
                        isError = showDateError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        placeholder = { Text("DD/MM/YYYY") },
                        visualTransformation = DateTransformation(),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(lastDonationDateFocusRequester),
                        shape = RoundedCornerShape(12.dp),
                        supportingText = if (showDateError) {
                            { Text("Enter a valid complete date (DD/MM/YYYY) or leave blank", color = MaterialTheme.colorScheme.error) }
                        } else {
                            { Text("Format: DD/MM/YYYY") }
                        }
                    )
                }
                3 -> {
                    Text("How can we help?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
                    Text(
                        "Choose how you want to participate in the Rakta-Seva network.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                    )
                    
                    val intentOptions = listOf(
                        Triple("Donate", "Donate Blood", "I want to be notified when someone needs my blood type."),
                        Triple("Receive", "Request Blood", "I am looking for donors for a patient."),
                        Triple("Both", "Both", "I want to do both.")
                    )
                    
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        intentOptions.forEach { (id, title, desc) ->
                            IntentCard(
                                title = title,
                                description = desc,
                                selected = intent == id,
                                onClick = { intent = id }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

class DateTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1 || i == 3) out += "/"
        }

        val dateOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }
        return TransformedText(AnnotatedString(out), dateOffsetTranslator)
    }
}

@Composable
fun SelectableChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun IntentCard(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (selected) 2.dp else 1.dp,
            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }
            if (selected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            } else {
                Box(
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
