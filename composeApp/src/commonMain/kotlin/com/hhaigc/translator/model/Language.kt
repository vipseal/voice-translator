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
            Language("en", "English", "🇺🇸", isEnabled = true),
            Language("zh", "中文", "🇨🇳", isEnabled = true),
            Language("ja", "日本語", "🇯🇵", isEnabled = true),
            Language("ko", "한국어", "🇰🇷", isEnabled = false),
            Language("ar", "العربية", "🇸🇦", isEnabled = false),
            Language("fr", "Français", "🇫🇷", isEnabled = false),
            Language("de", "Deutsch", "🇩🇪", isEnabled = false),
            Language("es", "Español", "🇪🇸", isEnabled = false),
            Language("ru", "Русский", "🇷🇺", isEnabled = false),
            Language("th", "ไทย", "🇹🇭", isEnabled = true),
            Language("vi", "Tiếng Việt", "🇻🇳", isEnabled = false),
            Language("pt", "Português", "🇧🇷", isEnabled = false),
            Language("hi", "हिन्दी", "🇮🇳", isEnabled = false),
            Language("id", "Bahasa Indonesia", "🇮🇩", isEnabled = false),
            Language("tr", "Türkçe", "🇹🇷", isEnabled = false)
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