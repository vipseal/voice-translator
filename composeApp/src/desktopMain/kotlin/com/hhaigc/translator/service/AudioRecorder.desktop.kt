package com.hhaigc.translator.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.sound.sampled.*

actual class AudioRecorder {
    private var targetDataLine: TargetDataLine? = null
    private var isCurrentlyRecording = false
    private var audioData = ByteArrayOutputStream()
    private var recordingThread: Thread? = null
    
    actual fun hasPermission(): Boolean = true // Desktop typically doesn't need explicit permission
    
    actual suspend fun startRecording(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isCurrentlyRecording) {
                return@withContext false
            }
            
            val audioFormat = AudioFormat(
                16000.0f, // Sample rate (16kHz is enough for speech, better compatibility)
                16,       // Sample size in bits
                1,        // Channels (mono)
                true,     // Signed
                false     // Big endian (little endian for WAV)
            )
            
            val dataLineInfo = DataLine.Info(TargetDataLine::class.java, audioFormat)
            
            if (!AudioSystem.isLineSupported(dataLineInfo)) {
                // Try fallback with default mixer
                println("AudioRecorder: Default format not supported, trying mixers...")
                val mixers = AudioSystem.getMixerInfo()
                for (mixerInfo in mixers) {
                    println("AudioRecorder: Mixer: ${mixerInfo.name} - ${mixerInfo.description}")
                }
                return@withContext false
            }
            
            targetDataLine = AudioSystem.getLine(dataLineInfo) as TargetDataLine
            targetDataLine?.open(audioFormat)
            targetDataLine?.start()
            println("AudioRecorder: Recording started with ${audioFormat.sampleRate}Hz")
            
            audioData = ByteArrayOutputStream()
            isCurrentlyRecording = true
            
            // Start recording in a separate thread
            recordingThread = Thread {
                val buffer = ByteArray(4096)
                while (isCurrentlyRecording && targetDataLine?.isOpen == true) {
                    val bytesRead = targetDataLine?.read(buffer, 0, buffer.size) ?: 0
                    if (bytesRead > 0) {
                        audioData.write(buffer, 0, bytesRead)
                    }
                }
            }
            recordingThread?.start()
            
            true
        } catch (e: Exception) {
            isCurrentlyRecording = false
            false
        }
    }
    
    actual suspend fun stopRecording(): ByteArray? = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentlyRecording || targetDataLine == null) {
                return@withContext null
            }
            
            isCurrentlyRecording = false
            
            // Wait for recording thread to finish
            recordingThread?.join(1000)
            
            val line = targetDataLine
            line?.stop()
            line?.close()
            targetDataLine = null
            
            val pcmData = audioData.toByteArray()
            audioData.close()
            
            if (pcmData.isEmpty()) return@withContext null
            
            // Wrap raw PCM in WAV format for Gemini compatibility
            val wavOutput = ByteArrayOutputStream()
            val audioFormat = AudioFormat(16000.0f, 16, 1, true, false)
            val audioInputStream = AudioInputStream(
                pcmData.inputStream(),
                audioFormat,
                pcmData.size.toLong() / audioFormat.frameSize
            )
            AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, wavOutput)
            wavOutput.toByteArray()
        } catch (e: Exception) {
            isCurrentlyRecording = false
            targetDataLine?.close()
            targetDataLine = null
            null
        }
    }
    
    actual fun requestPermission(callback: (Boolean) -> Unit) {
        // Actually test if we can open the mic
        try {
            val format = AudioFormat(16000.0f, 16, 1, true, false)
            val info = DataLine.Info(TargetDataLine::class.java, format)
            if (AudioSystem.isLineSupported(info)) {
                val line = AudioSystem.getLine(info) as TargetDataLine
                line.open(format)
                line.close()
                callback(true)
            } else {
                println("AudioRecorder: No supported audio line found")
                callback(false)
            }
        } catch (e: Exception) {
            println("AudioRecorder: Permission check failed: ${e.message}")
            callback(false)
        }
    }

    actual fun isRecording(): Boolean = isCurrentlyRecording
}