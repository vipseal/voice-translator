package com.hhaigc.translator.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val code: String,
    val name: String,
    val flag: String,
    val isEnabled: Boolean = true
) {
    companion object {
        val ALL_LANGUAGES = listOf(
            Language("en", "English", "EN", isEnabled = true),
            Language("zh", "中文", "ZH", isEnabled = true),
            Language("ja", "日本語", "JA", isEnabled = true),
            Language("ko", "한국어", "KO", isEnabled = false),
            Language("ar", "العربية", "AR", isEnabled = false),
            Language("fr", "Français", "FR", isEnabled = false),
            Language("de", "Deutsch", "DE", isEnabled = false),
            Language("es", "Español", "ES", isEnabled = false),
            Language("ru", "Русский", "RU", isEnabled = false),
            Language("th", "ไทย", "TH", isEnabled = true),
            Language("vi", "Tiếng Việt", "VI", isEnabled = false),
            Language("pt", "Português", "PT", isEnabled = false),
            Language("hi", "हिन्दी", "HI", isEnabled = false),
            Language("id", "Bahasa Indonesia", "ID", isEnabled = false),
            Language("tr", "Türkçe", "TR", isEnabled = false)
        )
    }
}

@Serializable
data class TranslationResult(
    val originalText: String,
    val translations: Map<String, String> // language code to translated text
)

@Serializable
data class TranscriptionResult(
    val text: String,
    val lang: String = "Unknown",
    val langCode: String = "und"
)