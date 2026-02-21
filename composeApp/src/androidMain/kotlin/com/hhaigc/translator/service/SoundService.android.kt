package com.hhaigc.translator.service

import android.media.MediaActionSound

actual class SoundService {
    private val sound = MediaActionSound()

    actual fun playStartRecording() {
        try {
            sound.play(MediaActionSound.START_VIDEO_RECORDING)
        } catch (_: Exception) {}
    }

    actual fun playStopRecording() {
        try {
            sound.play(MediaActionSound.STOP_VIDEO_RECORDING)
        } catch (_: Exception) {}
    }
}
