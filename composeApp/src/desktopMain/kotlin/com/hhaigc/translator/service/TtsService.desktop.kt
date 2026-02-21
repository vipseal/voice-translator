package com.hhaigc.translator.service

actual class TtsService {
    private var currentProcess: Process? = null

    private fun macVoice(lang: String): String = when (lang.lowercase().take(2)) {
        "zh" -> "Tingting"
        "ja" -> "Kyoko"
        "ko" -> "Yuna"
        "th" -> "Kanya"
        "ar" -> "Maged"
        "fr" -> "Thomas"
        "es" -> "Mónica"
        "de" -> "Anna"
        "pt" -> "Luciana"
        "ru" -> "Milena"
        "it" -> "Alice"
        "hi" -> "Lekha"
        else -> "Samantha"
    }

    actual fun speak(text: String, languageCode: String) {
        try {
            stop()
            val os = System.getProperty("os.name").lowercase()
            val process = when {
                os.contains("mac") -> ProcessBuilder("say", "-v", macVoice(languageCode), text)
                os.contains("linux") -> {
                    val lang = languageCode.lowercase().take(2)
                    ProcessBuilder("espeak", "-v", lang, text)
                }
                os.contains("win") -> {
                    val escaped = text.replace("'", "''")
                    ProcessBuilder(
                        "powershell", "-Command",
                        "Add-Type -AssemblyName System.Speech; " +
                        "\$s = New-Object System.Speech.Synthesis.SpeechSynthesizer; " +
                        "\$s.SelectVoiceByHints([System.Speech.Synthesis.VoiceGender]::NotSet, " +
                        "[System.Speech.Synthesis.VoiceAge]::NotSet, 0, " +
                        "(New-Object System.Globalization.CultureInfo('$languageCode'))); " +
                        "\$s.Speak('$escaped')"
                    )
                }
                else -> return
            }
            currentProcess = process.start()
        } catch (_: Exception) {}
    }

    actual fun stop() {
        try {
            currentProcess?.destroyForcibly()
            currentProcess = null
        } catch (_: Exception) {}
    }

    actual fun shutdown() { stop() }
}
