package com.hhaigc.translator.model

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class ResultModelsTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- TranslationResult ----

    @Test
    fun translationResultSerializationRoundTrip() {
        val result = TranslationResult(
            originalText = "Hello",
            translations = mapOf("zh" to "你好", "ja" to "こんにちは", "th" to "สวัสดี")
        )
        val serialized = json.encodeToString(TranslationResult.serializer(), result)
        val deserialized = json.decodeFromString(TranslationResult.serializer(), serialized)
        assertEquals(result, deserialized)
    }

    @Test
    fun translationResultWithEmptyTranslations() {
        val result = TranslationResult(
            originalText = "Test",
            translations = emptyMap()
        )
        val serialized = json.encodeToString(TranslationResult.serializer(), result)
        val deserialized = json.decodeFromString(TranslationResult.serializer(), serialized)
        assertEquals(result, deserialized)
    }

    // ---- TranscriptionResult ----

    @Test
    fun transcriptionResultSerializationRoundTrip() {
        val result = TranscriptionResult(
            text = "Hello world",
            lang = "English",
            langCode = "en"
        )
        val serialized = json.encodeToString(TranscriptionResult.serializer(), result)
        val deserialized = json.decodeFromString(TranscriptionResult.serializer(), serialized)
        assertEquals(result, deserialized)
    }

    @Test
    fun transcriptionResultDefaultValues() {
        val result = TranscriptionResult(text = "test")
        assertEquals("Unknown", result.lang)
        assertEquals("und", result.langCode)
    }

    @Test
    fun transcriptionResultSerializationWithDefaults() {
        val result = TranscriptionResult(text = "test")
        val serialized = json.encodeToString(TranscriptionResult.serializer(), result)
        val deserialized = json.decodeFromString(TranscriptionResult.serializer(), serialized)
        assertEquals(result, deserialized)
    }

    @Test
    fun transcriptionResultFromJsonWithMissingOptionalFields() {
        val jsonStr = """{"text": "hello"}"""
        val result = json.decodeFromString(TranscriptionResult.serializer(), jsonStr)
        assertEquals("hello", result.text)
        assertEquals("Unknown", result.lang)
        assertEquals("und", result.langCode)
    }
}
