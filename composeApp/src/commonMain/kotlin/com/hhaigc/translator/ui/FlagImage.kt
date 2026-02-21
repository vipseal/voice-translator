package com.hhaigc.translator.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import voicetranslator.composeapp.generated.resources.*

private val flagMap = mapOf(
    "en" to Res.drawable.flag_en,
    "zh" to Res.drawable.flag_zh,
    "ja" to Res.drawable.flag_ja,
    "ko" to Res.drawable.flag_ko,
    "ar" to Res.drawable.flag_ar,
    "fr" to Res.drawable.flag_fr,
    "de" to Res.drawable.flag_de,
    "es" to Res.drawable.flag_es,
    "ru" to Res.drawable.flag_ru,
    "th" to Res.drawable.flag_th,
    "vi" to Res.drawable.flag_vi,
    "pt" to Res.drawable.flag_pt,
    "hi" to Res.drawable.flag_hi,
    "id" to Res.drawable.flag_id,
    "tr" to Res.drawable.flag_tr,
)

@Composable
fun FlagImage(
    langCode: String,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp
) {
    val resource = flagMap[langCode] ?: return
    Image(
        painter = painterResource(resource),
        contentDescription = langCode,
        modifier = modifier.size(width = (size.value * 4 / 3).dp, height = size)
    )
}
