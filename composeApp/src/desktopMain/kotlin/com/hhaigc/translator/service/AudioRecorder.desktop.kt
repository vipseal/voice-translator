package com.hhaigc.translator.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
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
                44100.0f, // Sample rate
                16,       // Sample size in bits
                1,        // Channels (mono)
                true,     // Signed
                false     // Big endian
            )
            
            val dataLineInfo = DataLine.Info(TargetDataLine::class.java, audioFormat)
            
            if (!AudioSystem.isLineSupported(dataLineInfo)) {
                return@withContext false
            }
            
            targetDataLine = AudioSystem.getLine(dataLineInfo) as TargetDataLine
            targetDataLine?.open(audioFormat)
            targetDataLine?.start()
            
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
            
            targetDataLine?.stop()
            targetDataLine?.close()
            targetDataLine = null
            
            val recordedData = audioData.toByteArray()
            audioData.close()
            
            recordedData.takeIf { it.isNotEmpty() }
        } catch (e: Exception) {
            isCurrentlyRecording = false
            targetDataLine?.close()
            targetDataLine = null
            null
        }
    }
    
    actual fun isRecording(): Boolean = isCurrentlyRecording
}