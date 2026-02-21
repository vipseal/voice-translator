package com.hhaigc.translator.service

import javax.sound.sampled.*

actual class SoundService {
    private fun playTone(freqHz: Int, durationMs: Int, volume: Double = 0.3) {
        try {
            val sampleRate = 8000f
            val samples = (sampleRate * durationMs / 1000).toInt()
            val buf = ByteArray(samples)
            for (i in buf.indices) {
                val angle = 2.0 * Math.PI * i * freqHz / sampleRate
                buf[i] = (Math.sin(angle) * 127.0 * volume).toInt().toByte()
            }
            val format = AudioFormat(sampleRate, 8, 1, true, false)
            val info = DataLine.Info(SourceDataLine::class.java, format)
            val line = AudioSystem.getLine(info) as SourceDataLine
            line.open(format)
            line.start()
            line.write(buf, 0, buf.size)
            line.drain()
            line.close()
        } catch (_: Exception) {}
    }

    actual fun playStartRecording() {
        Thread { playTone(880, 150) }.start()
    }

    actual fun playStopRecording() {
        Thread { playTone(440, 150) }.start()
    }

    actual fun playClick() {
        Thread { playTone(1200, 30, 0.15) }.start()
    }
}
