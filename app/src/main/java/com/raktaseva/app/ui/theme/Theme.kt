package com.raktaseva.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val PrimaryRed = Color(0xFFD32F2F)
private val PrimaryDark = Color(0xFFB71C1C)
private val SuccessGreen = Color(0xFF4CAF50)
private val AppBackground = Color(0xFFFAFAFA)
private val TextMain = Color(0xFF1A1A1A)
private val TextMuted = Color(0xFF757575)
private val BorderColor = Color(0xFFEEEEEE)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryRed,
    secondary = PrimaryDark,
    tertiary = SuccessGreen,
    background = AppBackground,
    surface = Color.White,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextMain,
    onSurface = TextMain,
    outline = BorderColor,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = TextMuted
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFED4245), // Deep crimson/red
    secondary = Color(0xFFF26567),
    tertiary = Color(0xFF4CAF50), // Success Green
    background = Color(0xFF1E1F22), // Soft charcoal background
    surface = Color(0xFF2B2D31), // Layered dark surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFFF2F3F5), // Primary readable text
    onSurface = Color(0xFFDBDEE1), // Primary surface text
    outline = Color(0xFF3F4147), // Subtle elevation contrast
    surfaceVariant = Color(0xFF313338), // Lighter surface
    onSurfaceVariant = Color(0xFFB5BAC1) // Muted grayscale hierarchy
)

@Composable
fun RaktaSevaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
