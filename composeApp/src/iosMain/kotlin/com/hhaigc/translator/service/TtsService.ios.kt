package com.hhaigc.translator.service

import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechSynthesisVoice

actual class TtsService {
    private val synthesizer = AVSpeechSynthesizer()

    actual fun speak(text: String, languageCode: String) {
        val bcp47 = languageCodeToBcp47(languageCode)
        val utterance = AVSpeechUtterance(string = text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(bcp47)
        utterance.rate = 0.5f
        synthesizer.stopSpeakingAtBoundary(0) // 0 = immediate
        synthesizer.speakUtterance(utterance)
    }

    actual fun stop() {
        synthesizer.stopSpeakingAtBoundary(0) // 0 = immediate
    }

    actual fun shutdown() {
        stop()
    }

    private fun languageCodeToBcp47(code: String): String = when (code) {
        "zh" -> "zh-CN"
        "ja" -> "ja-JP"
        "ko" -> "ko-KR"
        "en" -> "en-US"
        "fr" -> "fr-FR"
        "de" -> "de-DE"
        "es" -> "es-ES"
        "ru" -> "ru-RU"
        "ar" -> "ar-SA"
        "th" -> "th-TH"
        "vi" -> "vi-VN"
        "pt" -> "pt-BR"
        "hi" -> "hi-IN"
        "id" -> "id-ID"
        "tr" -> "tr-TR"
        else -> code
    }
}
