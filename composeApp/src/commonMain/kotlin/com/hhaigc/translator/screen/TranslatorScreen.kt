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
import androidx.compose.ui.text.AnnotatedString
import com.hhaigc.translator.service.ClipboardService
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    onNavigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val clipboardService = remember { ClipboardService() }
    val audioRecorder = remember { AudioRecorder() }
    val settingsStore = remember { SettingsStore() }
    val geminiService = remember { GeminiService() }
    val ttsService = remember { TtsService() }
    val soundService = remember { SoundService() }
    val haptic = LocalHapticFeedback.current
    val s = AppStrings.current

    fun withFeedback(action: () -> Unit) {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        soundService.playClick()
        action()
    }
    
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
    val snackbarHostState = remember { SnackbarHostState() }
    var sourceExpanded by remember { mutableStateOf(false) }
    var showSourceInput by remember { mutableStateOf(false) }
    
    // Collect enabled languages
    LaunchedEffect(Unit) {
        settingsStore.getEnabledLanguages().collect { languages ->
            enabledLanguages = languages.filter { it.isEnabled }
        }
    }
    
        fun setStatus(text: String) {
        statusText = text
    }
    fun showToast(text: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            val job = launch {
                snackbarHostState.showSnackbar(text, duration = SnackbarDuration.Indefinite)
            }
            delay(1000L)
            snackbarHostState.currentSnackbarData?.dismiss()
            job.cancel()
        }
    }
    
    fun copyToClipboard(text: String, label: String = "") {
        clipboardService.writeText(text)
        if (label.isNotEmpty()) {
            showToast("${s.copiedLabel} $label")
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
        showToast(s.copiedAllTranslations)
    }
    
    fun translateSourceText(text: String = sourceText) {
        if (text.isBlank()) return
        scope.launch {
            isProcessing = true
            error = null
            sourceText = text.trim()
            detectedLanguage = ""
            setStatus(s.translating)
            
            try {
                val targetLanguages = enabledLanguages.map { it.code }
                val result = geminiService.detectAndTranslate(sourceText, targetLanguages)
                result.fold(
                    onSuccess = { (transcription, translationsMap) ->
                        detectedLanguage = transcription.lang
                        translations = translationsMap
                        showToast(s.translateDone)
                        showSourceInput = false
                    },
                    onFailure = { e ->
                        error = s.errorTranslationFailed
                        showToast(s.translateFailed)
                    }
                )
            } catch (e: Exception) {
                error = s.errorSomethingWrong
                showToast(s.somethingWrong)
            } finally {
                isProcessing = false
            }
        }
    }

    fun translateClipboardText() {
        clipboardService.readText { clipText ->
            if (clipText.isNullOrBlank()) {
                showToast(s.clipboardEmpty)
            } else {
                translateSourceText(clipText)
            }
        }
    }

    fun startOrStopRecording() {
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
                        setStatus(s.translating)
                        val targetLanguages = enabledLanguages.map { it.code }
                        val result = geminiService.transcribeAndTranslate(audioData, targetLanguages)
                        result.fold(
                            onSuccess = { (transcription, translationsMap) ->
                                sourceText = transcription.text
                                detectedLanguage = transcription.lang
                                translations = translationsMap
                                showToast(s.translateDone)
                                showSourceInput = false
                            },
                            onFailure = { exception ->
                                error = s.errorTranslationFailed
                                showToast(s.translateFailed)
                            }
                        )
                    } else {
                        error = s.errorFailedRecord
                        showToast(s.recordingFailed)
                    }
                } catch (e: Exception) {
                    error = s.errorRecording
                    showToast(s.recordingError)
                } finally {
                    isProcessing = false
                }
            }
        } else {
            soundService.playStartRecording()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            audioRecorder.requestPermission { granted ->
                if (granted) {
                    scope.launch {
                        if (audioRecorder.startRecording()) {
                            isRecording = true
                            error = null
                            setStatus(s.recording)
                        } else {
                            error = s.errorFailedStart
                            showToast(s.cannotStartRecording)
                        }
                    }
                } else {
                    error = s.errorMicPermission
                    showToast(s.micPermissionRequired)
                }
            }
        }
    }
    
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header - compact
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = s.appTitle,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            IconButton(onClick = { withFeedback { onNavigateToSettings() } }) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = s.settings,
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // Linear progress indicator
        if (isProcessing) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Action bar moved to bottom

        // Source text card - only when showSourceInput is true
        if (showSourceInput) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(12.dp)
                        .animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = s.sourceLabel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                letterSpacing = 1.sp
                            )
                            if (detectedLanguage.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = "$detectedLanguage",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        IconButton(
                            onClick = { withFeedback { showSourceInput = false } },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Collapse",
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = sourceText,
                        onValueChange = { sourceText = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = {
                            Text(
                                text = s.recordHint,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                fontStyle = FontStyle.Italic
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 24.sp
                        ),
                        maxLines = if (sourceExpanded) Int.MAX_VALUE else 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    if (sourceText.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { withFeedback { sourceText = ""; translations = emptyMap(); detectedLanguage = "" } }) {
                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(s.clear, fontSize = 12.sp)
                            }
                            FilledTonalButton(
                                onClick = { withFeedback { translateSourceText() } },
                                enabled = !isProcessing,
                                shape = RoundedCornerShape(20.dp),
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(s.translate, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
        
        // Translation cards - vertical list
        if (translations.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Copy all as first item
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 2.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = { withFeedback { copyAllTranslations() } },
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.ContentCopy, null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(s.copyAllButton, fontSize = 11.sp)
                        }
                    }
                }
                items(enabledLanguages.filter { translations.containsKey(it.code) }) { language ->
                    TranslationCard(
                        language = language,
                        translation = translations[language.code] ?: "",
                        onCopy = {
                            withFeedback {
                                copyToClipboard(
                                    translations[language.code] ?: "",
                                    language.name
                                )
                            }
                        },
                        onTTS = {
                            withFeedback {
                                val text = translations[language.code]
                                if (!text.isNullOrEmpty()) {
                                    ttsService.speak(text, language.code)
                                }
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
                    Text(
                        text = statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
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
        
        // Bottom action bar - always visible
        Spacer(modifier = Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit/type button (left)
            FilledTonalIconButton(
                onClick = { withFeedback { showSourceInput = !showSourceInput } },
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = if (showSourceInput) MaterialTheme.colorScheme.primaryContainer
                                     else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (showSourceInput) MaterialTheme.colorScheme.onPrimaryContainer
                                   else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Type",
                    modifier = Modifier.size(20.dp)
                )
            }
            // Record button (center, large, primary)
            FilledIconButton(
                onClick = { withFeedback { startOrStopRecording() } },
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isRecording) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isRecording) "Stop" else "Record",
                    modifier = Modifier.size(28.dp)
                )
            }
            // Paste button (right)
            FilledTonalIconButton(
                onClick = { withFeedback { translateClipboardText() } },
                modifier = Modifier.size(44.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Icon(
                    imageVector = Icons.Default.ContentPaste,
                    contentDescription = "Paste",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 120.dp)
    ) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface,
            shape = RoundedCornerShape(12.dp),
        )
    }
    } // end Box
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
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    com.hhaigc.translator.ui.FlagImage(
                        langCode = language.code,
                        size = 16.dp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = language.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )
                }
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