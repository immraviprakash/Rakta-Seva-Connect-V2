package com.raktaseva.app.ui.screens.donors

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.raktaseva.app.ui.components.BloodGroupBadge
import com.raktaseva.app.data.model.UserProfile
import com.raktaseva.app.ui.state.LocalUserState
import com.raktaseva.app.ui.theme.Dimens
import com.raktaseva.app.utils.BloodCompatibility
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import androidx.compose.foundation.clickable
import androidx.compose.material3.RadioButton
import androidx.compose.material.icons.filled.Send
import android.widget.Toast

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    var showEligibleOnly by remember { mutableStateOf(false) }
    var showCompatibleOnly by remember { mutableStateOf(false) }
    var donors by remember { mutableStateOf(emptyList<UserProfile>()) }
    var isLoading by remember { mutableStateOf(true) }

    var activeRequests by remember { mutableStateOf(emptyList<com.raktaseva.app.data.model.BloodRequest>()) }
    var selectedDonorForRequest by remember { mutableStateOf<UserProfile?>(null) }
    var showRequestDialog by remember { mutableStateOf(false) }
    var isFetchingRequests by remember { mutableStateOf(false) }

    val context = androidx.compose.ui.platform.LocalContext.current

    fun handleRequestClick(donor: UserProfile) {
        isFetchingRequests = true
        val db = FirebaseFirestore.getInstance()
        db.collection("requests")
            .whereEqualTo("requesterUid", LocalUserState.uid.value)
            .whereEqualTo("requestStatus", "active")
            .get()
            .addOnSuccessListener { snapshot ->
                isFetchingRequests = false
                if (snapshot == null || snapshot.isEmpty) {
                    Toast.makeText(context, "No active emergency requests found. Please create a request first.", Toast.LENGTH_LONG).show()
                } else {
                    val requestsList = snapshot.documents.mapNotNull { it.toObject(com.raktaseva.app.data.model.BloodRequest::class.java) }
                    activeRequests = requestsList
                    selectedDonorForRequest = donor
                    showRequestDialog = true
                }
            }
            .addOnFailureListener { e ->
                isFetchingRequests = false
                Toast.makeText(context, "Error checking requests: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
            }
    }

    if (showRequestDialog && selectedDonorForRequest != null) {
        val donorName = selectedDonorForRequest!!.fullName
        val donorUid = selectedDonorForRequest!!.uid
        
        var selectedRequestIndex by remember { mutableStateOf(0) }
        var isSendingDirectRequest by remember { mutableStateOf(false) }
        
        AlertDialog(
            onDismissRequest = { showRequestDialog = false },
            title = { Text("Send Donation Request") },
            text = {
                Column {
                    if (activeRequests.size == 1) {
                        val req = activeRequests[0]
                        Text("Send a direct donation request to $donorName for blood group ${req.bloodGroup} at ${req.hospitalName}?")
                    } else {
                        Text("Select which active request to send to $donorName:")
                        Spacer(modifier = Modifier.height(8.dp))
                        activeRequests.forEachIndexed { index, req ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(
                                        if (selectedRequestIndex == index) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Unspecified,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(8.dp)
                                    .clickable { selectedRequestIndex = index },
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedRequestIndex == index,
                                    onClick = { selectedRequestIndex = index }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("${req.bloodGroup} required at ${req.hospitalName}", fontWeight = FontWeight.Bold)
                                    Text("Urgency: ${req.urgencyLevel} · Units: ${req.unitsRequired}")
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isSendingDirectRequest,
                    onClick = {
                        isSendingDirectRequest = true
                        val selectedRequest = activeRequests[selectedRequestIndex]
                        val db = FirebaseFirestore.getInstance()
                        val patientUid = LocalUserState.uid.value

                        db.collection("requests")
                            .whereEqualTo("requesterUid", patientUid)
                            .whereIn("status", listOf("pending", "accepted"))
                            .get()
                            .addOnSuccessListener { querySnapshot ->
                                var hasActiveRequest = false
                                if (querySnapshot != null) {
                                    for (doc in querySnapshot.documents) {
                                        val contacted = doc.get("contactedDonors") as? List<*>
                                        val acceptedBy = doc.getString("acceptedByUid") ?: ""
                                        if ((contacted != null && contacted.contains(donorUid)) || acceptedBy == donorUid) {
                                            hasActiveRequest = true
                                            break
                                        }
                                    }
                                }
                                if (hasActiveRequest) {
                                    isSendingDirectRequest = false
                                    showRequestDialog = false
                                    Toast.makeText(context, "Request already exists and has been sent to the donor.", Toast.LENGTH_LONG).show()
                                } else {
                                    val notifRef = db.collection("notifications").document()
                                    val notification = hashMapOf(
                                        "notificationId" to notifRef.id,
                                        "targetUserUid" to donorUid,
                                        "senderUid" to patientUid,
                                        "title" to "Direct Donation Request",
                                        "message" to "${LocalUserState.name.value} has requested you to donate ${selectedRequest.bloodGroup} blood at ${selectedRequest.hospitalName}.",
                                        "type" to "direct_request",
                                        "relatedRequestId" to selectedRequest.requestId,
                                        "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                                        "isRead" to false
                                    )
                                    
                                    val reqRef = db.collection("requests").document(selectedRequest.requestId)
                                    val batch = db.batch()
                                    batch.set(notifRef, notification)
                                    batch.update(reqRef, "contactedDonors", com.google.firebase.firestore.FieldValue.arrayUnion(donorUid))
                                    
                                    batch.commit()
                                        .addOnSuccessListener {
                                            isSendingDirectRequest = false
                                            showRequestDialog = false
                                            Toast.makeText(context, "Request sent successfully!", Toast.LENGTH_SHORT).show()
                                        }
                                        .addOnFailureListener { e ->
                                            isSendingDirectRequest = false
                                            Toast.makeText(context, "Failed to send request: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                            .addOnFailureListener { e ->
                                isSendingDirectRequest = false
                                Toast.makeText(context, "Error checking active requests: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
                            }
                    }
                ) {
                    if (isSendingDirectRequest) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp))
                    } else {
                        Text("Send Request")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("users")
            .whereEqualTo("isAvailable", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val list = snapshot.documents.mapNotNull { doc ->
                        val donor = doc.toObject(UserProfile::class.java)
                        if (donor != null && donor.uid != LocalUserState.uid.value) donor else null
                    }
                    donors = list.sortedByDescending { it.lastDonationDate }
                    isLoading = false
                }
            }
        onDispose { listener.remove() }
    }

    // Filter logic
    val filteredDonors = donors.filter { donor ->
        if (showEligibleOnly && !isDonorEligible(donor.lastDonationDate)) return@filter false
        if (showCompatibleOnly) {
            val userGroup = LocalUserState.bloodGroup.value
            if (userGroup.isNotBlank() && !BloodCompatibility.isCompatibleDonor(donorGroup = donor.bloodGroup, neededGroup = userGroup)) {
                return@filter false
            }
        }

        val query = searchQuery.trim()
        if (query.isEmpty()) return@filter true
        
        val matchesName = donor.fullName.contains(query, ignoreCase = true)
        
        // If query looks like a blood group (e.g. "A+", "O-"), use compatibility matching
        val isBloodGroupQuery = query.uppercase() in listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val matchesBloodGroup = if (isBloodGroupQuery) {
            BloodCompatibility.isCompatibleDonor(donorGroup = donor.bloodGroup, neededGroup = query)
        } else {
            donor.bloodGroup.equals(query, ignoreCase = true)
        }
        
        matchesName || matchesBloodGroup
    }.sortedByDescending { isDonorEligible(it.lastDonationDate) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Find Donors", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by name or blood group") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenHorizontal, vertical = Dimens.spacingSm),
                shape = RoundedCornerShape(Dimens.cardRadius),
                singleLine = true
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.screenHorizontal, vertical = Dimens.spacingXs),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
            ) {
                FilterChip(
                    selected = showEligibleOnly,
                    onClick = { showEligibleOnly = !showEligibleOnly },
                    label = { Text("Eligible Only") }
                )
                FilterChip(
                    selected = showCompatibleOnly,
                    onClick = { showCompatibleOnly = !showCompatibleOnly },
                    label = { Text("Compatible Only") }
                )
            }
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredDonors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(Dimens.screenHorizontal), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No donors found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Dimens.screenHorizontal),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingLg),
                    contentPadding = PaddingValues(top = Dimens.spacingSm, bottom = Dimens.screenVertical)
                ) {
                    items(filteredDonors, key = { it.uid }) { donor ->
                        DonorCard(
                            donor = donor,
                            showContact = true,
                            onSendRequest = { handleRequestClick(it) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DonorCard(
    donor: UserProfile, 
    showContact: Boolean = false, 
    embedded: Boolean = false,
    onSendRequest: (UserProfile) -> Unit = {}
) {
    val isEligible = isDonorEligible(donor.lastDonationDate)
    
    val content: @Composable () -> Unit = {
        Row(
            modifier = Modifier.padding(if (embedded) PaddingValues(vertical = Dimens.spacingSm) else PaddingValues(Dimens.cardPadding)),
            verticalAlignment = androidx.compose.ui.Alignment.Top
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                // Top Section: Blood group badge left, large donor name right, plus eligibility badge
                Row(
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    BloodGroupBadge(group = donor.bloodGroup, size = 40)
                    Spacer(modifier = Modifier.width(Dimens.spacingMd))
                    Column {
                        Text(
                            text = donor.fullName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingXxs))
                        if (isEligible) {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Eligible", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.errorContainer, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Cooldown", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }

                // Information Section: Vertically stacked elements to prevent awkward horizontal wrapping
                Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)) {
                    Column {
                        Text(
                            text = "Last Donation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                        Text(
                            text = if (donor.lastDonationDate.isNotBlank()) donor.lastDonationDate else "Never",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (donor.mobileNumber.isNotBlank()) {
                        Column {
                            Text(
                                text = "Phone",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                            Text(
                                text = donor.mobileNumber,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                }
            }

            if (showContact) {
                val context = androidx.compose.ui.platform.LocalContext.current
                Spacer(modifier = Modifier.width(Dimens.spacingMd))
                Column(
                    modifier = Modifier.width(115.dp),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm),
                    horizontalAlignment = androidx.compose.ui.Alignment.End
                ) {
                    if (donor.mobileNumber.isNotBlank()) {
                        Button(
                            onClick = { 
                                try {
                                    val number = if (donor.mobileNumber.startsWith("+")) donor.mobileNumber else "+91${donor.mobileNumber}"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_DIAL).apply {
                                        data = android.net.Uri.parse("tel:$number")
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    android.widget.Toast.makeText(context, "Cannot open dialer", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            shape = RoundedCornerShape(Dimens.buttonRadius),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Row(
                                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                              ) {
                                Icon(Icons.Default.Phone, contentDescription = "Call", modifier = Modifier.size(16.dp))
                                Text("Call", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    
                    Button(
                        onClick = {
                            val waNum = donor.mobileNumber.trim()
                            if (waNum.isBlank()) {
                                Toast.makeText(context, "Phone number is missing.", Toast.LENGTH_SHORT).show()
                            } else {
                                try {
                                    val cleanPhone = waNum.filter { it.isDigit() }
                                    val url = "https://wa.me/$cleanPhone"
                                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                        data = android.net.Uri.parse(url)
                                    }
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open WhatsApp", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                            contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                        ),
                        shape = RoundedCornerShape(Dimens.buttonRadius),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        modifier = Modifier.fillMaxWidth().height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "WhatsApp", modifier = Modifier.size(16.dp))
                            Text("WhatsApp", style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    if (donor.uid != LocalUserState.uid.value) {
                        OutlinedButton(
                            onClick = { onSendRequest(donor) },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(Dimens.buttonRadius),
                            contentPadding = PaddingValues(horizontal = 8.dp),
                            modifier = Modifier.fillMaxWidth().height(36.dp)
                        ) {
                            Text("Request", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (embedded) {
        // Render inline without Card wrapper — for use inside another Card
        Column(modifier = Modifier.fillMaxWidth()) {
            content()
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(Dimens.cardRadius),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isEligible) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
            )
        ) {
            content()
        }
    }
}

fun isDonorEligible(lastDonationDate: String): Boolean {
    if (lastDonationDate.isBlank()) return true
    val parsedDate = com.raktaseva.app.ui.state.LocalUserState.tryParseDate(lastDonationDate.trim()) ?: return true
    if (parsedDate.after(Date())) return false
    val diffInMillies = Date().time - parsedDate.time
    val days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
    return days >= 90
}
