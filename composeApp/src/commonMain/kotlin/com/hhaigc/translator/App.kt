package com.hhaigc.translator

import androidx.compose.runtime.*
import com.hhaigc.translator.screen.SettingsScreen
import com.hhaigc.translator.screen.TranslatorScreen
import com.hhaigc.translator.theme.VoiceTranslatorTheme

enum class Screen {
    Translator,
    Settings
}

@Composable
fun App() {
    VoiceTranslatorTheme {
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
                    }
                )
            }
        }
    }
}