package com.raktaseva.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
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
import com.raktaseva.app.ui.theme.Dimens

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
                    .padding(horizontal = Dimens.screenHorizontal, vertical = Dimens.spacingSm)
            ) {
                Text(
                    "Donation History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = Dimens.spacingLg)
                )
                if (LocalUserState.donationHistory.isEmpty()) {
                    Text("No donations recorded yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    LocalUserState.donationHistory.forEach { record ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = Dimens.spacingXs),
                            shape = RoundedCornerShape(Dimens.cardRadius),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Row(
                                modifier = Modifier.padding(Dimens.cardPadding),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(Dimens.spacingMd))
                                Column {
                                    Text(record.date, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Text(record.hospital, style = MaterialTheme.typography.bodySmall)
                                    Text(record.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacingHuge))
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
                    .padding(horizontal = Dimens.screenHorizontal, vertical = Dimens.spacingSm)
            ) {
                Text(
                    "Accepted Requests",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = Dimens.spacingLg)
                )
                if (isFetching) {
                    Box(modifier = Modifier.fillMaxWidth().padding(Dimens.spacingLg), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (acceptedRequests.isEmpty()) {
                    Text("No accepted requests found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                    ) {
                        items(acceptedRequests, key = { it.requestId }) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Dimens.cardRadius),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(req.hospitalName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(req.requestStatus.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = if (req.requestStatus == "fulfilled") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                                    }
                                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                                    Text("${req.requesterName} · ${req.bloodGroup} · ${req.unitsRequired} units", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val dateStr = try {
                                        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(req.createdAt.toDate())
                                    } catch (e: Exception) { "Unknown date" }
                                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacingHuge))
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
                modifier = Modifier.fillMaxWidth().padding(horizontal = Dimens.screenHorizontal, vertical = Dimens.spacingSm)
            ) {
                Text(
                    "My Requests History",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = Dimens.spacingLg)
                )
                if (isFetchingMyReqs) {
                    Box(modifier = Modifier.fillMaxWidth().padding(Dimens.spacingLg), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else if (myRequests.isEmpty()) {
                    Text("You have not created any requests.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    androidx.compose.foundation.lazy.LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                    ) {
                        items(myRequests, key = { it.requestId }) { req ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Dimens.cardRadius),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                        Text(req.hospitalName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                        Text(req.requestStatus.uppercase(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall, color = if (req.requestStatus == "fulfilled" || req.requestStatus == "archived") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary)
                                    }
                                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                                    Text("${req.bloodGroup} · ${req.unitsRequired} units · ${req.donorResponsesCount} responses", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    val dateStr = try {
                                        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(req.createdAt.toDate())
                                    } catch (e: Exception) { "Unknown date" }
                                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacingHuge))
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
                .padding(horizontal = Dimens.screenHorizontal, vertical = Dimens.screenVertical),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Profile Header ──
            BloodGroupBadge(group = LocalUserState.bloodGroup.value, size = 64)
            Spacer(modifier = Modifier.height(Dimens.spacingMd))
            Text(LocalUserState.name.value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            val phoneText = if (LocalUserState.phone.value.isNotBlank()) "+91 ${LocalUserState.phone.value}" else "Add phone number"
            Text(phoneText, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            
            Spacer(modifier = Modifier.height(Dimens.spacingXl))
            
            // ── Stats Row ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.cardRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = Dimens.spacingLg),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val donationsCount = LocalUserState.donationHistory.size
                    ProfileStatItem(label = "Donations", value = "$donationsCount")
                    ProfileStatItem(label = "Lives Saved", value = "${donationsCount * 3}")
                    val isEligible = if (LocalUserState.isEligibleToDonate()) "Eligible" else "${LocalUserState.getDaysUntilEligible()}d"
                    ProfileStatItem(label = "Eligibility", value = isEligible)
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.spacingXl))
            
            // ── Menu Items ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.cardRadius),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevation)
            ) {
                Column {
                    ProfileMenuItem(icon = Icons.Default.Edit, label = "Edit Profile", onClick = onEditProfileClick)
                    Divider(modifier = Modifier.padding(horizontal = Dimens.cardPadding), color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileMenuItem(icon = Icons.Default.List, label = "My Requests", onClick = { showMyRequestsSheet = true })
                    Divider(modifier = Modifier.padding(horizontal = Dimens.cardPadding), color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileMenuItem(icon = Icons.Default.DateRange, label = "Donation History", onClick = { showHistorySheet = true })
                    Divider(modifier = Modifier.padding(horizontal = Dimens.cardPadding), color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileMenuItem(icon = Icons.Default.Done, label = "Accepted Requests", onClick = { showAcceptedSheet = true })
                    Divider(modifier = Modifier.padding(horizontal = Dimens.cardPadding), color = MaterialTheme.colorScheme.outlineVariant)
                    ProfileMenuItem(icon = Icons.Default.Settings, label = "Settings", onClick = onSettingsClick)
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.spacingLg))
            
            // ── AI Chat Button ──
            Button(
                onClick = onChatClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.buttonHeight),
                shape = RoundedCornerShape(Dimens.buttonRadius),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(Dimens.spacingSm))
                Text("AI Chat Assistant", fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(Dimens.spacingXl))
            
            // ── Logout ──
            TextButton(
                onClick = {
                    LocalUserState.clear()
                    com.google.firebase.auth.FirebaseAuth.getInstance().signOut()
                    onLogoutClick()
                }, 
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout", fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
        }
    }
}

@Composable
fun ProfileStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(Dimens.spacingXxs))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.cardPadding, vertical = Dimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(Dimens.spacingMd))
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
        }
    }
}
