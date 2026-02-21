package com.hhaigc.translator.i18n

actual fun getDeviceLocale(): String = java.util.Locale.getDefault().language
