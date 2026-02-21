package com.hhaigc.translator.service

actual class SoundService {
    actual fun playStartRecording() {
        // No-op on web
    }

    actual fun playStopRecording() {
        // No-op on web
    }

    actual fun playClick() {
        // no-op
    }
}
