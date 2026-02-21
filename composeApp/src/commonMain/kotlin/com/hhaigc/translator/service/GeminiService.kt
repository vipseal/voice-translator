package com.hhaigc.translator.service

import com.hhaigc.translator.model.TranscriptionResult
import com.hhaigc.translator.model.TranslationResult
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

@Serializable
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = null
)

@Serializable
data class Content(
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val inline_data: InlineData? = null
)

@Serializable
data class InlineData(
    val mime_type: String,
    val data: String
)

@Serializable
data class GeminiResponse(
    val candidates: List<Candidate>
)

@Serializable
data class Candidate(
    val content: ResponseContent
)

@Serializable
data class ResponseContent(
    val parts: List<ResponsePart>
)

@Serializable
data class ResponsePart(
    val text: String
)

class GeminiService {
    private val apiKey = "GEMINI_API_KEY_PLACEHOLDER"
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                coerceInputValues = true
            })
        }
    }
    
    private val json = Json { ignoreUnknownKeys = true }
    
    private fun cleanJsonResponse(text: String): String {
        return text.trim()
            .removePrefix("```json").removePrefix("```")
            .removeSuffix("```").trim()
    }
    
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun transcribeAudio(audioBytes: ByteArray): Result<TranscriptionResult> {
        return try {
            val base64Audio = Base64.encode(audioBytes)
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                inline_data = InlineData(
                                    mime_type = "audio/wav",
                                    data = base64Audio
                                )
                            ),
                            Part(
                                text = "Transcribe this audio. Respond in JSON format: {\"text\": \"transcribed text here\", \"lang\": \"detected language name\", \"langCode\": \"ISO 639-1 code\"}. Only output the JSON, nothing else."
                            )
                        )
                    )
                ),
                generationConfig = GenerationConfig(temperature = 0f)
            )
            
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("No transcription received"))
            
            try {
                val cleaned = cleanJsonResponse(text)
                val result = json.decodeFromString<TranscriptionResult>(cleaned)
                Result.success(result)
            } catch (e: Exception) {
                // Fallback: treat entire response as plain text
                Result.success(TranscriptionResult(text = text.trim()))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    suspend fun translateText(text: String, targetLanguages: List<String>): Result<Map<String, String>> {
        return try {
            val languageList = targetLanguages.joinToString(", ")
            val prompt = """
                Translate the following text into these languages: $languageList
                
                Text to translate: "$text"
                
                Return the result as a JSON object where each key is the language code and each value is the translation.
                Example: {"en": "Hello", "fr": "Bonjour", "es": "Hola"}
                If the source text is already in a target language, keep it as-is for that language.
                Only return the JSON object, no additional text.
            """.trimIndent()
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt)
                        )
                    )
                ),
                generationConfig = GenerationConfig(temperature = 0.1f)
            )
            
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("No translation received"))
            
            val cleanText = cleanJsonResponse(responseText)
            val translations = json.decodeFromString<Map<String, String>>(cleanText)
            
            Result.success(translations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Detect language and translate clipboard text in one call.
     * Returns a pair of (TranscriptionResult with detected language, translations map).
     */
    suspend fun detectAndTranslate(text: String, targetLanguages: List<String>): Result<Pair<TranscriptionResult, Map<String, String>>> {
        return try {
            val langNames = targetLanguages.joinToString(", ")
            val prompt = """
                Detect the language and translate the following text to these languages: $langNames

                Text: "$text"

                Respond ONLY in JSON: {"detectedLang": "language name", "detectedCode": "ISO code", "translations": {"en": "...", "zh": "...", ...}}
                If source matches a target language, keep as-is. Only output JSON.
            """.trimIndent()
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(Part(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(temperature = 0.1f)
            )
            
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("No response received"))
            
            val cleaned = cleanJsonResponse(responseText)
            val jsonObj = json.parseToJsonElement(cleaned).jsonObject
            
            val detectedLang = jsonObj["detectedLang"]?.jsonPrimitive?.contentOrNull ?: "Unknown"
            val detectedCode = jsonObj["detectedCode"]?.jsonPrimitive?.contentOrNull ?: "und"
            val translationsObj = jsonObj["translations"]?.jsonObject ?: return Result.failure(Exception("No translations in response"))
            
            val translations = translationsObj.mapValues { it.value.jsonPrimitive.content }
            val transcription = TranscriptionResult(text = text, lang = detectedLang, langCode = detectedCode)
            
            Result.success(Pair(transcription, translations))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun cleanup() {
        client.close()
    }
}
