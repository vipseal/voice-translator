package com.hhaigc.translator.service

actual class SoundService {
    actual fun playStartRecording() {
        // No-op on desktop
    }

    actual fun playStopRecording() {
        // No-op on desktop
    }
}
