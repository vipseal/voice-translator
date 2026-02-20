package com.hhaigc.translator.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import platform.AVFoundation.*
import platform.Foundation.*
import platform.darwin.NSObject

actual class AudioRecorder {
    private var audioRecorder: AVAudioRecorder? = null
    private var outputURL: NSURL? = null
    private var isCurrentlyRecording = false
    
    actual fun hasPermission(): Boolean {
        return AVAudioSession.sharedInstance().recordPermission() == AVAudioSessionRecordPermissionGranted
    }
    
    actual suspend fun startRecording(): Boolean = withContext(Dispatchers.Main) {
        try {
            if (isCurrentlyRecording) {
                return@withContext false
            }
            
            if (!hasPermission()) {
                // Request permission
                AVAudioSession.sharedInstance().requestRecordPermission { granted ->
                    // Permission result will be handled in the next call
                }
                return@withContext false
            }
            
            // Configure audio session
            val audioSession = AVAudioSession.sharedInstance()
            audioSession.setCategoryError(AVAudioSessionCategoryRecord, null)
            audioSession.setActiveError(true, null)
            
            // Create temporary file URL
            val documentsPath = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).first() as String
            
            outputURL = NSURL.fileURLWithPath("$documentsPath/recording.wav")
            
            // Audio settings
            val settings = mapOf<Any?, Any?>(
                AVFormatIDKey to kAudioFormatLinearPCM,
                AVSampleRateKey to 44100.0,
                AVNumberOfChannelsKey to 1,
                AVLinearPCMBitDepthKey to 16,
                AVLinearPCMIsBigEndianKey to false,
                AVLinearPCMIsFloatKey to false
            )
            
            audioRecorder = AVAudioRecorder.alloc().initWithURL(
                outputURL!!,
                settings as Map<Any?, Any?>,
                null
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
                    ByteArray(data.length.toInt()) { index ->
                        data.bytes()!!.reinterpret<ByteVar>()[index]
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
    
    actual fun isRecording(): Boolean = isCurrentlyRecording
}