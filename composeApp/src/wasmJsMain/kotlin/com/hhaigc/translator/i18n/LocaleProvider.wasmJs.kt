package com.hhaigc.translator.i18n

actual fun getDeviceLocale(): String {
    return js("navigator.language || navigator.userLanguage || 'en'").toString().take(2)
}
