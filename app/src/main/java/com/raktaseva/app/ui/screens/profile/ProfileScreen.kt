package com.raktaseva.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.items
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
    var showAcceptedSheet by remember { mutableStateOf(false) }
    var showMyRequestsSheet by remember { mutableStateOf(false) }

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
    
    if (showAcceptedSheet) {
        var acceptedRequests by remember { mutableStateOf(emptyList<com.raktaseva.app.data.model.BloodRequest>()) }
        var isFetching by remember { mutableStateOf(true) }

        DisposableEffect(Unit) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val listener = db.collection("requests")
                .whereArrayContains("responders", LocalUserState.uid.value)
                .addSnapshotListener { snapshot, error ->
                    isFetching = false
                    if (error == null && snapshot != null) {
                        acceptedRequests = snapshot.documents.mapNotNull { it.toObject(com.raktaseva.app.data.model.BloodRequest::class.java) }
                    }
                }
            onDispose { listener.remove() }
        }

        ModalBottomSheet(onDismissRequest = { showAcceptedSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    "Accepted Requests",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (isFetching) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (acceptedRequests.isEmpty()) {
                    Text("No accepted requests found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(acceptedRequests, key = { it.requestId }) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(req.hospitalName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(req.requestStatus.uppercase(), fontWeight = FontWeight.Bold, color = if (req.requestStatus == "fulfilled") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Requester: ${req.requesterName}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Blood Group: ${req.bloodGroup} | Units: ${req.unitsRequired}", style = MaterialTheme.typography.bodyMedium)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Urgency: ${req.urgencyLevel}", style = MaterialTheme.typography.bodyMedium)
                                        val dateStr = try {
                                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(req.createdAt.toDate())
                                        } catch (e: Exception) { "Unknown date" }
                                        Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showMyRequestsSheet) {
        var myRequests by remember { mutableStateOf(emptyList<com.raktaseva.app.data.model.BloodRequest>()) }
        var isFetchingMyReqs by remember { mutableStateOf(true) }

        DisposableEffect(Unit) {
            val db = com.google.firebase.firestore.FirebaseFirestore.getInstance()
            val listener = db.collection("requests")
                .whereEqualTo("requesterUid", LocalUserState.uid.value)
                .addSnapshotListener { snapshot, error ->
                    isFetchingMyReqs = false
                    if (error == null && snapshot != null) {
                        myRequests = snapshot.documents.mapNotNull { it.toObject(com.raktaseva.app.data.model.BloodRequest::class.java) }.sortedByDescending { it.createdAt }
                    }
                }
            onDispose { listener.remove() }
        }

        ModalBottomSheet(onDismissRequest = { showMyRequestsSheet = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Text(
                    "My Requests History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                if (isFetchingMyReqs) {
                    Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (myRequests.isEmpty()) {
                    Text("You have not created any requests.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(myRequests, key = { it.requestId }) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(req.hospitalName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(req.requestStatus.uppercase(), fontWeight = FontWeight.Bold, color = if (req.requestStatus == "fulfilled" || req.requestStatus == "archived") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("Blood Group: ${req.bloodGroup} | Units: ${req.unitsRequired}", style = MaterialTheme.typography.bodyMedium)
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("Responses: ${req.donorResponsesCount}", style = MaterialTheme.typography.bodyMedium)
                                        val dateStr = try {
                                            java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(req.createdAt.toDate())
                                        } catch (e: Exception) { "Unknown date" }
                                        Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                                    }
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
            ProfileMenuItem(icon = Icons.Default.List, label = "My Requests History", onClick = { showMyRequestsSheet = true })
            ProfileMenuItem(icon = Icons.Default.DateRange, label = "Donation History", onClick = { showHistorySheet = true })
            ProfileMenuItem(icon = Icons.Default.Done, label = "Accepted Requests", onClick = { showAcceptedSheet = true })
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
