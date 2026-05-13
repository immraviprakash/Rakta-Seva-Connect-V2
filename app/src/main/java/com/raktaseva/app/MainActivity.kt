package com.raktaseva.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.raktaseva.app.ui.theme.RaktaSevaTheme
import com.raktaseva.app.navigation.AppNavigation
import com.raktaseva.app.ui.state.LocalUserState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE)
        
        // Load theme mode preference (default: "system")
        val themeMode = prefs.getString("theme_mode", "system") ?: "system"
        LocalUserState.themeMode.value = themeMode
        LocalUserState.darkThemeEnabled.value = prefs.getBoolean("dark_theme", false)
        
        enableEdgeToEdge()
        setContent {
            val isDark = resolveTheme(LocalUserState.themeMode.value)
            RaktaSevaTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

/**
 * Resolves the effective dark/light theme based on user preference.
 * "system" -> follows device setting
 * "dark"   -> forced dark
 * "light"  -> forced light
 */
@Composable
fun resolveTheme(mode: String): Boolean {
    return when (mode) {
        "dark" -> true
        "light" -> false
        else -> isSystemInDarkTheme()
    }
}
