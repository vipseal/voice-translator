package com.hhaigc.translator

import androidx.compose.runtime.*
import com.hhaigc.translator.screen.ActivationScreen
import com.hhaigc.translator.screen.SettingsScreen
import com.hhaigc.translator.screen.TranslatorScreen
import com.hhaigc.translator.store.SettingsStore
import com.hhaigc.translator.theme.ThemeMode
import com.hhaigc.translator.theme.VoiceTranslatorTheme
import kotlinx.coroutines.launch

enum class Screen {
    Activation,
    Translator,
    Settings
}

@Composable
fun App() {
    val settingsStore = remember { SettingsStore() }
    val scope = rememberCoroutineScope()
    var themeMode by remember { mutableStateOf(ThemeMode.AUTO) }
    var currentScreen by remember { mutableStateOf<Screen?>(null) }

    LaunchedEffect(Unit) {
        val saved = settingsStore.getThemeMode()
        themeMode = when (saved) {
            "dark" -> ThemeMode.DARK
            "light" -> ThemeMode.LIGHT
            else -> ThemeMode.AUTO
        }
        currentScreen = if (settingsStore.isActivated()) {
            Screen.Translator
        } else {
            Screen.Activation
        }
    }

    VoiceTranslatorTheme(themeMode = themeMode) {
        when (currentScreen) {
            Screen.Activation -> {
                ActivationScreen(
                    onActivated = {
                        currentScreen = Screen.Translator
                    }
                )
            }

            Screen.Translator -> {
                PlatformBackHandler(enabled = false) {}
                TranslatorScreen(
                    onNavigateToSettings = {
                        currentScreen = Screen.Settings
                    }
                )
            }
            
            Screen.Settings -> {
                PlatformBackHandler(enabled = true) {
                    currentScreen = Screen.Translator
                }
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

            null -> {}
        }
    }
}

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
