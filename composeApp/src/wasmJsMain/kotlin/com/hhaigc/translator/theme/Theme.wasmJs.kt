package com.hhaigc.translator.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import org.jetbrains.compose.resources.Font
import voicetranslator.composeapp.generated.resources.Res
import voicetranslator.composeapp.generated.resources.droid_sans_fallback
import voicetranslator.composeapp.generated.resources.noto_sans_arabic
import voicetranslator.composeapp.generated.resources.noto_sans_devanagari
import voicetranslator.composeapp.generated.resources.noto_sans_thai

@Composable
actual fun platformTypography(): Typography {
    val fontFamily = FontFamily(
        Font(Res.font.droid_sans_fallback),
        Font(Res.font.noto_sans_thai),
        Font(Res.font.noto_sans_arabic),
        Font(Res.font.noto_sans_devanagari),
    )
    val d = Typography()
    return Typography(
        displayLarge = d.displayLarge.copy(fontFamily = fontFamily),
        displayMedium = d.displayMedium.copy(fontFamily = fontFamily),
        displaySmall = d.displaySmall.copy(fontFamily = fontFamily),
        headlineLarge = d.headlineLarge.copy(fontFamily = fontFamily),
        headlineMedium = d.headlineMedium.copy(fontFamily = fontFamily),
        headlineSmall = d.headlineSmall.copy(fontFamily = fontFamily),
        titleLarge = d.titleLarge.copy(fontFamily = fontFamily),
        titleMedium = d.titleMedium.copy(fontFamily = fontFamily),
        titleSmall = d.titleSmall.copy(fontFamily = fontFamily),
        bodyLarge = d.bodyLarge.copy(fontFamily = fontFamily),
        bodyMedium = d.bodyMedium.copy(fontFamily = fontFamily),
        bodySmall = d.bodySmall.copy(fontFamily = fontFamily),
        labelLarge = d.labelLarge.copy(fontFamily = fontFamily),
        labelMedium = d.labelMedium.copy(fontFamily = fontFamily),
        labelSmall = d.labelSmall.copy(fontFamily = fontFamily),
    )
}
