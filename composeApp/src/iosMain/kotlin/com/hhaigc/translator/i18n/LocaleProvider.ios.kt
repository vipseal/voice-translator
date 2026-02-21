package com.hhaigc.translator.i18n

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

actual fun getDeviceLocale(): String = NSLocale.currentLocale.languageCode
