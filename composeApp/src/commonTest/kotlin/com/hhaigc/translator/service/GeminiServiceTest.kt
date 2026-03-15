package com.hhaigc.translator.service

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class GeminiServiceTest {

    private val json = Json { ignoreUnknownKeys = true }

    // ---- cleanJsonResponse ----

    @Test
    fun cleanJsonResponsePlainJson() {
        val service = GeminiService("test-key")
        val input = """{"text": "hello"}"""
        assertEquals("""{"text": "hello"}""", service.cleanJsonResponse(input))
    }

    @Test
    fun cleanJsonResponseWithMarkdownCodeBlock() {
        val service = GeminiService("test-key")
        val input = "```json\n{\"text\": \"hello\"}\n```"
        assertEquals("{\"text\": \"hello\"}", service.cleanJsonResponse(input))
    }

    @Test
    fun cleanJsonResponseWithGenericCodeBlock() {
        val service = GeminiService("test-key")
        val input = "```\n{\"text\": \"hello\"}\n```"
        assertEquals("{\"text\": \"hello\"}", service.cleanJsonResponse(input))
    }

    @Test
    fun cleanJsonResponseWithWhitespace() {
        val service = GeminiService("test-key")
        val input = "  \n  {\"text\": \"hello\"}  \n  "
        assertEquals("{\"text\": \"hello\"}", service.cleanJsonResponse(input))
    }

    @Test
    fun cleanJsonResponseEmptyString() {
        val service = GeminiService("test-key")
        assertEquals("", service.cleanJsonResponse(""))
    }

    // ---- GeminiRequest/Response serialization ----

    @Test
    fun geminiRequestSerializationRoundTrip() {
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(text = "Hello")
                    )
                )
            ),
            generationConfig = GenerationConfig(temperature = 0.5f)
        )
        val serialized = json.encodeToString(GeminiRequest.serializer(), request)
        val deserialized = json.decodeFromString(GeminiRequest.serializer(), serialized)
        assertEquals(request.contents.size, deserialized.contents.size)
        assertEquals("Hello", deserialized.contents[0].parts[0].text)
        assertEquals(0.5f, deserialized.generationConfig?.temperature)
    }

    @Test
    fun geminiResponseDeserialization() {
        val responseJson = """
            {
                "candidates": [{
                    "content": {
                        "parts": [{"text": "Hello response"}]
                    }
                }]
            }
        """.trimIndent()
        val response = json.decodeFromString(GeminiResponse.serializer(), responseJson)
        assertEquals(1, response.candidates.size)
        assertEquals("Hello response", response.candidates[0].content.parts[0].text)
    }

    @Test
    fun geminiRequestWithInlineData() {
        val request = GeminiRequest(
            contents = listOf(
                Content(
                    parts = listOf(
                        Part(inline_data = InlineData(mime_type = "audio/wav", data = "base64data")),
                        Part(text = "Transcribe this")
                    )
                )
            )
        )
        val serialized = json.encodeToString(GeminiRequest.serializer(), request)
        val deserialized = json.decodeFromString(GeminiRequest.serializer(), serialized)
        assertEquals("audio/wav", deserialized.contents[0].parts[0].inline_data?.mime_type)
        assertEquals("base64data", deserialized.contents[0].parts[0].inline_data?.data)
        assertEquals("Transcribe this", deserialized.contents[0].parts[1].text)
    }

    @Test
    fun generationConfigSerializationRoundTrip() {
        val config = GenerationConfig(temperature = 0.1f)
        val serialized = json.encodeToString(GenerationConfig.serializer(), config)
        val deserialized = json.decodeFromString(GenerationConfig.serializer(), serialized)
        assertEquals(0.1f, deserialized.temperature)
    }

    @Test
    fun generationConfigNullTemperature() {
        val config = GenerationConfig(temperature = null)
        val serialized = json.encodeToString(GenerationConfig.serializer(), config)
        val deserialized = json.decodeFromString(GenerationConfig.serializer(), serialized)
        assertEquals(null, deserialized.temperature)
    }

    // ---- Response parsing logic (simulating what GeminiService methods do) ----

    @Test
    fun transcriptionResultParsedFromGeminiLikeResponse() {
        val responseText = """{"text": "Hello world", "lang": "English", "langCode": "en"}"""
        val result = json.decodeFromString(
            com.hhaigc.translator.model.TranscriptionResult.serializer(),
            responseText
        )
        assertEquals("Hello world", result.text)
        assertEquals("English", result.lang)
        assertEquals("en", result.langCode)
    }

    @Test
    fun translationResponseParsedFromGeminiLikeResponse() {
        val responseText = """{"en": "Hello", "zh": "你好", "ja": "こんにちは"}"""
        val translations: Map<String, String> = json.decodeFromString(responseText)
        assertEquals("Hello", translations["en"])
        assertEquals("你好", translations["zh"])
        assertEquals("こんにちは", translations["ja"])
    }

    @Test
    fun detectAndTranslateResponseParsing() {
        val responseText = """{
            "detectedLang": "English",
            "detectedCode": "en",
            "translations": {"zh": "你好", "ja": "こんにちは"}
        }"""
        val jsonObj = json.parseToJsonElement(responseText).jsonObject
        val detectedLang = jsonObj["detectedLang"]?.jsonPrimitive?.contentOrNull
        val detectedCode = jsonObj["detectedCode"]?.jsonPrimitive?.contentOrNull
        val translationsObj = jsonObj["translations"]?.jsonObject

        assertEquals("English", detectedLang)
        assertEquals("en", detectedCode)
        assertNotNull(translationsObj)
        assertEquals("你好", translationsObj["zh"]?.jsonPrimitive?.content)
        assertEquals("こんにちは", translationsObj["ja"]?.jsonPrimitive?.content)
    }

    @Test
    fun detectAndTranslateResponseWithCodeBlockWrapping() {
        val service = GeminiService("test-key")
        val rawResponse = "```json\n{\"detectedLang\": \"Chinese\", \"detectedCode\": \"zh\", \"translations\": {\"en\": \"Hello\"}}\n```"
        val cleaned = service.cleanJsonResponse(rawResponse)
        val jsonObj = json.parseToJsonElement(cleaned).jsonObject
        assertEquals("Chinese", jsonObj["detectedLang"]?.jsonPrimitive?.contentOrNull)
    }

    @Test
    fun transcribeAndTranslateResponseParsing() {
        val responseText = """{
            "text": "你好世界",
            "detectedLang": "Chinese",
            "detectedCode": "zh",
            "translations": {"en": "Hello world", "ja": "こんにちは世界"}
        }"""
        val jsonObj = json.parseToJsonElement(responseText).jsonObject
        val text = jsonObj["text"]?.jsonPrimitive?.contentOrNull
        val detectedLang = jsonObj["detectedLang"]?.jsonPrimitive?.contentOrNull
        val translations = jsonObj["translations"]?.jsonObject?.mapValues { it.value.jsonPrimitive.content }

        assertEquals("你好世界", text)
        assertEquals("Chinese", detectedLang)
        assertNotNull(translations)
        assertEquals("Hello world", translations["en"])
    }

    @Test
    fun geminiResponseMultipleCandidates() {
        val responseJson = """
            {
                "candidates": [
                    {"content": {"parts": [{"text": "First"}]}},
                    {"content": {"parts": [{"text": "Second"}]}}
                ]
            }
        """.trimIndent()
        val response = json.decodeFromString(GeminiResponse.serializer(), responseJson)
        assertEquals(2, response.candidates.size)
        assertEquals("First", response.candidates[0].content.parts[0].text)
        assertEquals("Second", response.candidates[1].content.parts[0].text)
    }

    @Test
    fun contentWithMultipleParts() {
        val content = Content(
            parts = listOf(
                Part(text = "part1"),
                Part(text = "part2"),
                Part(inline_data = InlineData("image/png", "abc"))
            )
        )
        val serialized = json.encodeToString(Content.serializer(), content)
        val deserialized = json.decodeFromString(Content.serializer(), serialized)
        assertEquals(3, deserialized.parts.size)
        assertEquals("part1", deserialized.parts[0].text)
        assertEquals("image/png", deserialized.parts[2].inline_data?.mime_type)
    }
}
