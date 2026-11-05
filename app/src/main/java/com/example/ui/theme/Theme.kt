package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DayPilotDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = DarkNavyBackground,
    primaryContainer = Color(0xFF00363D),
    onPrimaryContainer = ElectricCyan,
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF381E72),
    onSecondaryContainer = SoftViolet,
    tertiary = CyanAccent,
    onTertiary = DarkNavyBackground,
    background = DarkNavyBackground,
    onBackground = TextPrimary,
    surface = DarkNavySurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkNavySurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkNavyBorder,
    error = RoseError,
    onError = Color.White
)

@Composable
fun DayPilotTheme(
    darkTheme: Boolean = true, // Default to sleek dark mode
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DayPilotDarkColorScheme,
        typography = Typography,
        content = content
    )
}
