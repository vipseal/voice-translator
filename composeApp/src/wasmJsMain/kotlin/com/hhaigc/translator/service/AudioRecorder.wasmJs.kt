package com.hhaigc.translator.service

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.w3c.dom.MediaRecorder
import org.w3c.dom.Navigator
import org.w3c.dom.get
import org.w3c.dom.mediacapture.MediaStream
import org.w3c.dom.mediacapture.MediaStreamConstraints
import org.w3c.files.Blob
import org.w3c.files.FileReader
import kotlin.coroutines.resume
import kotlin.js.Promise

actual class AudioRecorder {
    private var mediaRecorder: MediaRecorder? = null
    private var mediaStream: MediaStream? = null
    private var audioChunks = mutableListOf<dynamic>()
    private var isCurrentlyRecording = false
    
    actual fun hasPermission(): Boolean {
        return js("navigator.mediaDevices && navigator.mediaDevices.getUserMedia") != null
    }
    
    actual suspend fun startRecording(): Boolean = withContext(Dispatchers.Main) {
        try {
            if (isCurrentlyRecording || !hasPermission()) {
                return@withContext false
            }
            
            mediaStream = suspendCancellableCoroutine { continuation ->
                val constraints = MediaStreamConstraints().apply {
                    audio = true
                    video = false
                }
                
                val promise = js("navigator.mediaDevices.getUserMedia(constraints)") as Promise<MediaStream>
                promise.then({ stream ->
                    continuation.resume(stream)
                }, { error ->
                    continuation.resume(null)
                })
            }
            
            if (mediaStream == null) {
                return@withContext false
            }
            
            audioChunks.clear()
            mediaRecorder = MediaRecorder(mediaStream!!).apply {
                ondataavailable = { event ->
                    if (event.asDynamic().data.size > 0) {
                        audioChunks.add(event.asDynamic().data)
                    }
                }
            }
            
            mediaRecorder?.start()
            isCurrentlyRecording = true
            
            true
        } catch (e: Exception) {
            isCurrentlyRecording = false
            false
        }
    }
    
    actual suspend fun stopRecording(): ByteArray? = withContext(Dispatchers.Main) {
        try {
            if (!isCurrentlyRecording || mediaRecorder == null) {
                return@withContext null
            }
            
            isCurrentlyRecording = false
            
            val audioData = suspendCancellableCoroutine<ByteArray?> { continuation ->
                mediaRecorder?.onstop = {
                    try {
                        val blob = Blob(audioChunks.toTypedArray(), BlobPropertyBag("audio/wav"))
                        val reader = FileReader()
                        reader.onload = {
                            val arrayBuffer = reader.result
                            val byteArray = js("new Uint8Array(arrayBuffer)") as ByteArray
                            continuation.resume(byteArray)
                        }
                        reader.onerror = {
                            continuation.resume(null)
                        }
                        reader.readAsArrayBuffer(blob)
                    } catch (e: Exception) {
                        continuation.resume(null)
                    }
                }
                
                mediaRecorder?.stop()
            }
            
            // Clean up
            mediaStream?.getTracks()?.forEach { track ->
                track.stop()
            }
            mediaStream = null
            mediaRecorder = null
            audioChunks.clear()
            
            audioData
        } catch (e: Exception) {
            isCurrentlyRecording = false
            mediaStream?.getTracks()?.forEach { track ->
                track.stop()
            }
            mediaStream = null
            mediaRecorder = null
            audioChunks.clear()
            null
        }
    }
    
    actual fun requestPermission(callback: (Boolean) -> Unit) {
        callback(hasPermission())
    }

    actual fun isRecording(): Boolean = isCurrentlyRecording
}

// Helper for BlobPropertyBag
private external interface BlobPropertyBag {
    val type: String
}

private fun BlobPropertyBag(type: String): BlobPropertyBag = js("({type: type})")