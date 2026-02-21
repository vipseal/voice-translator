package com.hhaigc.translator.service

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.js.Promise

@JsFun("() => navigator.clipboard.readText()")
private external fun jsClipboardReadText(): Promise<JsString>

@JsFun("(text) => navigator.clipboard.writeText(text)")
private external fun jsClipboardWriteText(text: JsString): Promise<JsAny>

actual class ClipboardService {
    private val scope = MainScope()

    actual fun readText(onResult: (String?) -> Unit) {
        try {
            val promise = jsClipboardReadText()
            promise.then<JsString> { jsText ->
                val text = jsText.toString()
                onResult(if (text.isBlank()) null else text)
                jsText
            }.catch { _ ->
                onResult(null)
                null
            }
        } catch (e: Exception) {
            onResult(null)
        }
    }

    actual fun writeText(text: String) {
        try {
            jsClipboardWriteText(text.toJsString())
        } catch (_: Exception) {}
    }
}
