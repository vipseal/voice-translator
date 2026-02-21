package com.hhaigc.translator.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context

actual class ClipboardService {
    companion object {
        private var appContext: Context? = null
        fun init(context: Context) {
            appContext = context.applicationContext
        }
        private fun getContext(): Context = appContext ?: throw IllegalStateException("ClipboardService not initialized")
    }

    actual fun readText(onResult: (String?) -> Unit) {
        try {
            val cm = getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = cm.primaryClip
            val text = clip?.getItemAt(0)?.text?.toString()
            onResult(if (text.isNullOrBlank()) null else text)
        } catch (e: Exception) {
            onResult(null)
        }
    }

    actual fun writeText(text: String) {
        try {
            val cm = getContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            cm.setPrimaryClip(ClipData.newPlainText("translation", text))
        } catch (_: Exception) {}
    }
}
