package com.hhaigc.translator.store

import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private fun storageGet(key: String): String? {
    val result = js("localStorage.getItem(key)")
    return result?.toString()
}

private fun storageSet(key: String, value: String) {
    js("localStorage.setItem(key, value)")
}

actual class SettingsStore {
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
        return storageGet("source_language") ?: "en"
    }
    
    actual suspend fun setSourceLanguage(languageCode: String) {
        storageSet("source_language", languageCode)
    }
    
    actual suspend fun getThemeMode(): String {
        return storageGet("theme_mode") ?: "auto"
    }

    actual suspend fun setThemeMode(mode: String) {
        storageSet("theme_mode", mode)
    }

    actual suspend fun isActivated(): Boolean {
        return storageGet("activated") == "true"
    }

    actual suspend fun setActivated(activated: Boolean) {
        storageSet("activated", activated.toString())
    }

    actual suspend fun getApiKey(): String {
        return storageGet("api_key") ?: ""
    }

    actual suspend fun setApiKey(key: String) {
        storageSet("api_key", key)
    }

    private fun loadEnabledLanguages(): List<Language> {
        val saved = storageGet("enabled_languages")
        return if (saved != null) {
            try { Json.decodeFromString<List<Language>>(saved) } catch (_: Exception) { Language.ALL_LANGUAGES }
        } else {
            Language.ALL_LANGUAGES
        }
    }

    private fun saveEnabledLanguages(languages: List<Language>) {
        storageSet("enabled_languages", Json.encodeToString(languages))
    }
}
