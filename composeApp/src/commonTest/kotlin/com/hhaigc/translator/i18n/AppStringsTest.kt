package com.hhaigc.translator.i18n

import kotlin.test.Test
import kotlin.test.assertTrue

class AppStringsTest {

    private val allStringSets: List<Pair<String, AppStringSet>> = listOf(
        "en" to StringsEn,
        "zh" to StringsZh,
        "th" to StringsTh,
        "ja" to StringsJa,
        "ko" to StringsKo,
        "ar" to StringsAr,
        "fr" to StringsFr,
        "es" to StringsEs,
    )

    private fun assertAllFieldsNonEmpty(name: String, strings: AppStringSet) {
        val fields = listOf(
            "appTitle" to strings.appTitle,
            "sourceLabel" to strings.sourceLabel,
            "recordHint" to strings.recordHint,
            "copyAllButton" to strings.copyAllButton,
            "copiedLabel" to strings.copiedLabel,
            "copiedAllTranslations" to strings.copiedAllTranslations,
            "clipboardEmpty" to strings.clipboardEmpty,
            "translating" to strings.translating,
            "translateDone" to strings.translateDone,
            "translateFailed" to strings.translateFailed,
            "somethingWrong" to strings.somethingWrong,
            "recognizingSpeech" to strings.recognizingSpeech,
            "recognitionFailed" to strings.recognitionFailed,
            "recording" to strings.recording,
            "cannotStartRecording" to strings.cannotStartRecording,
            "recordingFailed" to strings.recordingFailed,
            "recordingError" to strings.recordingError,
            "micPermissionRequired" to strings.micPermissionRequired,
            "recordingTapStop" to strings.recordingTapStop,
            "recordAndPaste" to strings.recordAndPaste,
            "statusDefault" to strings.statusDefault,
            "copySourceLabel" to strings.copySourceLabel,
            "copyTranslationLabel" to strings.copyTranslationLabel,
            "copyFooter" to strings.copyFooter,
            "expand" to strings.expand,
            "collapse" to strings.collapse,
            "errorTranslationFailed" to strings.errorTranslationFailed,
            "errorSomethingWrong" to strings.errorSomethingWrong,
            "errorVoiceRecognition" to strings.errorVoiceRecognition,
            "errorFailedRecord" to strings.errorFailedRecord,
            "errorRecording" to strings.errorRecording,
            "errorFailedStart" to strings.errorFailedStart,
            "errorMicPermission" to strings.errorMicPermission,
            "activationSubtitle" to strings.activationSubtitle,
            "activationCodeLabel" to strings.activationCodeLabel,
            "activationCodeEmpty" to strings.activationCodeEmpty,
            "activationCodeInvalid" to strings.activationCodeInvalid,
            "activateButton" to strings.activateButton,
            "settings" to strings.settings,
            "appearance" to strings.appearance,
            "themeAuto" to strings.themeAuto,
            "themeLight" to strings.themeLight,
            "themeDark" to strings.themeDark,
            "translationLanguages" to strings.translationLanguages,
            "selectLanguagesHint" to strings.selectLanguagesHint,
            "about" to strings.about,
            "aboutDescription" to strings.aboutDescription,
            "versionLabel" to strings.versionLabel,
            "clear" to strings.clear,
            "translate" to strings.translate,
            "resetAuth" to strings.resetAuth,
            "resetAuthConfirm" to strings.resetAuthConfirm,
            "resetAuthDescription" to strings.resetAuthDescription,
            "cancel" to strings.cancel,
            "reset" to strings.reset,
            "authentication" to strings.authentication,
        )
        fields.forEach { (fieldName, value) ->
            assertTrue(
                value.isNotBlank(),
                "[$name] field '$fieldName' should not be blank"
            )
        }
    }

    @Test
    fun allLanguageSetsHaveNonEmptyStrings() {
        allStringSets.forEach { (name, strings) ->
            assertAllFieldsNonEmpty(name, strings)
        }
    }

    @Test
    fun englishStringsAreComplete() {
        assertAllFieldsNonEmpty("en", StringsEn)
    }

    @Test
    fun chineseStringsAreComplete() {
        assertAllFieldsNonEmpty("zh", StringsZh)
    }

    @Test
    fun thaiStringsAreComplete() {
        assertAllFieldsNonEmpty("th", StringsTh)
    }

    @Test
    fun japaneseStringsAreComplete() {
        assertAllFieldsNonEmpty("ja", StringsJa)
    }

    @Test
    fun koreanStringsAreComplete() {
        assertAllFieldsNonEmpty("ko", StringsKo)
    }

    @Test
    fun arabicStringsAreComplete() {
        assertAllFieldsNonEmpty("ar", StringsAr)
    }

    @Test
    fun frenchStringsAreComplete() {
        assertAllFieldsNonEmpty("fr", StringsFr)
    }

    @Test
    fun spanishStringsAreComplete() {
        assertAllFieldsNonEmpty("es", StringsEs)
    }

    @Test
    fun exactlyEightLanguageSetsExist() {
        assertEquals(8, allStringSets.size)
    }

    private fun assertEquals(expected: Int, actual: Int) {
        kotlin.test.assertEquals(expected, actual, "Expected $expected language sets but got $actual")
    }
}
