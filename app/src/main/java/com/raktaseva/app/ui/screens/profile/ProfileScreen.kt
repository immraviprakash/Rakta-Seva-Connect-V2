package com.raktaseva.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.raktaseva.app.ui.components.BloodGroupBadge

import com.raktaseva.app.ui.state.LocalUserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onChatClick: () -> Unit,
    onLogoutClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    var showHistorySheet by remember { mutableStateOf(false) }

    if (showHistorySheet) {
        ModalBottomSheet(onDismissRequest = { showHistorySheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Donation History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (LocalUserState.donationHistory.isEmpty()) {
                    Text("No donations recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LocalUserState.donationHistory.forEach { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(record.date, fontWeight = FontWeight.Bold)
                                    Text(record.hospital, style = MaterialTheme.typography.bodyMedium)
                                    Text(record.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BloodGroupBadge(group = LocalUserState.bloodGroup.value, size = 80)
            Spacer(modifier = Modifier.height(16.dp))
            Text(LocalUserState.name.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            val phoneText = if (LocalUserState.phone.value.isNotBlank()) "+91 ${LocalUserState.phone.value}" else "Add phone number"
            Text(phoneText, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                val donationsCount = LocalUserState.donationHistory.size
                ProfileStatItem(label = "Donations", value = "$donationsCount")
                ProfileStatItem(label = "Lives Saved", value = "${donationsCount * 3}")
                val isEligible = if (LocalUserState.isEligibleToDonate()) "Eligible" else "${LocalUserState.getDaysUntilEligible()}d"
                ProfileStatItem(label = "Eligibility", value = isEligible)
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileMenuItem(icon = Icons.Default.Edit, label = "Edit Profile", onClick = onEditProfileClick)
            ProfileMenuItem(icon = Icons.Default.List, label = "Donation History", onClick = { showHistorySheet = true })
            ProfileMenuItem(icon = Icons.Default.Settings, label = "Settings", onClick = onSettingsClick)
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Button(
                onClick = onChatClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("AI Chat Assistant")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            TextButton(
                onClick = {
                    LocalUserState.clear()
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onLogoutClick()
                }, 
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelMedium)
    }
}

@Composable
fun ProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = onClick,
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(label, modifier = Modifier.weight(1f))
        }
    }
}
