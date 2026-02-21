package com.hhaigc.translator.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import androidx.core.content.ContextCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException

actual class AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var isCurrentlyRecording = false
    
    companion object {
        private var appContext: Context? = null
        private var permissionRequester: (() -> Unit)? = null
        private var permissionCallback: ((Boolean) -> Unit)? = null
        
        fun initWithContext(context: Context) {
            appContext = context.applicationContext
        }
        
        fun setPermissionRequester(requester: () -> Unit) {
            permissionRequester = requester
        }
        
        fun onPermissionResult(granted: Boolean) {
            permissionCallback?.invoke(granted)
            permissionCallback = null
        }
    }
    
    private fun getContext(): Context? {
        return appContext
    }
    
    actual fun hasPermission(): Boolean {
        return try {
            val context = getContext() ?: return false
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
    
    actual fun requestPermission(callback: (Boolean) -> Unit) {
        if (hasPermission()) {
            callback(true)
            return
        }
        permissionCallback = callback
        permissionRequester?.invoke() ?: callback(false)
    }
    
    actual suspend fun startRecording(): Boolean = withContext(Dispatchers.IO) {
        try {
            if (isCurrentlyRecording) {
                return@withContext false
            }
            
            val context = getContext() ?: return@withContext false
            if (!hasPermission()) {
                return@withContext false
            }
            
            // Create output file
            outputFile = File.createTempFile("recording", ".wav", context.cacheDir)
            
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile?.absolutePath)
                
                try {
                    prepare()
                    start()
                    isCurrentlyRecording = true
                    true
                } catch (e: IOException) {
                    false
                }
            }
            
            isCurrentlyRecording
        } catch (e: Exception) {
            isCurrentlyRecording = false
            false
        }
    }
    
    actual suspend fun stopRecording(): ByteArray? = withContext(Dispatchers.IO) {
        try {
            if (!isCurrentlyRecording || mediaRecorder == null) {
                return@withContext null
            }
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isCurrentlyRecording = false
            
            val audioData = outputFile?.readBytes()
            outputFile?.delete()
            outputFile = null
            
            audioData
        } catch (e: Exception) {
            isCurrentlyRecording = false
            mediaRecorder?.release()
            mediaRecorder = null
            outputFile?.delete()
            outputFile = null
            null
        }
    }
    
    actual fun isRecording(): Boolean = isCurrentlyRecording
}
