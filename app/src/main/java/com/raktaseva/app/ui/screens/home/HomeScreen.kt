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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(onNavigateToRequest: () -> Unit) {
    val requests = remember { mutableStateListOf<BloodRequest>() }
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
                    requests.clear()
                    for (doc in snapshot.documents) {
                        val request = doc.toObject(BloodRequest::class.java)
                        if (request != null) {
                            requests.add(request)
                        }
                    }
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
                shape = RoundedCornerShape(8.dp)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 24.dp, top = 24.dp, end = 24.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        "Emergency Dashboard",
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        "3 critical requests pending within 5 km of your location.",
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
                    "Live Critical Requests",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
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
                items(requests) { request ->
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Donor Eligibility",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFFAAAAAA)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                statusText,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Light,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(20.dp))
            androidx.compose.material3.LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
                color = progressColor,
                trackColor = Color(0xFF333333),
                strokeCap = StrokeCap.Round
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Last: $lastDate", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
                Text("Next: $nextDate", style = MaterialTheme.typography.labelSmall, color = Color(0xFFAAAAAA))
            }
        }
    }
}

@Composable
fun EmergencyRequestItem(request: BloodRequest, showSnackbar: (String) -> Unit = {}) {
    val context = LocalContext.current
    val currentUid = LocalUserState.uid.value
    var isAccepting by remember { mutableStateOf(false) }
    var showCancelDialog by remember { mutableStateOf(false) }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Cancel Request") },
            text = { Text("Are you sure you want to cancel this emergency request? This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    showCancelDialog = false
                    FirebaseFirestore.getInstance().collection("requests")
                        .document(request.requestId)
                        .update("requestStatus", "cancelled")
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
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.Top
            ) {
                BloodGroupBadge(group = request.bloodGroup, size = 48)
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFFFEBEE),
                            contentColor = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "Urgency: ${request.urgencyLevel}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Text(
                            "Responses: ${request.donorResponsesCount}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(request.hospitalName, style = MaterialTheme.typography.titleLarge)
                    Text(
                        "Requester: ${request.requesterName} - ${request.unitsRequired} units required.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))
            Divider(color = MaterialTheme.colorScheme.outline)
            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (request.requesterUid == currentUid) {
                    if (request.donorResponsesCount > 0) {
                        Button(
                            onClick = {
                                FirebaseFirestore.getInstance().collection("requests")
                                    .document(request.requestId)
                                    .update("requestStatus", "fulfilled")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Text("Mark Fulfilled", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showSnackbar("This request already has donor responses.") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel Request", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = { },
                            modifier = Modifier.weight(1f),
                            enabled = false,
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text("Your Request", fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Cancel Request", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Button(
                        onClick = { 
                            if (currentUid.isEmpty()) return@Button
                            isAccepting = true
                            val db = FirebaseFirestore.getInstance()
                            val docRef = db.collection("requests").document(request.requestId)
                            db.runTransaction { transaction ->
                                val snapshot = transaction.get(docRef)
                                val reqUid = snapshot.getString("requesterUid")
                                if (reqUid == currentUid) {
                                    throw com.google.firebase.firestore.FirebaseFirestoreException(
                                        "Cannot accept your own request.", 
                                        com.google.firebase.firestore.FirebaseFirestoreException.Code.ABORTED
                                    )
                                }
                                val responders = snapshot.get("responders") as? List<String> ?: emptyList()
                                if (!responders.contains(currentUid)) {
                                    val newResponders = responders + currentUid
                                    transaction.update(docRef, "responders", newResponders)
                                    val count = snapshot.getLong("donorResponsesCount") ?: 0
                                    transaction.update(docRef, "donorResponsesCount", count + 1)
                                }
                            }.addOnSuccessListener {
                                isAccepting = false
                                Toast.makeText(context, "Request Accepted!", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                isAccepting = false
                                Toast.makeText(context, "Failed to accept", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !request.responders.contains(currentUid) && !isAccepting,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF212121))
                    ) {
                        if (isAccepting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text(if (request.responders.contains(currentUid)) "Accepted" else "Accept", fontWeight = FontWeight.Bold)
                        }
                    }
                    OutlinedButton(
                        onClick = { 
                            Toast.makeText(context, "Notes: ${request.additionalNotes}", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text("Details", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}
