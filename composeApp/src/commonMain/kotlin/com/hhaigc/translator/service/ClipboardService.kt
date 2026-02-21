package com.hhaigc.translator.service

expect class ClipboardService() {
    fun readText(onResult: (String?) -> Unit)
    fun writeText(text: String)
}
