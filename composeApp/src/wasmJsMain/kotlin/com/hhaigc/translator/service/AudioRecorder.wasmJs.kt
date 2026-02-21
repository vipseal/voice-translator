package com.hhaigc.translator.service

actual class AudioRecorder {
    private var isCurrentlyRecording = false

    actual fun hasPermission(): Boolean = true

    actual suspend fun startRecording(): Boolean {
        // Audio recording in WASM requires complex JS interop
        // For now, users can use paste/type input instead
        return false
    }

    actual suspend fun stopRecording(): ByteArray? {
        isCurrentlyRecording = false
        return null
    }

    actual fun requestPermission(callback: (Boolean) -> Unit) {
        callback(true)
    }

    actual fun isRecording(): Boolean = isCurrentlyRecording
}
