package com.hhaigc.translator.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class LanguageTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun allLanguagesListIsNotEmpty() {
        assertTrue(Language.ALL_LANGUAGES.isNotEmpty(), "ALL_LANGUAGES should not be empty")
    }

    @Test
    fun allLanguagesContainsExpectedCount() {
        assertEquals(15, Language.ALL_LANGUAGES.size, "ALL_LANGUAGES should contain 15 languages")
    }

    @Test
    fun allLanguagesHaveUniqueCode() {
        val codes = Language.ALL_LANGUAGES.map { it.code }
        assertEquals(codes.size, codes.distinct().size, "All language codes should be unique")
    }

    @Test
    fun allLanguagesHaveNonEmptyFields() {
        Language.ALL_LANGUAGES.forEach { lang ->
            assertTrue(lang.code.isNotBlank(), "Language code should not be blank: $lang")
            assertTrue(lang.name.isNotBlank(), "Language name should not be blank: $lang")
            assertTrue(lang.flag.isNotBlank(), "Language flag should not be blank: $lang")
        }
    }

    @Test
    fun allLanguagesContainsMajorLanguages() {
        val codes = Language.ALL_LANGUAGES.map { it.code }.toSet()
        val expected = setOf("en", "zh", "ja", "ko", "fr", "de", "es", "ru", "th", "ar")
        expected.forEach { code ->
            assertTrue(code in codes, "ALL_LANGUAGES should contain $code")
        }
    }

    @Test
    fun allLanguagesDefaultEnabledAreCorrect() {
        val enabledByDefault = Language.ALL_LANGUAGES.filter { it.isEnabled }.map { it.code }.toSet()
        assertEquals(setOf("en", "zh", "ja", "th"), enabledByDefault, "Default enabled languages should be en, zh, ja, th")
    }

    // ---- Serialization round-trip ----

    @Test
    fun languageSerializationRoundTrip() {
        val lang = Language("en", "English", "🇺🇸", isEnabled = true)
        val serialized = json.encodeToString(Language.serializer(), lang)
        val deserialized = json.decodeFromString(Language.serializer(), serialized)
        assertEquals(lang, deserialized)
    }

    @Test
    fun languageSerializationWithDefaultEnabled() {
        val lang = Language("fr", "Français", "🇫🇷")
        val serialized = json.encodeToString(Language.serializer(), lang)
        val deserialized = json.decodeFromString(Language.serializer(), serialized)
        assertEquals(lang, deserialized)
        assertTrue(deserialized.isEnabled, "Default isEnabled should be true")
    }

    @Test
    fun languageListSerializationRoundTrip() {
        val languages = Language.ALL_LANGUAGES
        val serializer = kotlinx.serialization.builtins.ListSerializer(Language.serializer())
        val serialized = json.encodeToString(serializer, languages)
        val deserialized = json.decodeFromString(serializer, serialized)
        assertEquals(languages, deserialized)
    }
}
