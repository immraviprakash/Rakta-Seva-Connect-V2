package com.raktaseva.app.ui.screens.profile

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.raktaseva.app.ui.theme.Dimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current

    // Theme mode: "system", "light", "dark"
    val themeMode = LocalUserState.themeMode.value
    var showThemeDialog by remember { mutableStateOf(false) }

    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = {
                Text("Choose Theme", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    listOf("system" to "Follow System", "light" to "Light", "dark" to "Dark").forEach { (mode, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = themeMode == mode,
                                onClick = {
                                    LocalUserState.themeMode.value = mode
                                    // Backward compat: update darkThemeEnabled based on selection
                                    LocalUserState.darkThemeEnabled.value = mode == "dark"
                                    val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                                    prefs.edit()
                                        .putString("theme_mode", mode)
                                        .putBoolean("dark_theme", mode == "dark")
                                        .apply()
                                    showThemeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(Dimens.cardRadius)
        )
    }

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
                .padding(horizontal = Dimens.screenHorizontal, vertical = Dimens.screenVertical)
        ) {
            // ── Appearance Section ──
            Text(
                "Appearance",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.spacingSm)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showThemeDialog = true },
                shape = RoundedCornerShape(Dimens.cardRadius),
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier.padding(Dimens.cardPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            when (themeMode) {
                                "light" -> "Light"
                                "dark" -> "Dark"
                                else -> "Follow System"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingXxl))

            // ── Preferences Section ──
            Text(
                "Preferences",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.spacingSm)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.cardRadius),
                tonalElevation = 1.dp
            ) {
                Column {
                    SettingToggleItem(
                        label = "Notifications",
                        description = "Receive alerts for blood requests nearby",
                        checked = LocalUserState.notificationsEnabled.value,
                        onCheckedChange = { checked ->
                            LocalUserState.notificationsEnabled.value = checked
                            val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("notifications_enabled", checked).apply()
                        }
                    )
                    Divider(
                        modifier = Modifier.padding(horizontal = Dimens.cardPadding),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    SettingToggleItem(
                        label = "Location Services",
                        description = "Use your location for nearby donors",
                        checked = LocalUserState.locationEnabled.value,
                        onCheckedChange = { checked ->
                            LocalUserState.locationEnabled.value = checked
                            val prefs = context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
                            prefs.edit().putBoolean("location_enabled", checked).apply()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingXxl))

            // ── About Section ──
            Text(
                "About",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = Dimens.spacingSm)
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Dimens.cardRadius),
                tonalElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(Dimens.cardPadding)) {
                    Text("Rakta-Seva Connect", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                    Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(Dimens.spacingXs))
                    Text(
                        "Emergency blood donation coordination platform.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun SettingToggleItem(label: String, description: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Dimens.cardPadding, vertical = Dimens.spacingMd),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
