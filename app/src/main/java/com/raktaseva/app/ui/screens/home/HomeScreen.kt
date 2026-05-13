package com.raktaseva.app.ui.screens.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
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
fun HomeScreen(onNavigateToRequest: () -> Unit) {
    var requests by remember { mutableStateOf(emptyList<BloodRequest>()) }
    var isLoading by remember { mutableStateOf(true) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("requests")
            .whereEqualTo("requestStatus", "active")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(5)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    requests = snapshot.documents.mapNotNull { it.toObject(BloodRequest::class.java) }
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
            contentPadding = PaddingValues(start = Dimens.screenHorizontal, top = Dimens.screenVertical, end = Dimens.screenHorizontal, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(Dimens.cardSpacing)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingXs)) {
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
            }
            item {
                AvailabilityCard()
            }
            item {
                Text(
                    "LIVE CRITICAL REQUESTS",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = Dimens.spacingXs)
                )
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
                }
            }
        }
    }
}

@Composable
fun AvailabilityCard() {
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Last: $lastDate", style = MaterialTheme.typography.labelSmall, color = heroMuted)
                Text("Next: $nextDate", style = MaterialTheme.typography.labelSmall, color = heroMuted)
            }
        }
    }
}

@Composable
fun EmergencyRequestItem(request: BloodRequest, showSnackbar: (String) -> Unit = {}) {
    val currentUid = LocalUserState.uid.value
    var isAccepting by remember { mutableStateOf(false) }
    val localAccepted = request.responders.contains(currentUid) || isAccepting
    var showCancelDialog by remember { mutableStateOf(false) }
    var showCompatibleDonors by remember { mutableStateOf(false) }
    var showResponders by remember { mutableStateOf(false) }
    var isVisible by remember { mutableStateOf(true) }

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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Dimens.cardRadius),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = Dimens.cardElevation)
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
                            Surface(
                                color = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "Urgency: ${request.urgencyLevel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
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
                            Surface(
                                color = statusColor,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            "Responses: ${request.donorResponsesCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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

            Spacer(modifier = Modifier.height(Dimens.spacingMd))
            Divider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(Dimens.spacingMd))

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
                    if (localAccepted) {
                        Surface(
                            modifier = Modifier.weight(1f).height(40.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                            shape = RoundedCornerShape(Dimens.buttonRadius)
                        ) {
                            Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
                                Text("Accepted by You", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Button(
                            onClick = { 
                                if (currentUid.isEmpty() || request.requesterUid == currentUid) return@Button
                                isAccepting = true
                                val db = FirebaseFirestore.getInstance()
                                val docRef = db.collection("requests").document(request.requestId)
                                val batch = db.batch()
                                
                                batch.update(docRef, "responders", com.google.firebase.firestore.FieldValue.arrayUnion(currentUid))
                                batch.update(docRef, "donorResponsesCount", com.google.firebase.firestore.FieldValue.increment(1))
                                
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
                                batch.set(notifRef, notification)
                                
                                batch.commit().addOnSuccessListener {
                                    isAccepting = false
                                    showSnackbar("Request Accepted!")
                                }.addOnFailureListener {
                                    isAccepting = false
                                    showSnackbar("Failed to accept")
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
                    OutlinedButton(
                        onClick = { 
                            showSnackbar("Notes: ${request.additionalNotes}")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(Dimens.buttonRadius),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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

@Composable
fun AcceptedDonorsList(request: BloodRequest) {
    val responders = request.responders
    var donors by remember { mutableStateOf(emptyList<com.raktaseva.app.data.model.UserProfile>()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(responders) {
        if (responders.isEmpty()) {
            isLoading = false
            return@DisposableEffect onDispose {}
        }
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
                    com.raktaseva.app.ui.screens.donors.DonorCard(donor, showContact = (request.requesterUid == LocalUserState.uid.value))
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
                                            batch.update(db.collection("requests").document(request.requestId), "contactedDonors", request.contactedDonors + donor.uid)
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
                                        batch.update(reqRef, "completedDonors", request.completedDonors + donor.uid)
                                        batch.update(reqRef, "requestStatus", "archived")
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
            .orderBy("createdAt", Query.Direction.DESCENDING)
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
                com.raktaseva.app.ui.screens.donors.DonorCard(donor, showContact = false)
            }
        }
    }
}
