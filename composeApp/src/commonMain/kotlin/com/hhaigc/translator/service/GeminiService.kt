package com.hhaigc.translator.service

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
    val contents: List<Content>
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
    
    @OptIn(ExperimentalEncodingApi::class)
    suspend fun transcribeAudio(audioBytes: ByteArray): Result<String> {
        return try {
            val base64Audio = Base64.encode(audioBytes)
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(
                                text = "Please transcribe this audio file and return only the transcribed text:"
                            ),
                            Part(
                                inline_data = InlineData(
                                    mime_type = "audio/wav",
                                    data = base64Audio
                                )
                            )
                        )
                    )
                )
            )
            
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            val text = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("No transcription received"))
            
            Result.success(text.trim())
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
                
                Only return the JSON object, no additional text.
            """.trimIndent()
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        parts = listOf(
                            Part(text = prompt)
                        )
                    )
                )
            )
            
            val response: GeminiResponse = client.post("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent") {
                parameter("key", apiKey)
                contentType(ContentType.Application.Json)
                setBody(request)
            }.body()
            
            val responseText = response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("No translation received"))
            
            // Parse JSON response
            val json = Json { ignoreUnknownKeys = true }
            val translations = json.decodeFromString<Map<String, String>>(responseText.trim())
            
            Result.success(translations)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    fun cleanup() {
        client.close()
    }
}