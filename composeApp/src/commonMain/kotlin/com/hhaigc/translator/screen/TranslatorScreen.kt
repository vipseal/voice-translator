package com.hhaigc.translator.screen

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hhaigc.translator.i18n.AppStrings
import com.hhaigc.translator.model.Language
import com.hhaigc.translator.model.TranscriptionResult
import com.hhaigc.translator.service.AudioRecorder
import com.hhaigc.translator.service.GeminiService
import com.hhaigc.translator.service.SoundService
import com.hhaigc.translator.service.TtsService
import com.hhaigc.translator.store.SettingsStore
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    onNavigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val audioRecorder = remember { AudioRecorder() }
    val settingsStore = remember { SettingsStore() }
    val geminiService = remember { GeminiService() }
    val ttsService = remember { TtsService() }
    val soundService = remember { SoundService() }
    val haptic = LocalHapticFeedback.current
    val s = AppStrings.current
    
    // Load API key from settings
    LaunchedEffect(Unit) {
        val apiKey = settingsStore.getApiKey()
        if (apiKey.isNotEmpty()) {
            geminiService.updateApiKey(apiKey)
        }
    }
    
    var isRecording by remember { mutableStateOf(false) }
    var sourceText by remember { mutableStateOf("") }
    var detectedLanguage by remember { mutableStateOf("") }
    var translations by remember { mutableStateOf(mapOf<String, String>()) }
    var enabledLanguages by remember { mutableStateOf(emptyList<Language>()) }
    var isProcessing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf(s.statusDefault) }
    var sourceExpanded by remember { mutableStateOf(false) }
    
    // Collect enabled languages
    LaunchedEffect(Unit) {
        settingsStore.getEnabledLanguages().collect { languages ->
            enabledLanguages = languages.filter { it.isEnabled }
        }
    }
    
    fun setStatus(text: String) { statusText = text }
    
    fun copyToClipboard(text: String, label: String = "") {
        clipboardManager.setText(AnnotatedString(text))
        if (label.isNotEmpty()) {
            statusText = "${s.copiedLabel} $label"
        }
    }
    
    fun copyAllTranslations() {
        val parts = mutableListOf<String>()
        enabledLanguages.forEach { lang ->
            val text = translations[lang.code]
            if (!text.isNullOrEmpty()) {
                parts.add("${lang.flag} ${lang.name}: $text")
            }
        }
        copyToClipboard(parts.joinToString("\n"))
        setStatus(s.copiedAllTranslations)
    }
    
    fun translateClipboardText() {
        val clipText = clipboardManager.getText()?.text
        if (clipText.isNullOrBlank()) {
            setStatus(s.clipboardEmpty)
            return
        }
        scope.launch {
            isProcessing = true
            error = null
            sourceText = clipText.trim()
            detectedLanguage = ""
            setStatus(s.translating)
            
            try {
                val targetLanguages = enabledLanguages.map { it.code }
                val result = geminiService.detectAndTranslate(sourceText, targetLanguages)
                result.fold(
                    onSuccess = { (transcription, translationsMap) ->
                        detectedLanguage = transcription.lang
                        translations = translationsMap
                        setStatus(s.translateDone)
                    },
                    onFailure = { e ->
                        error = s.errorTranslationFailed
                        setStatus(s.translateFailed)
                    }
                )
            } catch (e: Exception) {
                error = s.errorSomethingWrong
                setStatus(s.somethingWrong)
            } finally {
                isProcessing = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    modifier = Modifier.size(28.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = s.appTitle,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = s.settings,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Source text card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .then(if (sourceText.isNotEmpty()) Modifier.clickable { sourceExpanded = !sourceExpanded } else Modifier),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .animateContentSize()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = s.sourceLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    if (sourceText.isNotEmpty()) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (sourceExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (sourceExpanded) s.collapse else s.expand,
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (sourceExpanded) s.collapse else s.expand,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                if (sourceText.isNotEmpty()) {
                    Text(
                        text = sourceText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp,
                        maxLines = if (sourceExpanded) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (detectedLanguage.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Text(
                                text = "🔍 $detectedLanguage",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = s.recordHint,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontStyle = FontStyle.Italic
                    )
                }
            }
        }
        
        // Copy all button
        if (translations.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = { copyAllTranslations() },
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(s.copyAllButton, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        
        // Translation cards - vertical list
        if (translations.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(enabledLanguages.filter { translations.containsKey(it.code) }) { language ->
                    TranslationCard(
                        language = language,
                        translation = translations[language.code] ?: "",
                        onCopy = {
                            copyToClipboard(
                                translations[language.code] ?: "",
                                "${language.flag} ${language.name}"
                            )
                        },
                        onTTS = {
                            val text = translations[language.code]
                            if (!text.isNullOrEmpty()) {
                                ttsService.speak(text, language.code)
                            }
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        // Error message
        error?.let { errorMessage ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Text(
                    text = errorMessage,
                    modifier = Modifier.padding(16.dp),
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Record + Clipboard buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Record button
            FloatingActionButton(
                onClick = {
                    if (isRecording) {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        soundService.playStopRecording()
                        scope.launch {
                            isRecording = false
                            isProcessing = true
                            error = null
                            setStatus(s.recognizingSpeech)
                            
                            try {
                                val audioData = audioRecorder.stopRecording()
                                if (audioData != null) {
                                    val transcriptionResult = geminiService.transcribeAudio(audioData)
                                    transcriptionResult.fold(
                                        onSuccess = { transcription ->
                                            sourceText = transcription.text
                                            detectedLanguage = transcription.lang
                                            
                                            setStatus(s.translating)
                                            val targetLanguages = enabledLanguages.map { it.code }
                                            val translationResult = geminiService.translateText(transcription.text, targetLanguages)
                                            
                                            translationResult.fold(
                                                onSuccess = { translationsMap ->
                                                    translations = translationsMap
                                                    setStatus(s.translateDone)
                                                },
                                                onFailure = { exception ->
                                                    error = s.errorTranslationFailed
                                                    setStatus(s.translateFailed)
                                                }
                                            )
                                        },
                                        onFailure = { exception ->
                                            error = s.errorVoiceRecognition
                                            setStatus(s.recognitionFailed)
                                        }
                                    )
                                } else {
                                    error = s.errorFailedRecord
                                    setStatus(s.recordingFailed)
                                }
                            } catch (e: Exception) {
                                error = s.errorRecording
                                setStatus(s.recordingError)
                            } finally {
                                isProcessing = false
                            }
                        }
                    } else {
                        audioRecorder.requestPermission { granted ->
                            if (granted) {
                                scope.launch {
                                    if (audioRecorder.startRecording()) {
                                        isRecording = true
                                        error = null
                                        setStatus(s.recording)
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        soundService.playStartRecording()
                                    } else {
                                        error = s.errorFailedStart
                                        setStatus(s.cannotStartRecording)
                                    }
                                }
                            } else {
                                error = s.errorMicPermission
                                setStatus(s.micPermissionRequired)
                            }
                        }
                    }
                },
                modifier = Modifier.size(72.dp),
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop" else "Record",
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Clipboard paste button
            FloatingActionButton(
                onClick = { translateClipboardText() },
                modifier = Modifier.size(56.dp),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        
        Text(
            text = if (isRecording) s.recordingTapStop else s.recordAndPaste,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 8.dp)
        )
        
        // Status bar
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f),
            fontSize = 11.sp
        )
    }
}

@Composable
private fun TranslationCard(
    language: Language,
    translation: String,
    onCopy: () -> Unit,
    onTTS: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val s = AppStrings.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCopy() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${language.flag} ${language.name}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Row {
                    IconButton(
                        onClick = onCopy,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                    IconButton(
                        onClick = onTTS,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = "Play",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp,
                maxLines = if (expanded) Int.MAX_VALUE else 4,
                overflow = TextOverflow.Ellipsis
            )
            
            if (translation.length > 120) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .clickable { expanded = !expanded }
                ) {
                    Text(
                        text = if (expanded) s.collapse else s.expand,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp
                    )
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
