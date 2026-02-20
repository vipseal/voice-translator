package com.hhaigc.translator.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Custom colors
private val DarkBackground = Color(0xFF0F0F1A)
private val DarkSurface = Color(0xFF1A1A2E)
private val DarkSurfaceVariant = Color(0xFF16213E)
private val Primary = Color(0xFF7C4DFF)
private val PrimaryContainer = Color(0xFF5E35B1)
private val OnBackground = Color(0xFFE8E8F2)
private val OnSurface = Color(0xFFE8E8F2)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    primaryContainer = PrimaryContainer,
    secondary = Color(0xFFBB86FC),
    secondaryContainer = Color(0xFF3F2A7A),
    tertiary = Color(0xFF03DAC6),
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = OnBackground,
    onSurface = OnSurface,
    onSurfaceVariant = Color(0xFFBBBBCC),
    outline = Color(0xFF444454),
    outlineVariant = Color(0xFF2A2A3A)
)

@Composable
fun VoiceTranslatorTheme(
    darkTheme: Boolean = true, // Always dark theme
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}