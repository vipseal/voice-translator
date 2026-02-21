package com.hhaigc.translator.i18n

@JsFun("function() { return navigator.language || navigator.userLanguage || 'en'; }")
private external fun getNavigatorLanguage(): String

actual fun getDeviceLocale(): String {
    return try { getNavigatorLanguage().take(2) } catch (_: Exception) { "en" }
}
