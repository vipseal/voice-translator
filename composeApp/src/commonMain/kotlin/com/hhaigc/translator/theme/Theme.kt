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

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF6200EE),
    primaryContainer = Color(0xFFEDE0FF),
    secondary = Color(0xFF625B71),
    secondaryContainer = Color(0xFFE8DEF8),
    tertiary = Color(0xFF018786),
    background = Color(0xFFFFFBFF),
    surface = Color(0xFFF8F5FF),
    surfaceVariant = Color(0xFFEEE8F4),
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    onSurfaceVariant = Color(0xFF49454E),
    outline = Color(0xFF7A757F),
    outlineVariant = Color(0xFFCAC4CF),
    error = Color(0xFFB3261E),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

enum class ThemeMode { AUTO, DARK, LIGHT }

@Composable
fun VoiceTranslatorTheme(
    themeMode: ThemeMode = ThemeMode.AUTO,
    content: @Composable () -> Unit
) {
    val isDark = when (themeMode) {
        ThemeMode.AUTO -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    MaterialTheme(
        colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
        typography = Typography(),
        content = content
    )
}
