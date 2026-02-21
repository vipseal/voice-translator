package com.hhaigc.translator.service

expect class TtsService() {
    fun speak(text: String, languageCode: String)
    fun stop()
    fun shutdown()
}
