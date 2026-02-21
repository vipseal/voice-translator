package com.hhaigc.translator.service

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

actual class TtsService {
    companion object {
        private var appContext: Context? = null
        fun initWithContext(context: Context) {
            appContext = context.applicationContext
        }
    }

    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private fun ensureInit() {
        if (tts == null) {
            val ctx = appContext ?: return
            tts = TextToSpeech(ctx) { status ->
                isInitialized = status == TextToSpeech.SUCCESS
            }
        }
    }

    actual fun speak(text: String, languageCode: String) {
        ensureInit()
        val engine = tts ?: return
        val locale = languageCodeToLocale(languageCode)
        engine.language = locale
        engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "tts_${System.currentTimeMillis()}")
    }

    actual fun stop() {
        tts?.stop()
    }

    actual fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        isInitialized = false
    }

    private fun languageCodeToLocale(code: String): Locale = when (code) {
        "zh" -> Locale.CHINESE
        "ja" -> Locale.JAPANESE
        "ko" -> Locale.KOREAN
        "en" -> Locale.US
        "fr" -> Locale.FRENCH
        "de" -> Locale.GERMAN
        "es" -> Locale("es")
        "ru" -> Locale("ru")
        "ar" -> Locale("ar")
        "th" -> Locale("th")
        "vi" -> Locale("vi")
        "pt" -> Locale("pt", "BR")
        "hi" -> Locale("hi")
        "id" -> Locale("id")
        "tr" -> Locale("tr")
        else -> Locale(code)
    }
}
