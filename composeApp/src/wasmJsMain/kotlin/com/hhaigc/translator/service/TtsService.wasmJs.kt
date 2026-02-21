package com.hhaigc.translator.service

actual class TtsService {
    actual fun speak(text: String, languageCode: String) {
        js("window.speechSynthesis.cancel(); var u = new SpeechSynthesisUtterance(text); u.lang = languageCode; window.speechSynthesis.speak(u)")
    }

    actual fun stop() {
        js("window.speechSynthesis.cancel()")
    }

    actual fun shutdown() {
        stop()
    }
}
