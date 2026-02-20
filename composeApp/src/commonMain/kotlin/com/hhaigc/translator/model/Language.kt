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
            Language("en", "English", "🇺🇸"),
            Language("zh", "Chinese", "🇨🇳"),
            Language("ja", "Japanese", "🇯🇵"),
            Language("ko", "Korean", "🇰🇷"),
            Language("ar", "Arabic", "🇸🇦"),
            Language("fr", "French", "🇫🇷"),
            Language("de", "German", "🇩🇪"),
            Language("es", "Spanish", "🇪🇸"),
            Language("ru", "Russian", "🇷🇺"),
            Language("th", "Thai", "🇹🇭"),
            Language("vi", "Vietnamese", "🇻🇳"),
            Language("pt", "Portuguese", "🇧🇷"),
            Language("hi", "Hindi", "🇮🇳"),
            Language("id", "Indonesian", "🇮🇩"),
            Language("tr", "Turkish", "🇹🇷")
        )
    }
}

@Serializable
data class TranslationResult(
    val originalText: String,
    val translations: Map<String, String> // language code to translated text
)