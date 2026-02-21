package com.hhaigc.translator.service

import platform.UIKit.UIPasteboard

actual class ClipboardService {
    actual fun readText(onResult: (String?) -> Unit) {
        val text = UIPasteboard.generalPasteboard.string
        onResult(if (text.isNullOrBlank()) null else text)
    }

    actual fun writeText(text: String) {
        UIPasteboard.generalPasteboard.string = text
    }
}
