@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlinx.cinterop.BetaInteropApi::class)

package com.hhaigc.translator.service

import kotlinx.cinterop.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFAudio.*
import platform.Foundation.*
import platform.darwin.NSObject

actual class AudioRecorder {
    private var audioRecorder: AVAudioRecorder? = null
    private var outputURL: NSURL? = null
    private var isCurrentlyRecording = false
    
    actual fun hasPermission(): Boolean {
        return AVAudioSession.sharedInstance().recordPermission == AVAudioSessionRecordPermissionGranted
    }
    
    actual suspend fun startRecording(): Boolean = withContext(Dispatchers.Main) {
        try {
            if (isCurrentlyRecording) {
                return@withContext false
            }
            
            if (!hasPermission()) {
                AVAudioSession.sharedInstance().requestRecordPermission { _ -> }
                return@withContext false
            }
            
            // Configure audio session
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategory(AVAudioSessionCategoryRecord, error = null)
            audioSession.setActive(true, error = null)
            
            // Create temporary file URL
            val tempDir = NSTemporaryDirectory()
            outputURL = NSURL.fileURLWithPath("${tempDir}recording.wav")
            
            // Audio settings
            val settings = mapOf<Any?, Any?>(
                AVFormatIDKey to 1819304813L, // kAudioFormatLinearPCM
                AVSampleRateKey to 44100.0,
                AVNumberOfChannelsKey to 1L,
                AVLinearPCMBitDepthKey to 16L,
                AVLinearPCMIsBigEndianKey to false,
                AVLinearPCMIsFloatKey to false
            )
            
            audioRecorder = AVAudioRecorder(
                uRL = outputURL!!,
                settings = settings,
                error = null
            )
            
            val success = audioRecorder?.record() ?: false
            isCurrentlyRecording = success
            
            success
        } catch (e: Exception) {
            isCurrentlyRecording = false
            false
        }
    }
    
    actual suspend fun stopRecording(): ByteArray? = withContext(Dispatchers.Main) {
        try {
            if (!isCurrentlyRecording || audioRecorder == null) {
                return@withContext null
            }
            
            audioRecorder?.stop()
            isCurrentlyRecording = false
            
            val audioData = outputURL?.path?.let { path ->
                NSData.dataWithContentsOfFile(path)?.let { data ->
                    val length = data.length.toInt()
                    if (length == 0) return@let null
                    ByteArray(length).also { bytes ->
                        bytes.usePinned { pinned ->
                            data.getBytes(pinned.addressOf(0), data.length)
                        }
                    }
                }
            }
            
            // Clean up
            outputURL?.path?.let { NSFileManager.defaultManager.removeItemAtPath(it, null) }
            outputURL = null
            audioRecorder = null
            
            audioData
        } catch (e: Exception) {
            isCurrentlyRecording = false
            audioRecorder = null
            outputURL?.path?.let { NSFileManager.defaultManager.removeItemAtPath(it, null) }
            outputURL = null
            null
        }
    }
    
    actual fun requestPermission(callback: (Boolean) -> Unit) {
        callback(hasPermission())
    }

    actual fun isRecording(): Boolean = isCurrentlyRecording
}
