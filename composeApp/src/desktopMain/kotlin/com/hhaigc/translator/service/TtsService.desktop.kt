package com.hhaigc.translator.service

actual class TtsService {
    actual fun speak(text: String, languageCode: String) {
        try {
            val os = System.getProperty("os.name").lowercase()
            val process = when {
                os.contains("mac") -> ProcessBuilder("say", text)
                os.contains("linux") -> ProcessBuilder("espeak", text)
                os.contains("win") -> ProcessBuilder(
                    "powershell", "-Command",
                    "Add-Type -AssemblyName System.Speech; " +
                    "(New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('${text.replace("'", "''")}')"
                )
                else -> return
            }
            process.start()
        } catch (_: Exception) {
            // TTS not available on this system
        }
    }

    actual fun stop() {}
    actual fun shutdown() {}
}
