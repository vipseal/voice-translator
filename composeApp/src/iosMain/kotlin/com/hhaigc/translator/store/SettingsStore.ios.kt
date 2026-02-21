package com.hhaigc.translator.store

import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class SettingsStore {
    private val defaults = NSUserDefaults.standardUserDefaults
    private val _enabledLanguages = MutableStateFlow(loadEnabledLanguages())
    
    actual fun getEnabledLanguages(): Flow<List<Language>> = _enabledLanguages
    
    actual suspend fun setLanguageEnabled(languageCode: String, enabled: Boolean) {
        val currentLanguages = _enabledLanguages.value.toMutableList()
        val index = currentLanguages.indexOfFirst { it.code == languageCode }
        
        if (index != -1) {
            currentLanguages[index] = currentLanguages[index].copy(isEnabled = enabled)
            _enabledLanguages.value = currentLanguages
            saveEnabledLanguages(currentLanguages)
        }
    }
    
    actual suspend fun getSourceLanguage(): String {
        return defaults.stringForKey("source_language") ?: "en"
    }
    
    actual suspend fun setSourceLanguage(languageCode: String) {
        defaults.setObject(languageCode, "source_language")
        defaults.synchronize()
    }
    
    private fun loadEnabledLanguages(): List<Language> {
        val savedData = defaults.stringForKey("enabled_languages")
        return if (savedData != null) {
            try {
                Json.decodeFromString<List<Language>>(savedData)
            } catch (e: Exception) {
                Language.ALL_LANGUAGES
            }
        } else {
            Language.ALL_LANGUAGES
        }
    }
    
    actual suspend fun getThemeMode(): String {
        return defaults.stringForKey("theme_mode") ?: "auto"
    }

    actual suspend fun setThemeMode(mode: String) {
        defaults.setObject(mode, "theme_mode")
        defaults.synchronize()
    }

    private fun saveEnabledLanguages(languages: List<Language>) {
        val jsonString = Json.encodeToString(languages)
        defaults.setObject(jsonString, "enabled_languages")
        defaults.synchronize()
    }
}