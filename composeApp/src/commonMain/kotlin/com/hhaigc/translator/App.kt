package com.hhaigc.translator

import androidx.compose.runtime.*
import com.hhaigc.translator.screen.SettingsScreen
import com.hhaigc.translator.screen.TranslatorScreen
import com.hhaigc.translator.store.SettingsStore
import com.hhaigc.translator.theme.ThemeMode
import com.hhaigc.translator.theme.VoiceTranslatorTheme
import kotlinx.coroutines.launch

enum class Screen {
    Translator,
    Settings
}

@Composable
fun App() {
    val settingsStore = remember { SettingsStore() }
    val scope = rememberCoroutineScope()
    var themeMode by remember { mutableStateOf(ThemeMode.AUTO) }

    LaunchedEffect(Unit) {
        val saved = settingsStore.getThemeMode()
        themeMode = when (saved) {
            "dark" -> ThemeMode.DARK
            "light" -> ThemeMode.LIGHT
            else -> ThemeMode.AUTO
        }
    }

    VoiceTranslatorTheme(themeMode = themeMode) {
        var currentScreen by remember { mutableStateOf(Screen.Translator) }
        
        when (currentScreen) {
            Screen.Translator -> {
                TranslatorScreen(
                    onNavigateToSettings = {
                        currentScreen = Screen.Settings
                    }
                )
            }
            
            Screen.Settings -> {
                SettingsScreen(
                    onBackClick = {
                        currentScreen = Screen.Translator
                    },
                    currentThemeMode = themeMode,
                    onThemeModeChanged = { newMode ->
                        themeMode = newMode
                        scope.launch {
                            settingsStore.setThemeMode(
                                when (newMode) {
                                    ThemeMode.AUTO -> "auto"
                                    ThemeMode.DARK -> "dark"
                                    ThemeMode.LIGHT -> "light"
                                }
                            )
                        }
                    }
                )
            }
        }
    }
}
