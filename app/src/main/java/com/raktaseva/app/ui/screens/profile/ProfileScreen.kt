package com.raktaseva.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raktaseva.app.ui.components.BloodGroupBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onChatClick: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Profile", fontWeight = FontWeight.Bold) })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            BloodGroupBadge(group = "O+", size = 80)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Aman Gupta", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("aman.gupta@example.com", color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                ProfileStatItem(label = "Donations", value = "5")
                ProfileStatItem(label = "Lives Saved", value = "12")
                ProfileStatItem(label = "Eligibility", value = "Eligible")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            ProfileMenuItem(icon = Icons.Default.Edit, label = "Edit Profile")
            ProfileMenuItem(icon = Icons.Default.List, label = "Donation History")
            ProfileMenuItem(icon = Icons.Default.Settings, label = "Settings")
            
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
            
            Spacer(modifier = Modifier.weight(1f))
            
            TextButton(onClick = { /* Logout */ }, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
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
fun ProfileMenuItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        onClick = { /* Menu click */ },
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
