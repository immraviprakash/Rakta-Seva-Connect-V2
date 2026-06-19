package com.raktaseva.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.foundation.clickable
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import android.widget.Toast
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.OutlinedTextField
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.raktaseva.app.data.model.BloodRequest
import com.raktaseva.app.ui.components.BloodGroupBadge
import com.raktaseva.app.ui.state.LocalUserState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raktaseva.app.ui.theme.Dimens
import com.raktaseva.app.ui.theme.LightHeroCard
import com.raktaseva.app.ui.theme.LightHeroOnCard
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onNavigateToRequest: () -> Unit,
    onNavigateToChat: () -> Unit = {}
) {
    val context = LocalContext.current
    var requests by remember { mutableStateOf(emptyList<BloodRequest>()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(LocalUserState.bloodGroup.value) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("requests")
            .whereEqualTo("requestStatus", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    android.widget.Toast.makeText(context, "Error loading requests: ${error.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val allRequests = snapshot.documents.mapNotNull { it.toObject(BloodRequest::class.java) }
                    requests = allRequests.filter { request ->
                        request.requesterUid == LocalUserState.uid.value ||
                        com.raktaseva.app.utils.BloodCompatibility.isCompatibleDonor(
                            donorGroup = LocalUserState.bloodGroup.value,
                            neededGroup = request.bloodGroup
                        )
                    }.take(5)
                    isLoading = false
                }
            }
        onDispose {
            listener.remove()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToRequest,
                icon = { Icon(Icons.Default.Add, contentDescription = "Request Blood") },
                text = { Text("Request Blood") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(Dimens.fabRadius)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = Dimens.screenHorizontal, top = Dimens.screenVertical, end = Dimens.screenHorizontal, bottom = 96.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingXs)
                    ) {
                        Text(
                            "Emergency Dashboard",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            "Active requests in your area",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(Dimens.spacingMd))
                    Button(
                        onClick = onNavigateToChat,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        shape = RoundedCornerShape(Dimens.buttonRadiusFull),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Text(
                            text = "AI Assistance",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(Dimens.spacingMd))
            }
            item {
                AvailabilityCard()
                Spacer(modifier = Modifier.height(Dimens.spacingXxl))
            }
            item {
                Text(
                    "LIVE CRITICAL REQUESTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
            }
            if (isLoading) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            } else if (requests.isEmpty()) {
                item {
                    Text(
                        "No active requests nearby.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            } else {
                items(requests, key = { it.requestId }) { request ->
                    EmergencyRequestItem(
                        request = request,
                        showSnackbar = { msg ->
                            coroutineScope.launch {
                                snackbarHostState.showSnackbar(msg)
                            }
                        }
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                }
            }
        }
    }
}

@Composable
fun AvailabilityCard() {
    val context = LocalContext.current
    var showConsentDialog by remember { mutableStateOf(false) }
    var isAvailableChecked by remember { mutableStateOf(LocalUserState.isAvailable.value) }
    
    LaunchedEffect(LocalUserState.isAvailable.value) {
        isAvailableChecked = LocalUserState.isAvailable.value
    }

    fun updateAvailability(available: Boolean) {
        val uid = LocalUserState.uid.value
        if (uid.isEmpty()) {
            isAvailableChecked = false
            return
        }
        val db = FirebaseFirestore.getInstance()
        val updates = mapOf(
            "isAvailable" to available
        )
        db.collection("users").document(uid).set(updates, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                LocalUserState.isAvailable.value = available
                if (available) {
                    android.util.Log.d("Availability", "Availability ON success")
                } else {
                    android.util.Log.d("Availability", "Availability OFF success")
                }
                Toast.makeText(context, "Availability status updated!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                isAvailableChecked = LocalUserState.isAvailable.value
                android.util.Log.e("Availability", "Firestore update failure reason: ${e.localizedMessage}")
                Toast.makeText(context, "Failed to update: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    if (showConsentDialog) {
        AlertDialog(
            onDismissRequest = { 
                showConsentDialog = false 
                isAvailableChecked = false
            },
            title = { Text("Become Available Donor") },
            text = {
                Column {
                    Text("By enabling this option:\n")
                    Text("• Your blood group will appear in the donor directory.")
                    Text("• Patients may contact you using your registered contact information.")
                    Text("• You may receive blood donation requests.")
                    Text("• You can disable availability at any time.")
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showConsentDialog = false
                        isAvailableChecked = true
                        updateAvailability(true)
                    }
                ) {
                    Text("I Agree")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showConsentDialog = false 
                        isAvailableChecked = false
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    val isEligible = LocalUserState.isEligibleToDonate()
    val lastDate = LocalUserState.lastDonationDate.value.ifBlank { "Never" }
    val nextDate = if (isEligible) "Now" else "In ${LocalUserState.getDaysUntilEligible()} days"
    val progress = if (isEligible) 1f else {
        val days = LocalUserState.getDaysSinceLastDonation() ?: 0
        (days.toFloat() / 90f).coerceIn(0f, 1f)
    }
    val statusText = if (isEligible) "Ready to Donate" else "Not Eligible"
    val progressColor = if (isEligible) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

    val isDark = isSystemInDarkTheme()
    val heroBackground = if (isDark) MaterialTheme.colorScheme.surfaceVariant else LightHeroCard
    val heroOnSurface = if (isDark) MaterialTheme.colorScheme.onSurface else LightHeroOnCard
    val heroMuted = if (isDark) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFB0B0B0)
    val heroTrack = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFF4A4A4C)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = heroBackground),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.cardPadding)) {
            Text(
                "DONOR ELIGIBILITY",
                style = MaterialTheme.typography.labelMedium,
                color = heroMuted
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            Text(
                statusText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                color = heroOnSurface
            )
            Spacer(modifier = Modifier.height(Dimens.spacingLg))
            androidx.compose.material3.LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = progressColor,
                trackColor = heroTrack,
                strokeCap = StrokeCap.Round
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Dimens.spacingSm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Last: $lastDate", style = MaterialTheme.typography.labelSmall, color = heroMuted)
                Text("Next: $nextDate", style = MaterialTheme.typography.labelSmall, color = heroMuted)
            }
            if (isEligible) {
                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                Divider(color = heroMuted.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Available for Donation",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = heroOnSurface
                        )
                        val subtext = if (isEligible && lastDate != "Never" && !isAvailableChecked) {
                            "You are now eligible to donate again."
                        } else {
                            "Visible in available donors list"
                        }
                        val subtextColor = if (isEligible && lastDate != "Never" && !isAvailableChecked) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            heroMuted
                        }
                        Text(
                            subtext,
                            style = MaterialTheme.typography.labelSmall,
                            color = subtextColor
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = isAvailableChecked,
                        onCheckedChange = { checked ->
                            if (checked) {
                                showConsentDialog = true
                            } else {
                                isAvailableChecked = false
                                updateAvailability(false)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EmergencyRequestItem(request: BloodRequest, showSnackbar: (String) -> Unit = {}) {
    val currentUid = LocalUserState.uid.value
    var isAccepting by remember { mutableStateOf(false) }
    val isAcceptedByOthers = request.acceptedByUid.isNotEmpty() && request.acceptedByUid != currentUid
    val isAcceptedByMe = request.acceptedByUid == currentUid || request.responders.contains(currentUid)
    val localAccepted = isAcceptedByMe || isAccepting
    var showCancelDialog by remember { mutableStateOf(false) }
    var showCompatibleDonors by remember { mutableStateOf(false) }
    var showResponders by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }
    var isExpanded by remember { mutableStateOf(false) }

    if (!isVisible) return

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Request") },
            text = { Text("Are you sure you want to cancel this emergency request? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    isVisible = false
                    val db = FirebaseFirestore.getInstance()
                    val batch = db.batch()
                    val reqRef = db.collection("requests").document(request.requestId)
                    batch.update(reqRef, "requestStatus", "archived")
                    batch.update(reqRef, "status", "cancelled")
                    
                    request.responders.forEach { responderUid ->
                        val notifRef = db.collection("notifications").document()
                        val notification = hashMapOf(
                            "notificationId" to notifRef.id,
                            "targetUserUid" to responderUid,
                            "title" to "Request Cancelled",
                            "message" to "An emergency request you accepted was cancelled.",
                            "type" to "cancelled",
                            "relatedRequestId" to request.requestId,
                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                            "isRead" to false
                        )
                        batch.set(notifRef, notification)
                    }
                    batch.commit()
                }) {
                    Text("Confirm Cancel", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep Request")
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(Dimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.cardPadding)) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                BloodGroupBadge(group = request.bloodGroup, size = 42)
                Spacer(modifier = Modifier.width(Dimens.spacingMd))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    "Urgency: ${request.urgencyLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            val statusLabel = when (request.requestStatus) {
                                "active" -> if (request.donorResponsesCount > 0) "Accepted" else "Awaiting"
                                "fulfilled" -> "Completed"
                                "archived" -> "Archived"
                                else -> "Active"
                            }
                            val statusColor = when (statusLabel) {
                                "Awaiting", "Active" -> MaterialTheme.colorScheme.errorContainer
                                "Accepted" -> MaterialTheme.colorScheme.primaryContainer
                                "Completed" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                            Box(
                                modifier = Modifier
                                    .background(statusColor, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    statusLabel,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(
                                "Responses: ${request.donorResponsesCount}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (isExpanded) "Collapse" else "Expand",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    Text(request.hospitalName, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "${request.requesterName} · ${request.unitsRequired} units",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            androidx.compose.animation.AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = Dimens.spacingMd)) {
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                    
                    // Detailed Information
                    Text(
                        "Request Details",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Patient Name:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(request.requesterName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Hospital Name:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(request.hospitalName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Blood Group:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(request.bloodGroup, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Units Required:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${request.unitsRequired} units", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Urgency Level:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(request.urgencyLevel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Request Status:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(request.requestStatus.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    }
                    
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                    
                    Text(
                        "AI Emergency Message",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(Dimens.cardRadius))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = cleanEmergencyMessage(request.additionalNotes).ifBlank { "No message details generated." },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                    Divider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                    
                    // Action Buttons (Accept, Cancel, Archive, etc.)
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (request.requesterUid == currentUid) {
                            if (request.donorResponsesCount > 0) {
                                Button(
                                    onClick = {
                                        isVisible = false
                                        val db = FirebaseFirestore.getInstance()
                                        val batch = db.batch()
                                        val reqRef = db.collection("requests").document(request.requestId)
                                        batch.update(reqRef, "requestStatus", "archived")
                                        
                                        request.responders.forEach { responderUid ->
                                            val notifRef = db.collection("notifications").document()
                                            val notification = hashMapOf(
                                                "notificationId" to notifRef.id,
                                                "targetUserUid" to responderUid,
                                                "title" to "Request Archived",
                                                "message" to "The emergency request you accepted has been archived.",
                                                "type" to "archived",
                                                "relatedRequestId" to request.requestId,
                                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                                "isRead" to false
                                            )
                                            batch.set(notifRef, notification)
                                        }
                                        batch.commit()
                                    },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(Dimens.buttonRadius),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                                ) {
                                    Text("Archive Request", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { showResponders = !showResponders },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(Dimens.buttonRadius),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(if (showResponders) "Hide Donors" else "View Donors", fontWeight = FontWeight.Bold)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { showCompatibleDonors = !showCompatibleDonors },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(Dimens.buttonRadius),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                ) {
                                    Text(if (showCompatibleDonors) "Hide Match" else "Match Donors", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(Dimens.buttonRadius),
                                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                                ) {
                                    Text("Cancel Request", fontWeight = FontWeight.Bold)
                                }
                            }
                        } else {
                            if (isAcceptedByOthers) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(Dimens.buttonRadius)),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    Text("Accepted by ${request.acceptedByName}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else if (localAccepted) {
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(40.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(Dimens.buttonRadius)),
                                    contentAlignment = androidx.compose.ui.Alignment.Center
                                ) {
                                    Text("Accepted by You", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            } else {
                                Button(
                                    onClick = { 
                                        if (currentUid.isEmpty() || request.requesterUid == currentUid) return@Button
                                        isAccepting = true
                                        val db = FirebaseFirestore.getInstance()
                                        val docRef = db.collection("requests").document(request.requestId)
                                        
                                        db.runTransaction { transaction ->
                                            val snapshot = transaction.get(docRef)
                                            val acceptedBy = snapshot.getString("acceptedByUid") ?: ""
                                            if (acceptedBy.isNotEmpty()) {
                                                throw Exception("This request has already been accepted by another donor.")
                                            }
                                            
                                            transaction.update(docRef, "responders", com.google.firebase.firestore.FieldValue.arrayUnion(currentUid))
                                            transaction.update(docRef, "donorResponsesCount", com.google.firebase.firestore.FieldValue.increment(1))
                                            transaction.update(docRef, "acceptedByUid", currentUid)
                                            transaction.update(docRef, "acceptedByName", LocalUserState.name.value)
                                            transaction.update(docRef, "status", "accepted")
                                            
                                            val notifRef = db.collection("notifications").document()
                                            val notification = hashMapOf(
                                                "notificationId" to notifRef.id,
                                                "targetUserUid" to request.requesterUid,
                                                "title" to "Donor Accepted",
                                                "message" to "${LocalUserState.name.value} accepted your emergency request for ${request.bloodGroup} blood.",
                                                "type" to "accepted",
                                                "relatedRequestId" to request.requestId,
                                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                                "isRead" to false
                                            )
                                            transaction.set(notifRef, notification)
                                            null
                                        }.addOnSuccessListener {
                                            isAccepting = false
                                            showSnackbar("Request Accepted!")
                                        }.addOnFailureListener { e ->
                                            isAccepting = false
                                            showSnackbar(e.message ?: "Failed to accept")
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = !localAccepted && !isAccepting,
                                    shape = RoundedCornerShape(Dimens.buttonRadius),
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                ) {
                                    if (isAccepting) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    } else {
                                        Text("Accept", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = showCompatibleDonors) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Divider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Compatible Donors (Auto-Matched)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            CompatibleDonorsList(neededBloodGroup = request.bloodGroup)
                        }
                    }

                    androidx.compose.animation.AnimatedVisibility(visible = showResponders) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            Divider(color = MaterialTheme.colorScheme.outline)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Accepted Donors",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            AcceptedDonorsList(request = request)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AcceptedDonorsList(request: BloodRequest) {
    val responders = request.responders
    var donors by remember { mutableStateOf(emptyList<com.raktaseva.app.data.model.UserProfile>()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(responders) {
        donors = emptyList()
        if (responders.isEmpty()) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }
        isLoading = true
        val db = FirebaseFirestore.getInstance()
        
        // Firestore whereIn accepts max 10 elements.
        val chunks = responders.chunked(10)
        val listeners = chunks.map { chunk ->
            db.collection("users")
                .whereIn("uid", chunk)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        isLoading = false
                        return@addSnapshotListener
                    }
                    if (snapshot != null) {
                        val newDonors = snapshot.documents.mapNotNull { it.toObject(com.raktaseva.app.data.model.UserProfile::class.java) }
                        donors = (donors.filter { d -> newDonors.none { it.uid == d.uid } } + newDonors).sortedBy { it.fullName }
                        isLoading = false
                    }
                }
        }
        
        onDispose {
            listeners.forEach { it.remove() }
        }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else if (donors.isEmpty()) {
        Text("No donor details available.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            donors.forEach { donor ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    com.raktaseva.app.ui.screens.donors.DonorCard(donor, showContact = (request.requesterUid == LocalUserState.uid.value), embedded = true)
                    if (request.requesterUid == LocalUserState.uid.value && request.requestStatus == "active") {
                        val isContacted = request.contactedDonors.contains(donor.uid)
                        val isCompleted = request.completedDonors.contains(donor.uid)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            if (isCompleted) {
                                Text("Donation Completed", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                            } else {
                                if (!isContacted) {
                                    OutlinedButton(
                                        onClick = {
                                            val db = FirebaseFirestore.getInstance()
                                            val batch = db.batch()
                                            batch.update(db.collection("requests").document(request.requestId), "contactedDonors", com.google.firebase.firestore.FieldValue.arrayUnion(donor.uid))
                                            val notifRef = db.collection("notifications").document()
                                            batch.set(notifRef, hashMapOf(
                                                "notificationId" to notifRef.id,
                                                "targetUserUid" to donor.uid,
                                                "title" to "Requester Contacted You",
                                                "message" to "${LocalUserState.name.value} has marked that they contacted you.",
                                                "type" to "contacted",
                                                "relatedRequestId" to request.requestId,
                                                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                                "isRead" to false
                                            ))
                                            batch.commit()
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                                    ) { Text("Mark Contacted") }
                                } else {
                                    Text("Contacted", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(8.dp))
                                }
                                Button(
                                    onClick = {
                                        val db = FirebaseFirestore.getInstance()
                                        val batch = db.batch()
                                        val reqRef = db.collection("requests").document(request.requestId)
                                        batch.update(reqRef, "completedDonors", com.google.firebase.firestore.FieldValue.arrayUnion(donor.uid))
                                        batch.update(reqRef, "requestStatus", "fulfilled")

                                        val todayStr = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                                        val donorRef = db.collection("users").document(donor.uid)
                                        batch.update(donorRef, "lastDonationDate", todayStr)
                                        batch.update(donorRef, "isAvailable", false)

                                        val notifRef = db.collection("notifications").document()
                                        batch.set(notifRef, hashMapOf(
                                            "notificationId" to notifRef.id,
                                            "targetUserUid" to donor.uid,
                                            "title" to "Donation Completed",
                                            "message" to "Thank you! The requester has confirmed your donation.",
                                            "type" to "completed",
                                            "relatedRequestId" to request.requestId,
                                            "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                            "isRead" to false
                                        ))
                                        batch.commit()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                                ) { Text("Donation Received") }
                            }
                        }
                    } else if (request.requestStatus == "archived" && request.completedDonors.contains(donor.uid)) {
                        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
                            Text("Donation Completed", color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CompatibleDonorsList(neededBloodGroup: String) {
    var donors by remember { mutableStateOf(emptyList<com.raktaseva.app.data.model.UserProfile>()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(neededBloodGroup) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("users")
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val newDonors = snapshot.documents.mapNotNull { doc ->
                        val donor = doc.toObject(com.raktaseva.app.data.model.UserProfile::class.java)
                        if (donor != null && donor.uid != LocalUserState.uid.value && com.raktaseva.app.utils.BloodCompatibility.isCompatibleDonor(donorGroup = donor.bloodGroup, neededGroup = neededBloodGroup)) {
                            donor
                        } else null
                    }
                    donors = newDonors.sortedWith(compareByDescending { com.raktaseva.app.ui.screens.donors.isDonorEligible(it.lastDonationDate) })
                    isLoading = false
                }
            }
        onDispose { listener.remove() }
    }

    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        }
    } else if (donors.isEmpty()) {
        Text("No compatible donors found nearby.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            donors.take(5).forEach { donor ->
                com.raktaseva.app.ui.screens.donors.DonorCard(donor, showContact = false, embedded = true)
            }
        }
    }
}

fun cleanEmergencyMessage(rawMessage: String): String {
    val noEmojis = rawMessage
        .replace("🚨", "")
        .replace("🩸", "")
        .replace("❤️", "")
        .replace("💉", "")
        .replace("✨", "")
        .replace("⚠️", "")

    val lines = noEmojis.split("\n")
    val cleanedLines = lines.map { it.trim() }.filter { line ->
        val upper = line.uppercase()
        !upper.startsWith("PATIENT NAME:") &&
        !upper.startsWith("PATIENT:") &&
        !upper.startsWith("HOSPITAL:") &&
        !upper.startsWith("BLOOD GROUP:") &&
        !upper.startsWith("BLOOD GROUP REQUIRED:") &&
        !upper.startsWith("UNITS:") &&
        !upper.startsWith("UNITS REQUIRED:") &&
        !upper.startsWith("URGENT MESSAGE:")
    }

    return cleanedLines.joinToString("\n")
        .replace(Regex("\n{3,}"), "\n\n")
        .trim()
}
