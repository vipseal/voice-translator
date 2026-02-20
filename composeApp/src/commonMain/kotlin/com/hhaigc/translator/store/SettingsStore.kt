package com.hhaigc.translator.store

import com.hhaigc.translator.model.Language
import kotlinx.coroutines.flow.Flow

expect class SettingsStore() {
    fun getEnabledLanguages(): Flow<List<Language>>
    suspend fun setLanguageEnabled(languageCode: String, enabled: Boolean)
    suspend fun getSourceLanguage(): String
    suspend fun setSourceLanguage(languageCode: String)
}