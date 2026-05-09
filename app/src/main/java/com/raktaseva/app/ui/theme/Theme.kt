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
    primary = Color(0xFFFFB4AB),
    secondary = Color(0xFFFFDAD6),
    tertiary = Color(0xFF8DE39F),
    background = Color(0xFF161212),
    surface = Color(0xFF211A1A),
    onPrimary = Color(0xFF690005),
    onSecondary = Color(0xFF410002),
    onTertiary = Color(0xFF003914),
    onBackground = Color(0xFFFFEDEA),
    onSurface = Color(0xFFFFEDEA),
    outline = Color(0xFF5E4A48),
    surfaceVariant = Color(0xFF332625),
    onSurfaceVariant = Color(0xFFE7CFCB)
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
