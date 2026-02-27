package com.hhaigc.translator.store

import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import platform.Foundation.NSUserDefaults

actual class SettingsStore private constructor() {
    actual companion object {
        private val _instance by lazy { SettingsStore() }
        actual fun getInstance(): SettingsStore = _instance
    }
    
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

    actual suspend fun isActivated(): Boolean {
        return defaults.boolForKey("is_activated")
    }

    actual suspend fun setActivated(activated: Boolean) {
        defaults.setBool(activated, "is_activated")
        defaults.synchronize()
    }

    actual suspend fun getApiKey(): String {
        return defaults.stringForKey("api_key") ?: ""
    }

    actual suspend fun setApiKey(key: String) {
        defaults.setObject(key, "api_key")
        defaults.synchronize()
    }

    private fun saveEnabledLanguages(languages: List<Language>) {
        val jsonString = Json.encodeToString(languages)
        defaults.setObject(jsonString, "enabled_languages")
        defaults.synchronize()
    }
}