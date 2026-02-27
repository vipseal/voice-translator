package com.hhaigc.translator.store

import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.prefs.Preferences

actual class SettingsStore private constructor() {
    actual companion object {
        private val _instance by lazy { SettingsStore() }
        actual fun getInstance(): SettingsStore = _instance
    }
    
    private val prefs = Preferences.userRoot().node("com/hhaigc/translator")
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
        return prefs.get("source_language", "en")
    }
    
    actual suspend fun setSourceLanguage(languageCode: String) {
        prefs.put("source_language", languageCode)
        prefs.flush()
    }
    
    private fun loadEnabledLanguages(): List<Language> {
        val savedData = prefs.get("enabled_languages", null)
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
        return prefs.get("theme_mode", "auto")
    }

    actual suspend fun setThemeMode(mode: String) {
        prefs.put("theme_mode", mode)
        prefs.flush()
    }

    actual suspend fun isActivated(): Boolean {
        return prefs.getBoolean("is_activated", false)
    }

    actual suspend fun setActivated(activated: Boolean) {
        prefs.putBoolean("is_activated", activated)
        prefs.flush()
    }

    actual suspend fun getApiKey(): String {
        return prefs.get("api_key", "")
    }

    actual suspend fun setApiKey(key: String) {
        prefs.put("api_key", key)
        prefs.flush()
    }

    private fun saveEnabledLanguages(languages: List<Language>) {
        val jsonString = Json.encodeToString(languages)
        prefs.put("enabled_languages", jsonString)
        prefs.flush()
    }
}