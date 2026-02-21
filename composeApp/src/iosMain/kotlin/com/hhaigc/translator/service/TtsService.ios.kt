package com.hhaigc.translator.service

import platform.AVFAudio.AVSpeechSynthesizer
import platform.AVFAudio.AVSpeechUtterance
import platform.AVFAudio.AVSpeechSynthesisVoice
import platform.AVFAudio.AVSpeechBoundaryImmediate

actual class TtsService {
    private val synthesizer = AVSpeechSynthesizer()

    actual fun speak(text: String, languageCode: String) {
        val bcp47 = languageCodeToBcp47(languageCode)
        val utterance = AVSpeechUtterance(string = text)
        utterance.voice = AVSpeechSynthesisVoice.voiceWithLanguage(bcp47)
        utterance.rate = 0.5f
        if (synthesizer.isSpeaking()) {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate) // AVSpeechBoundaryImmediate = 0
        }
        synthesizer.speakUtterance(utterance)
    }

    actual fun stop() {
        if (synthesizer.isSpeaking()) {
            synthesizer.stopSpeakingAtBoundary(AVSpeechBoundaryImmediate) // AVSpeechBoundaryImmediate = 0
        }
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
