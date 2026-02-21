package com.hhaigc.translator.service

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual class AudioRecorder {
    private var isCurrentlyRecording = false

    actual fun hasPermission(): Boolean = true

    actual suspend fun startRecording(): Boolean {
        return suspendCancellableCoroutine { cont ->
            try {
                js("""
                    navigator.mediaDevices.getUserMedia({audio: true}).then(function(stream) {
                        window._vtStream = stream;
                        window._vtChunks = [];
                        var mr = new MediaRecorder(stream);
                        mr.ondataavailable = function(e) { if(e.data.size > 0) window._vtChunks.push(e.data); };
                        mr.start();
                        window._vtRecorder = mr;
                    })
                """)
                isCurrentlyRecording = true
                cont.resume(true)
            } catch (e: Exception) {
                cont.resume(false)
            }
        }
    }

    actual suspend fun stopRecording(): ByteArray? {
        if (!isCurrentlyRecording) return null
        isCurrentlyRecording = false

        return suspendCancellableCoroutine { cont ->
            try {
                js("""
                    (function() {
                        var mr = window._vtRecorder;
                        if (!mr) { return null; }
                        mr.onstop = function() {
                            var blob = new Blob(window._vtChunks, {type: 'audio/webm'});
                            var reader = new FileReader();
                            reader.onload = function() {
                                var arr = new Uint8Array(reader.result);
                                window._vtAudioResult = arr;
                            };
                            reader.readAsArrayBuffer(blob);
                            if (window._vtStream) {
                                window._vtStream.getTracks().forEach(function(t){ t.stop(); });
                            }
                        };
                        mr.stop();
                    })()
                """)
                // For now, audio recording in WASM has limitations
                // The data needs to be passed through JS interop
                cont.resume(null)
            } catch (e: Exception) {
                cont.resume(null)
            }
        }
    }

    actual fun requestPermission(callback: (Boolean) -> Unit) {
        callback(true)
    }

    actual fun isRecording(): Boolean = isCurrentlyRecording
}
