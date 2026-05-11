package com.raktaseva.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.raktaseva.app.ui.state.LocalUserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingToggleItem(
                label = "Enable Notifications",
                description = "Receive alerts for blood requests in your area",
                checked = LocalUserState.notificationsEnabled.value,
                onCheckedChange = { LocalUserState.notificationsEnabled.value = it }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingToggleItem(
                label = "Location Services",
                description = "Allow app to use your location for nearby donors",
                checked = LocalUserState.locationEnabled.value,
                onCheckedChange = { LocalUserState.locationEnabled.value = it }
            )
            
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            
            SettingToggleItem(
                label = "Dark Theme",
                description = "Enable dark mode for the app",
                checked = LocalUserState.darkThemeEnabled.value,
                onCheckedChange = { LocalUserState.darkThemeEnabled.value = it }
            )
        }
    }
}

@Composable
fun SettingToggleItem(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
