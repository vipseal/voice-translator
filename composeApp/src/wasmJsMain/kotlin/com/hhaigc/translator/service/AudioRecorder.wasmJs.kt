package com.hhaigc.translator.service

import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.js.Promise

// JS interop functions
@JsFun("""
() => {
    return navigator.mediaDevices.getUserMedia({ audio: true });
}
""")
private external fun jsGetUserMedia(): Promise<JsAny>

@JsFun("""
(stream) => {
    const options = { mimeType: 'audio/webm;codecs=opus' };
    let mr;
    try {
        mr = new MediaRecorder(stream, options);
    } catch(e) {
        mr = new MediaRecorder(stream);
    }
    window.__audioChunks = [];
    mr.ondataavailable = (e) => { if (e.data.size > 0) window.__audioChunks.push(e.data); };
    mr.start();
    window.__mediaRecorder = mr;
    return true;
}
""")
private external fun jsStartRecording(stream: JsAny): Boolean

@JsFun("""
() => {
    return new Promise((resolve) => {
        const mr = window.__mediaRecorder;
        if (!mr || mr.state === 'inactive') { resolve(null); return; }
        mr.onstop = () => {
            const blob = new Blob(window.__audioChunks, { type: mr.mimeType || 'audio/webm' });
            const reader = new FileReader();
            reader.onloadend = () => {
                const arr = new Uint8Array(reader.result);
                resolve(arr);
            };
            reader.readAsArrayBuffer(blob);
        };
        mr.stop();
        mr.stream.getTracks().forEach(t => t.stop());
    });
}
""")
private external fun jsStopRecording(): Promise<JsAny?>

@JsFun("() => window.__mediaRecorder && window.__mediaRecorder.state === 'recording'")
private external fun jsIsRecording(): Boolean

@JsFun("""
() => {
    return navigator.mediaDevices.getUserMedia({ audio: true })
        .then(() => true)
        .catch(() => false);
}
""")
private external fun jsCheckPermission(): Promise<JsBoolean>

@JsFun("(arr) => arr.length")
private external fun jsArrayLength(arr: JsAny): Int

@JsFun("(arr, i) => arr[i]")
private external fun jsArrayGet(arr: JsAny, i: Int): JsByte

private fun JsAny.toByteArray(): ByteArray {
    val len = jsArrayLength(this)
    return ByteArray(len) { i -> jsArrayGet(this, i) }
}

actual class AudioRecorder {
    private var stream: JsAny? = null

    actual fun hasPermission(): Boolean = true

    actual suspend fun startRecording(): Boolean {
        return suspendCancellableCoroutine { cont ->
            jsGetUserMedia().then<JsAny> { mediaStream ->
                stream = mediaStream
                val result = jsStartRecording(mediaStream)
                cont.resume(result)
                mediaStream
            }.catch { _ ->
                cont.resume(false)
                null
            }
        }
    }

    actual suspend fun stopRecording(): ByteArray? {
        return suspendCancellableCoroutine { cont ->
            jsStopRecording().then<JsAny?> { result ->
                if (result != null) {
                    cont.resume(result.toByteArray())
                } else {
                    cont.resume(null)
                }
                result
            }.catch { _ ->
                cont.resume(null)
                null
            }
        }
    }

    actual fun requestPermission(callback: (Boolean) -> Unit) {
        jsCheckPermission().then<JsBoolean> { result ->
            callback(result.toBoolean())
            result
        }.catch { _ ->
            callback(false)
            null
        }
    }

    actual fun isRecording(): Boolean = jsIsRecording()
}
