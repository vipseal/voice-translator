package com.hhaigc.translator.service

import java.awt.Toolkit
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection

actual class ClipboardService {
    actual fun readText(onResult: (String?) -> Unit) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            val text = clipboard.getData(DataFlavor.stringFlavor) as? String
            onResult(if (text.isNullOrBlank()) null else text)
        } catch (e: Exception) {
            onResult(null)
        }
    }

    actual fun writeText(text: String) {
        try {
            val clipboard = Toolkit.getDefaultToolkit().systemClipboard
            clipboard.setContents(StringSelection(text), null)
        } catch (_: Exception) {}
    }
}
