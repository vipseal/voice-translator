package com.hhaigc.translator.service

actual class TtsService {
    actual fun speak(text: String, languageCode: String) {
        // Web Speech API stub - would need JS interop
    }

    actual fun stop() {}
    actual fun shutdown() {}
}
