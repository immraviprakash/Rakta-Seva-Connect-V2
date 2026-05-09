package com.raktaseva.app.ui.screens.donors

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raktaseva.app.ui.components.BloodGroupBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DonorsScreen() {
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
            SearchBar(
                query = "",
                onQueryChange = {},
                onSearch = {},
                active = false,
                onActiveChange = {},
                placeholder = { Text("Search by blood group or location") },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) {}
            
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(10) {
                    DonorCard()
                }
            }
        }
    }
}

@Composable
fun DonorCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            BloodGroupBadge(group = "A+", size = 44)
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Rahul Sharma", fontWeight = FontWeight.Bold)
                Text("Last Donated: 4 months ago", style = MaterialTheme.typography.bodySmall)
                Text("Distance: 1.2 km", style = MaterialTheme.typography.labelSmall)
            }
            TextButton(onClick = { /* Contact logic */ }) {
                Text("Contact")
            }
        }
    }
}
