package com.hhaigc.translator.service

expect class AudioRecorder() {
    suspend fun startRecording(): Boolean
    suspend fun stopRecording(): ByteArray?
    fun isRecording(): Boolean
    fun hasPermission(): Boolean
    fun requestPermission(callback: (Boolean) -> Unit)
}
