package com.raktaseva.app.ui.screens.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.raktaseva.app.data.model.Notification
import com.raktaseva.app.data.model.BloodRequest
import com.raktaseva.app.ui.state.LocalUserState
import com.raktaseva.app.ui.theme.Dimens
import androidx.compose.foundation.layout.offset
import java.text.SimpleDateFormat
import java.util.Locale
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var notifications by remember { mutableStateOf(emptyList<Notification>()) }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("notifications")
            .whereEqualTo("targetUserUid", LocalUserState.uid.value)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    android.widget.Toast.makeText(context, "Error loading alerts: ${error.localizedMessage}", android.widget.Toast.LENGTH_LONG).show()
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    notifications = snapshot.documents.mapNotNull { it.toObject(Notification::class.java) }
                    isLoading = false
                }
            }
        onDispose { listener.remove() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifications", fontWeight = FontWeight.Bold) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(Dimens.screenHorizontal), contentAlignment = Alignment.Center) {
                    Text("No notifications yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                    contentPadding = PaddingValues(top = Dimens.spacingSm, bottom = Dimens.screenVertical)
                ) {
                    items(notifications, key = { it.notificationId }) { notif ->
                        if (notif.type == "direct_request") {
                            var bloodRequest by remember(notif.relatedRequestId) { mutableStateOf<BloodRequest?>(null) }
                            var isFetchingRequest by remember(notif.relatedRequestId) { mutableStateOf(false) }

                            DisposableEffect(notif.relatedRequestId) {
                                var listener: com.google.firebase.firestore.ListenerRegistration? = null
                                if (notif.relatedRequestId.isNotEmpty()) {
                                    isFetchingRequest = true
                                    val db = FirebaseFirestore.getInstance()
                                    listener = db.collection("requests").document(notif.relatedRequestId)
                                        .addSnapshotListener { doc, error ->
                                            isFetchingRequest = false
                                            if (error == null && doc != null && doc.exists()) {
                                                bloodRequest = doc.toObject(BloodRequest::class.java)
                                            }
                                        }
                                } else {
                                    isFetchingRequest = false
                                }
                                onDispose {
                                    listener?.remove()
                                }
                            }
                            DirectRequestCard(notification = notif, bloodRequest = bloodRequest, isFetching = isFetchingRequest)
                        } else if (notif.type == "accepted") {
                            var bloodRequest by remember(notif.relatedRequestId) { mutableStateOf<BloodRequest?>(null) }
                            var isFetchingRequest by remember(notif.relatedRequestId) { mutableStateOf(false) }

                            DisposableEffect(notif.relatedRequestId) {
                                var listener: com.google.firebase.firestore.ListenerRegistration? = null
                                if (notif.relatedRequestId.isNotEmpty()) {
                                    isFetchingRequest = true
                                    val db = FirebaseFirestore.getInstance()
                                    listener = db.collection("requests").document(notif.relatedRequestId)
                                        .addSnapshotListener { doc, error ->
                                            isFetchingRequest = false
                                            if (error == null && doc != null && doc.exists()) {
                                                bloodRequest = doc.toObject(BloodRequest::class.java)
                                            }
                                        }
                                } else {
                                    isFetchingRequest = false
                                }
                                onDispose {
                                    listener?.remove()
                                }
                            }
                            AcceptanceConfirmationCard(notification = notif, bloodRequest = bloodRequest, isFetching = isFetchingRequest)
                        } else {
                            NotificationCard(notif)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationCard(notification: Notification) {
    val db = FirebaseFirestore.getInstance()

    val cardColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }

    Card(
        onClick = {
            if (!notification.isRead) {
                db.collection("notifications").document(notification.notificationId).update("isRead", true)
            }
        },
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        shape = RoundedCornerShape(Dimens.cardRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Dimens.cardPadding, vertical = Dimens.spacingMd),
            verticalAlignment = Alignment.Top
        ) {
            // Unread indicator dot
            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .offset(y = 6.dp)
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(50))
                ) {}
                Spacer(modifier = Modifier.width(Dimens.spacingMd))
            }
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = notification.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    val dateStr = try {
                        val sdf = SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault())
                        sdf.format(notification.createdAt.toDate())
                    } catch (e: Exception) {
                        "Just now"
                    }
                    Text(
                        text = dateStr,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(Dimens.spacingXs))
                Text(
                    text = notification.message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun DirectRequestCard(
    notification: Notification,
    bloodRequest: BloodRequest?,
    isFetching: Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = FirebaseFirestore.getInstance()
    val currentUid = LocalUserState.uid.value
    var isProcessing by remember { mutableStateOf(false) }

    val cardColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(Dimens.cardRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = notification.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val dateStr = try {
                    val sdf = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
                    sdf.format(notification.createdAt.toDate())
                } catch (e: Exception) {
                    "Just now"
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacingXs))
            Text(
                text = notification.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            if (isFetching) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            } else if (bloodRequest != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("Patient: ${bloodRequest.requesterName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Blood Group: ${bloodRequest.bloodGroup} · Units: ${bloodRequest.unitsRequired}", style = MaterialTheme.typography.bodyMedium)
                    Text("Hospital: ${bloodRequest.hospitalName}", style = MaterialTheme.typography.bodyMedium)
                    if (bloodRequest.additionalNotes.isNotBlank()) {
                        Text("Notes: ${bloodRequest.additionalNotes}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val isLocked = bloodRequest.acceptedByUid.isNotEmpty()
                    if (isLocked) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (bloodRequest.acceptedByUid == currentUid) "Accepted by You" else "Accepted by ${bloodRequest.acceptedByName}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                if (!notification.isRead && bloodRequest.requestStatus == "active" && bloodRequest.acceptedByUid.isEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Button(
                            onClick = {
                                isProcessing = true
                                val docRef = db.collection("requests").document(bloodRequest.requestId)
                                
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

                                    val currentNotifRef = db.collection("notifications").document(notification.notificationId)
                                    transaction.update(currentNotifRef, "isRead", true)

                                    val confirmationNotifRef = db.collection("notifications").document()
                                    val confirmationNotif = hashMapOf(
                                        "notificationId" to confirmationNotifRef.id,
                                        "targetUserUid" to bloodRequest.requesterUid,
                                        "title" to "Direct Request Accepted",
                                        "message" to "${LocalUserState.name.value} has accepted your direct request for ${bloodRequest.bloodGroup} blood.",
                                        "type" to "accepted",
                                        "relatedRequestId" to bloodRequest.requestId,
                                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                        "isRead" to false
                                    )
                                    transaction.set(confirmationNotifRef, confirmationNotif)
                                    null
                                }.addOnSuccessListener {
                                    isProcessing = false
                                    android.widget.Toast.makeText(context, "Request Accepted!", android.widget.Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener { e ->
                                    isProcessing = false
                                    android.widget.Toast.makeText(context, e.message ?: "Failed to accept request", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Accept Request")
                        }

                        OutlinedButton(
                            onClick = {
                                isProcessing = true
                                val docRef = db.collection("requests").document(bloodRequest.requestId)
                                db.runTransaction { transaction ->
                                    transaction.update(docRef, "status", "rejected")
                                    
                                    val currentNotifRef = db.collection("notifications").document(notification.notificationId)
                                    transaction.update(currentNotifRef, "isRead", true)
                                    
                                    val rejectionNotifRef = db.collection("notifications").document()
                                    val rejectionNotif = hashMapOf(
                                        "notificationId" to rejectionNotifRef.id,
                                        "targetUserUid" to bloodRequest.requesterUid,
                                        "title" to "Direct Request Rejected",
                                        "message" to "${LocalUserState.name.value} has declined your direct request for ${bloodRequest.bloodGroup} blood.",
                                        "type" to "rejected",
                                        "relatedRequestId" to bloodRequest.requestId,
                                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                        "isRead" to false
                                    )
                                    transaction.set(rejectionNotifRef, rejectionNotif)
                                    null
                                }.addOnSuccessListener {
                                    isProcessing = false
                                    android.widget.Toast.makeText(context, "Request Rejected", android.widget.Toast.LENGTH_SHORT).show()
                                }.addOnFailureListener { e ->
                                    isProcessing = false
                                    android.widget.Toast.makeText(context, e.message ?: "Failed to reject", android.widget.Toast.LENGTH_LONG).show()
                                }
                            },
                            enabled = !isProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Reject")
                        }
                    }
                } else {
                    val statusText = if (bloodRequest.requestStatus != "active") {
                        "Request ${bloodRequest.requestStatus.replaceFirstChar { it.uppercase() }}"
                    } else if (bloodRequest.acceptedByUid.isNotEmpty()) {
                        if (bloodRequest.acceptedByUid == currentUid) "Accepted by You" else "Accepted by ${bloodRequest.acceptedByName}"
                    } else {
                        "Handled"
                    }
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            } else {
                Text(
                    text = "Request no longer available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun AcceptanceConfirmationCard(
    notification: Notification,
    bloodRequest: BloodRequest?,
    isFetching: Boolean
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = FirebaseFirestore.getInstance()
    var isProcessing by remember { mutableStateOf(false) }

    var showVerificationDialog by remember { mutableStateOf(false) }
    var showFinalConfirmDialog by remember { mutableStateOf(false) }

    // Questionnaire Answers State
    var receivedBloodAns by remember { mutableStateOf<String?>(null) }
    var donorPresentAns by remember { mutableStateOf<String?>(null) }
    var receivedTimePeriodAns by remember { mutableStateOf<String?>(null) }
    var experienceRatingAns by remember { mutableStateOf<String?>(null) }
    var feedbackAns by remember { mutableStateOf("") }

    val cardColor = if (notification.isRead) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    } else {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
    }

    val isClickable = bloodRequest != null && bloodRequest.status == "accepted"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable && !isProcessing) {
                showVerificationDialog = true
            },
        colors = CardDefaults.cardColors(containerColor = cardColor),
        shape = RoundedCornerShape(Dimens.cardRadius),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(Dimens.cardPadding)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Donation Accepted",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val dateStr = try {
                    val sdf = java.text.SimpleDateFormat("MMM dd, hh:mm a", java.util.Locale.getDefault())
                    sdf.format(notification.createdAt.toDate())
                } catch (e: Exception) {
                    "Just now"
                }
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacingXs))
            
            Text(
                text = if (bloodRequest != null) {
                    "${bloodRequest.acceptedByName} has accepted your donation request. Please confirm only after the blood donation has been successfully completed."
                } else {
                    "Your donation request has been accepted. Please confirm only after the blood donation has been successfully completed."
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            if (isFetching) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp))
                }
            } else if (bloodRequest != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Text("Donor: ${bloodRequest.acceptedByName}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Blood Group: ${bloodRequest.bloodGroup} · Units: ${bloodRequest.unitsRequired}", style = MaterialTheme.typography.bodyMedium)
                    Text("Hospital: ${bloodRequest.hospitalName}", style = MaterialTheme.typography.bodyMedium)
                    val statusText = when (bloodRequest.status) {
                        "accepted" -> "Accepted"
                        "completed" -> "Completed"
                        "cancelled" -> "Cancelled"
                        "rejected" -> "Rejected"
                        else -> "Pending"
                    }
                    Text("Status: $statusText", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                if (bloodRequest.status == "accepted") {
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                    Button(
                        onClick = {
                            showVerificationDialog = true
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Confirm Donation Received")
                    }
                } else if (bloodRequest.status == "completed") {
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                    Text(
                        text = "Donation successfully recorded. Thank you for using Rakta-Seva Connect.",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                    Text(
                        text = "This request is no longer active (Status: ${bloodRequest.status.replaceFirstChar { it.uppercase() }}).",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            } else {
                Text(
                    text = "Request no longer available.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showVerificationDialog) {
        DonationVerificationDialog(
            onDismiss = { showVerificationDialog = false },
            onSubmit = { received, present, period, rating, feedbackText ->
                receivedBloodAns = received
                donorPresentAns = present
                receivedTimePeriodAns = period
                experienceRatingAns = rating
                feedbackAns = feedbackText
                showVerificationDialog = false
                showFinalConfirmDialog = true
            }
        )
    }

    if (showFinalConfirmDialog && bloodRequest != null) {
        FinalConfirmDialog(
            onDismiss = { showFinalConfirmDialog = false },
            onConfirm = {
                showFinalConfirmDialog = false
                isProcessing = true
                val reqRef = db.collection("requests").document(bloodRequest.requestId)
                val currentNotifRef = db.collection("notifications").document(notification.notificationId)
                val donorUid = bloodRequest.acceptedByUid

                val todayStr = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date())

                db.runTransaction { transaction ->
                    transaction.update(reqRef, "status", "completed")
                    transaction.update(reqRef, "requestStatus", "fulfilled")

                    val donationRef = db.collection("donations").document()
                    val donationRecord = hashMapOf(
                        "donorUid" to donorUid,
                        "donorName" to bloodRequest.acceptedByName,
                        "recipientName" to bloodRequest.requesterName,
                        "bloodGroup" to bloodRequest.bloodGroup,
                        "units" to bloodRequest.unitsRequired,
                        "hospitalName" to bloodRequest.hospitalName,
                        "requestId" to bloodRequest.requestId,
                        "donationDate" to todayStr,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "completedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "experienceRating" to experienceRatingAns,
                        "feedback" to feedbackAns,
                        "receivedTimePeriod" to receivedTimePeriodAns,
                        "verifiedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                    )
                    transaction.set(donationRef, donationRecord)

                    val donorNotifRef = db.collection("notifications").document()
                    val completionMsg = buildString {
                        append("${bloodRequest.requesterName} has confirmed that the blood donation was successfully received.\n\n")
                        append("Hospital:\n${bloodRequest.hospitalName}\n\n")
                        append("Donation Date:\n$todayStr\n\n")
                        append("Thank you for donating blood and helping save a life.\n\n")
                        append("For your safety and recovery, you will not be eligible to donate blood again for the next 90 days.\n\n")
                        append("You can make yourself available again once the recovery period has been completed.\n\n")
                        append("Thank you for using Rakta-Seva Connect.\n\n")
                        append("Save Blood. Save Lives.")
                    }
                    val donorNotif = hashMapOf(
                        "notificationId" to donorNotifRef.id,
                        "targetUserUid" to donorUid,
                        "title" to "Donation Confirmed",
                        "message" to completionMsg,
                        "type" to "donation_confirmed",
                        "relatedRequestId" to bloodRequest.requestId,
                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                        "isRead" to false
                    )
                    transaction.set(donorNotifRef, donorNotif)

                    transaction.update(currentNotifRef, "isRead", true)
                    null
                }.addOnSuccessListener {
                    isProcessing = false
                    android.widget.Toast.makeText(context, "Donation completed successfully!", android.widget.Toast.LENGTH_SHORT).show()
                }.addOnFailureListener { e ->
                    isProcessing = false
                    android.widget.Toast.makeText(context, e.message ?: "Failed to complete donation", android.widget.Toast.LENGTH_LONG).show()
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonationVerificationDialog(
    onDismiss: () -> Unit,
    onSubmit: (received: String, present: String, period: String, rating: String, feedbackText: String) -> Unit
) {
    var receivedBlood by remember { mutableStateOf<String?>(null) }
    var donorPresent by remember { mutableStateOf<String?>(null) }
    var receivedTimePeriod by remember { mutableStateOf<String?>(null) }
    var experienceRating by remember { mutableStateOf<String?>(null) }
    var feedback by remember { mutableStateOf("") }

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .heightIn(max = 580.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                Text(
                    text = "Donation Verification",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Q1
                Text(
                    text = "Did you successfully receive the blood donation? *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { receivedBlood = "Yes" }) {
                        RadioButton(
                            selected = receivedBlood == "Yes",
                            onClick = { receivedBlood = "Yes" }
                        )
                        Text("Yes", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { receivedBlood = "No" }) {
                        RadioButton(
                            selected = receivedBlood == "No",
                            onClick = { receivedBlood = "No" }
                        )
                        Text("No", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                if (receivedBlood == "No") {
                    Text(
                        text = "The donation cannot be marked as completed until blood has been successfully received.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Q2
                Text(
                    text = "Was the donor present and able to assist? *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { donorPresent = "Yes" }) {
                        RadioButton(
                            selected = donorPresent == "Yes",
                            onClick = { donorPresent = "Yes" }
                        )
                        Text("Yes", style = MaterialTheme.typography.bodyMedium)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { donorPresent = "No" }) {
                        RadioButton(
                            selected = donorPresent == "No",
                            onClick = { donorPresent = "No" }
                        )
                        Text("No", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Q3
                Text(
                    text = "Approximately when did you receive the blood donation? *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val periods = listOf(
                    "Morning (6 AM – 12 PM)",
                    "Afternoon (12 PM – 5 PM)",
                    "Evening (5 PM – 9 PM)",
                    "Night (9 PM – 6 AM)"
                )
                periods.forEach { period ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { receivedTimePeriod = period }
                    ) {
                        RadioButton(
                            selected = receivedTimePeriod == period,
                            onClick = { receivedTimePeriod = period }
                        )
                        Text(period, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Q4
                Text(
                    text = "How was your experience with the donor? *",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                val ratings = listOf("Excellent", "Good", "Average", "Poor")
                ratings.forEach { rating ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { experienceRating = rating }
                    ) {
                        RadioButton(
                            selected = experienceRating == rating,
                            onClick = { experienceRating = rating }
                        )
                        Text(rating, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // Feedback
                OutlinedTextField(
                    value = feedback,
                    onValueChange = { if (it.length <= 200) feedback = it },
                    label = { Text("Share your experience with the donor (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    supportingText = {
                        Text(
                            text = "${feedback.length}/200",
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    val canSubmit = receivedBlood == "Yes" &&
                                   donorPresent != null &&
                                   receivedTimePeriod != null &&
                                   experienceRating != null
                    Button(
                        onClick = {
                            if (canSubmit) {
                                onSubmit(
                                    receivedBlood!!,
                                    donorPresent!!,
                                    receivedTimePeriod!!,
                                    experienceRating!!,
                                    feedback
                                )
                            }
                        },
                        enabled = canSubmit
                    ) {
                        Text("Submit")
                    }
                }
            }
        }
    }
}

@Composable
fun FinalConfirmDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Confirm Donation Completion",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "You are about to confirm that the blood donation was successfully completed.\n\nThis will:\n• Record the donation in history\n• Mark the request as completed\n• Start the donor's 90-day recovery period\n\nThis action cannot be undone.",
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
