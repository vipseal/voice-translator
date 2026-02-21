package com.hhaigc.translator.i18n

object AppStrings {
    val current: AppStringSet
        get() {
            val locale = getDeviceLocale().lowercase().take(2)
            return when (locale) {
                "zh" -> StringsZh
                "th" -> StringsTh
                "ja" -> StringsJa
                "ko" -> StringsKo
                "ar" -> StringsAr
                "fr" -> StringsFr
                "es" -> StringsEs
                else -> StringsEn
            }
        }
}
