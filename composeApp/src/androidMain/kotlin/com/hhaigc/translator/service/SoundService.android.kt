package com.hhaigc.translator.service

import android.media.AudioManager
import android.media.MediaActionSound
import android.media.ToneGenerator

actual class SoundService {
    private val sound = MediaActionSound()
    private val toneGenerator by lazy {
        try { ToneGenerator(AudioManager.STREAM_SYSTEM, 50) } catch (_: Exception) { null }
    }

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

    actual fun playClick() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 30)
        } catch (_: Exception) {}
    }
}
