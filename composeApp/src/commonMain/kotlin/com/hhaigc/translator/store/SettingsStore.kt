package com.hhaigc.translator.store

import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.Flow

expect class SettingsStore {
    companion object {
        fun getInstance(): SettingsStore
    }
    
    fun getEnabledLanguages(): Flow<List<Language>>
    suspend fun setLanguageEnabled(languageCode: String, enabled: Boolean)
    suspend fun getSourceLanguage(): String
    suspend fun setSourceLanguage(languageCode: String)
    suspend fun getThemeMode(): String
    suspend fun setThemeMode(mode: String)
    suspend fun isActivated(): Boolean
    suspend fun setActivated(activated: Boolean)
    suspend fun getApiKey(): String
    suspend fun setApiKey(key: String)
}
