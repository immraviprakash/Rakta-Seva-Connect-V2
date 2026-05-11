package com.raktaseva.app.ui.screens.donors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.raktaseva.app.ui.components.BloodGroupBadge
import com.raktaseva.app.data.model.UserProfile
import com.raktaseva.app.ui.state.LocalUserState
import com.raktaseva.app.utils.BloodCompatibility
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorsScreen() {
    var searchQuery by remember { mutableStateOf("") }
    val donors = remember { mutableStateListOf<UserProfile>() }
    var isLoading by remember { mutableStateOf(true) }

    DisposableEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        val listener = db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    isLoading = false
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    donors.clear()
                    for (doc in snapshot.documents) {
                        val donor = doc.toObject(UserProfile::class.java)
                        if (donor != null && donor.uid != LocalUserState.uid.value) {
                            donors.add(donor)
                        }
                    }
                    isLoading = false
                }
            }
        onDispose { listener.remove() }
    }

    // Filter logic
    val filteredDonors = donors.filter { donor ->
        val query = searchQuery.trim()
        if (query.isEmpty()) return@filter true
        
        val matchesName = donor.fullName.contains(query, ignoreCase = true)
        
        // If query looks like a blood group (e.g. "A+", "O-"), use compatibility matching
        val isBloodGroupQuery = query.uppercase() in listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")
        val matchesBloodGroup = if (isBloodGroupQuery) {
            // "If request needs A+: Show only A+, A-, O+, O-"
            // This means donor must be compatible with the needed group (query)
            BloodCompatibility.isCompatibleDonor(donorGroup = donor.bloodGroup, neededGroup = query)
        } else {
            donor.bloodGroup.equals(query, ignoreCase = true)
        }
        
        matchesName || matchesBloodGroup
    }

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
                placeholder = { Text("Search by name or needed blood group (e.g. A+)") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (filteredDonors.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No donors found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredDonors) { donor ->
                        DonorCard(donor)
                    }
                }
            }
        }
    }
}

@Composable
fun DonorCard(donor: UserProfile) {
    val isEligible = isDonorEligible(donor.lastDonationDate)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEligible) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            BloodGroupBadge(group = donor.bloodGroup, size = 44)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(donor.fullName, fontWeight = FontWeight.Bold)
                
                if (donor.lastDonationDate.isNotBlank()) {
                    Text("Last Donated: ${donor.lastDonationDate}", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Last Donated: Never", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(4.dp))
                
                if (isEligible) {
                    Surface(color = Color(0xFFE8F5E9), shape = RoundedCornerShape(4.dp)) {
                        Text("Eligible Donor", style = MaterialTheme.typography.labelSmall, color = Color(0xFF2E7D32), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                } else {
                    Surface(color = Color(0xFFFFEBEE), shape = RoundedCornerShape(4.dp)) {
                        Text("Cooldown Active", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }
            }
            if (donor.mobileNumber.isNotBlank()) {
                IconButton(onClick = { /* Contact logic */ }) {
                    Icon(Icons.Default.Phone, contentDescription = "Contact", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

fun isDonorEligible(lastDonationDate: String): Boolean {
    if (lastDonationDate.isBlank()) return true
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val lastDate = sdf.parse(lastDonationDate) ?: return true
        val diffInMillies = Math.abs(Date().time - lastDate.time)
        val days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS)
        days >= 90
    } catch (e: Exception) {
        try {
            // Check for format ddMMyyyy as used in RegistrationScreen
            val sdf2 = SimpleDateFormat("ddMMyyyy", Locale.getDefault())
            val lastDate2 = sdf2.parse(lastDonationDate) ?: return true
            val diffInMillies2 = Math.abs(Date().time - lastDate2.time)
            val days2 = TimeUnit.DAYS.convert(diffInMillies2, TimeUnit.MILLISECONDS)
            days2 >= 90
        } catch (e2: Exception) {
            true
        }
    }
}
