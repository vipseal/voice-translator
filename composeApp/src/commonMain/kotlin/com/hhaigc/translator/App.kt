package com.hhaigc.translator

import androidx.compose.animation.*
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
    val settingsStore = remember { SettingsStore.getInstance() }
    val scope = rememberCoroutineScope()
    var themeMode by remember { mutableStateOf(ThemeMode.AUTO) }
    var currentScreen by remember { mutableStateOf<Screen?>(null) }
    var settingsVersion by remember { mutableStateOf(0) }

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

            Screen.Translator, Screen.Settings -> {
                androidx.compose.foundation.layout.Box {
                    // Always keep TranslatorScreen composed to preserve state
                    TranslatorScreen(
                        onNavigateToSettings = {
                            currentScreen = Screen.Settings
                        },
                        settingsVersion = settingsVersion
                    )
                    // Overlay Settings on top when active
                    AnimatedVisibility(
                        visible = currentScreen == Screen.Settings,
                        enter = slideInHorizontally(initialOffsetX = { it }),
                        exit = slideOutHorizontally(targetOffsetX = { it })
                    ) {
                        PlatformBackHandler(enabled = true) {
                            settingsVersion++
                            currentScreen = Screen.Translator
                        }
                        SettingsScreen(
                            onBackClick = {
                                settingsVersion++
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
                            },
                            onResetAuth = {
                                scope.launch {
                                    settingsStore.setApiKey("")
                                    settingsStore.setActivated(false)
                                    currentScreen = Screen.Activation
                                }
                            }
                        )
                    }
                }
            }

            null -> {}
        }
    }
}

@Composable
expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
