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
import androidx.compose.ui.platform.LocalContext
import com.raktaseva.app.ui.state.LocalUserState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
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
                onCheckedChange = { 
                    LocalUserState.darkThemeEnabled.value = it
                    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                    prefs.edit().putBoolean("dark_theme", it).apply()
                }
            )
            
            Divider(modifier = Modifier.padding(vertical = 16.dp))
            
            Text("About", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Rakta-Seva Connect", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
            Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Emergency blood donation coordination platform.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
