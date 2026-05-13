package com.raktaseva.app.ui.screens.auth

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.raktaseva.app.ui.state.LocalUserState
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var forgotPasswordEmail by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var isLoading by remember { mutableStateOf(false) }
    var isResettingPassword by remember { mutableStateOf(false) }

    val canLogin = email.isNotBlank() && password.isNotBlank()

    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Column {
                    Text("Enter your registered email address.")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = forgotPasswordEmail,
                        onValueChange = { forgotPasswordEmail = it },
                        label = { Text("Email") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isResettingPassword = true
                        FirebaseAuth.getInstance().sendPasswordResetEmail(forgotPasswordEmail)
                            .addOnCompleteListener { task ->
                                isResettingPassword = false
                                if (task.isSuccessful) {
                                    showForgotPasswordDialog = false
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Password reset link sent successfully")
                                    }
                                } else {
                                    scope.launch {
                                        snackbarHostState.showSnackbar(task.exception?.message ?: "Failed to send reset email")
                                    }
                                }
                            }
                    },
                    enabled = forgotPasswordEmail.isNotBlank() && !isResettingPassword
                ) {
                    if (isResettingPassword) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text("Send Link")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
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
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                "Welcome Back",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Enter your email and password to login.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                lineHeight = 24.sp
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email Address") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                    val description = if (passwordVisible) "Hide password" else "Show password"
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Forgot Password?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .clickable { showForgotPasswordDialog = true }
                        .padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = { 
                    isLoading = true
                    FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = FirebaseAuth.getInstance().currentUser
                                val uid = user?.uid ?: ""
                                if (uid.isNotEmpty()) {
                                    com.google.firebase.firestore.FirebaseFirestore.getInstance().collection("users").document(uid)
                                        .get()
                                        .addOnSuccessListener { document ->
                                            isLoading = false
                                            LocalUserState.uid.value = uid
                                            LocalUserState.email.value = document.getString("email") ?: user?.email ?: ""
                                            LocalUserState.phone.value = document.getString("mobileNumber") ?: ""
                                            LocalUserState.name.value = document.getString("fullName") ?: user?.displayName?.takeIf { it.isNotBlank() } ?: (user?.email?.substringBefore("@") ?: "User")
                                            LocalUserState.age.value = document.getString("age") ?: ""
                                            LocalUserState.gender.value = document.getString("gender") ?: ""
                                            LocalUserState.bloodGroup.value = document.getString("bloodGroup") ?: "O+"
                                            
                                            val lastDate = document.getString("lastDonationDate") ?: ""
                                            LocalUserState.lastDonationDate.value = lastDate
                                            
                                            if (lastDate.isNotBlank() && LocalUserState.donationHistory.none { it.date == lastDate }) {
                                                LocalUserState.donationHistory.add(0, com.raktaseva.app.ui.state.DonationRecord(lastDate, "Past Record", "Completed"))
                                            }
                                            
                                            LocalUserState.isLoggedIn.value = true
                                            onLoginSuccess()
                                        }
                                        .addOnFailureListener { _ ->
                                            isLoading = false
                                            LocalUserState.uid.value = uid
                                            LocalUserState.email.value = user?.email ?: ""
                                            LocalUserState.name.value = user?.displayName?.takeIf { it.isNotBlank() } ?: (user?.email?.substringBefore("@") ?: "User")
                                            LocalUserState.isLoggedIn.value = true
                                            onLoginSuccess()
                                        }
                                } else {
                                    isLoading = false
                                    LocalUserState.isLoggedIn.value = true
                                    onLoginSuccess()
                                }
                            } else {
                                isLoading = false
                                scope.launch {
                                    snackbarHostState.showSnackbar(task.exception?.message ?: "Invalid credentials")
                                }
                            }
                        }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = canLogin && !isLoading,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    disabledElevation = 0.dp
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                } else {
                    Text("Login", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
