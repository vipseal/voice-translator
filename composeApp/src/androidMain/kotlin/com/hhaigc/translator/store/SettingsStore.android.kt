package com.hhaigc.translator.store

import android.content.Context
import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

actual class SettingsStore private constructor() {
    actual companion object {
        private var appContext: Context? = null
        private val _instance by lazy { SettingsStore() }
        
        actual fun getInstance(): SettingsStore = _instance
        
        fun initWithContext(context: Context) {
            appContext = context.applicationContext
        }
    }
    
    private val prefs get() = appContext?.getSharedPreferences("voice_translator_settings", Context.MODE_PRIVATE)
    
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
        return prefs?.getString("source_language", "en") ?: "en"
    }
    
    actual suspend fun setSourceLanguage(languageCode: String) {
        prefs?.edit()?.putString("source_language", languageCode)?.apply()
    }
    
    private fun loadEnabledLanguages(): List<Language> {
        val savedData = prefs?.getString("enabled_languages", null)
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
        return prefs?.getString("theme_mode", "auto") ?: "auto"
    }

    actual suspend fun setThemeMode(mode: String) {
        prefs?.edit()?.putString("theme_mode", mode)?.apply()
    }

    actual suspend fun isActivated(): Boolean {
        return prefs?.getBoolean("is_activated", false) ?: false
    }

    actual suspend fun setActivated(activated: Boolean) {
        prefs?.edit()?.putBoolean("is_activated", activated)?.apply()
    }

    actual suspend fun getApiKey(): String {
        return prefs?.getString("api_key", "") ?: ""
    }

    actual suspend fun setApiKey(key: String) {
        prefs?.edit()?.putString("api_key", key)?.apply()
    }

    private fun saveEnabledLanguages(languages: List<Language>) {
        val jsonString = Json.encodeToString(languages)
        prefs?.edit()?.putString("enabled_languages", jsonString)?.apply()
    }
}