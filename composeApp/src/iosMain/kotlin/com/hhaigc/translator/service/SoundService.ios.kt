package com.hhaigc.translator.service

import platform.AudioToolbox.AudioServicesPlaySystemSound
import platform.AudioToolbox.kSystemSoundID_Vibrate

actual class SoundService {
    actual fun playStartRecording() {
        // System sound 1113 = begin recording beep
        AudioServicesPlaySystemSound(1113u)
    }

    actual fun playStopRecording() {
        // System sound 1114 = end recording beep
        AudioServicesPlaySystemSound(1114u)
    }
}
