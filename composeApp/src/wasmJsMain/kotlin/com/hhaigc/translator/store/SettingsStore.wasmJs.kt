package com.hhaigc.translator.store

import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@JsFun("function(key) { return localStorage.getItem(key); }")
private external fun jsStorageGet(key: String): String?

@JsFun("function(key, value) { localStorage.setItem(key, value); }")
private external fun jsStorageSet(key: String, value: String)

actual class SettingsStore private constructor() {
    actual companion object {
        private val _instance by lazy { SettingsStore() }
        actual fun getInstance(): SettingsStore = _instance
    }
    
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
    
    actual suspend fun getSourceLanguage(): String = jsStorageGet("source_language") ?: "en"
    actual suspend fun setSourceLanguage(languageCode: String) { jsStorageSet("source_language", languageCode) }
    actual suspend fun getThemeMode(): String = jsStorageGet("theme_mode") ?: "auto"
    actual suspend fun setThemeMode(mode: String) { jsStorageSet("theme_mode", mode) }
    actual suspend fun isActivated(): Boolean = jsStorageGet("activated") == "true"
    actual suspend fun setActivated(activated: Boolean) { jsStorageSet("activated", activated.toString()) }
    actual suspend fun getApiKey(): String = jsStorageGet("api_key") ?: ""
    actual suspend fun setApiKey(key: String) { jsStorageSet("api_key", key) }

    private fun loadEnabledLanguages(): List<Language> {
        val saved = jsStorageGet("enabled_languages")
        return if (saved != null) {
            try { Json.decodeFromString<List<Language>>(saved) } catch (_: Exception) { Language.ALL_LANGUAGES }
        } else {
            Language.ALL_LANGUAGES
        }
    }

    private fun saveEnabledLanguages(languages: List<Language>) {
        jsStorageSet("enabled_languages", Json.encodeToString(languages))
    }
}
