package com.hhaigc.translator.service

@JsFun("function(text, lang) { window.speechSynthesis.cancel(); var u = new SpeechSynthesisUtterance(text); u.lang = lang; window.speechSynthesis.speak(u); }")
private external fun jsTtsSpeak(text: String, lang: String)

@JsFun("function() { window.speechSynthesis.cancel(); }")
private external fun jsTtsStop()

actual class TtsService {
    actual fun speak(text: String, languageCode: String) {
        try { jsTtsSpeak(text, languageCode) } catch (_: Exception) {}
    }
    actual fun stop() {
        try { jsTtsStop() } catch (_: Exception) {}
    }
    actual fun shutdown() { stop() }
}
