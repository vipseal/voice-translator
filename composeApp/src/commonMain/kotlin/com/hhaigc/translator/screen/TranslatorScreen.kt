package com.hhaigc.translator.screen

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hhaigc.translator.model.Language
import com.hhaigc.translator.model.TranscriptionResult
import com.hhaigc.translator.service.AudioRecorder
import com.hhaigc.translator.service.GeminiService
import com.hhaigc.translator.store.SettingsStore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    onNavigateToSettings: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    val audioRecorder = remember { AudioRecorder() }
    val geminiService = remember { GeminiService() }
    val settingsStore = remember { SettingsStore() }
    
    var isRecording by remember { mutableStateOf(false) }
    var sourceText by remember { mutableStateOf("") }
    var detectedLanguage by remember { mutableStateOf("") }
    var translations by remember { mutableStateOf(mapOf<String, String>()) }
    var enabledLanguages by remember { mutableStateOf(emptyList<Language>()) }
    var isProcessing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var statusText by remember { mutableStateOf("Gemini AI 翻译") }
    
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
            statusText = "✅ 已复制 $label"
        }
    }
    
    fun copyAllTranslations() {
        val detected = if (detectedLanguage.isNotEmpty()) " ($detectedLanguage)" else ""
        var result = "📝 原文$detected:\n$sourceText\n\n"
        result += "🌍 翻译结果:\n${"─".repeat(20)}\n"
        enabledLanguages.forEach { lang ->
            val text = translations[lang.code]
            if (!text.isNullOrEmpty()) {
                result += "\n${lang.flag} ${lang.name}:\n$text\n"
            }
        }
        result += "\n${"─".repeat(20)}\n⚡ VoiceTranslator by Gemini AI"
        copyToClipboard(result)
        setStatus("✅ 已复制全部翻译")
    }
    
    fun translateClipboardText() {
        val clipText = clipboardManager.getText()?.text
        if (clipText.isNullOrBlank()) {
            setStatus("❌ 剪贴板为空")
            return
        }
        scope.launch {
            isProcessing = true
            error = null
            sourceText = clipText.trim()
            detectedLanguage = ""
            setStatus("🌍 正在翻译...")
            
            try {
                val targetLanguages = enabledLanguages.map { it.code }
                val result = geminiService.detectAndTranslate(sourceText, targetLanguages)
                result.fold(
                    onSuccess = { (transcription, translationsMap) ->
                        detectedLanguage = transcription.lang
                        translations = translationsMap
                        setStatus("✅ 翻译完成")
                    },
                    onFailure = { e ->
                        error = "Translation failed: ${e.message}"
                        setStatus("❌ 翻译失败")
                    }
                )
            } catch (e: Exception) {
                error = "Error: ${e.message}"
                setStatus("❌ ${e.message}")
            } finally {
                isProcessing = false
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "🌐 语音翻译",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onNavigateToSettings) {
                Icon(
                    Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Source text card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "原文 / Source",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (sourceText.isNotEmpty()) {
                    Text(
                        text = sourceText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 24.sp
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
                        text = "点击下方麦克风开始录音...",
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
                    Text("📄 复制全部翻译", fontSize = 12.sp)
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
                            // TODO: Implement TTS via expect/actual platform implementation
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
                        scope.launch {
                            isRecording = false
                            isProcessing = true
                            error = null
                            setStatus("🔄 正在识别语音...")
                            
                            try {
                                val audioData = audioRecorder.stopRecording()
                                if (audioData != null) {
                                    val transcriptionResult = geminiService.transcribeAudio(audioData)
                                    transcriptionResult.fold(
                                        onSuccess = { transcription ->
                                            sourceText = transcription.text
                                            detectedLanguage = transcription.lang
                                            
                                            setStatus("🌍 正在翻译...")
                                            val targetLanguages = enabledLanguages.map { it.code }
                                            val translationResult = geminiService.translateText(transcription.text, targetLanguages)
                                            
                                            translationResult.fold(
                                                onSuccess = { translationsMap ->
                                                    translations = translationsMap
                                                    setStatus("✅ 翻译完成")
                                                },
                                                onFailure = { exception ->
                                                    error = "Translation failed: ${exception.message}"
                                                    setStatus("❌ 翻译失败")
                                                }
                                            )
                                        },
                                        onFailure = { exception ->
                                            error = "Transcription failed: ${exception.message}"
                                            setStatus("❌ 识别失败")
                                        }
                                    )
                                } else {
                                    error = "Failed to record audio"
                                    setStatus("❌ 录音失败")
                                }
                            } catch (e: Exception) {
                                error = "Recording error: ${e.message}"
                                setStatus("❌ ${e.message}")
                            } finally {
                                isProcessing = false
                            }
                        }
                    } else {
                        if (audioRecorder.hasPermission()) {
                            scope.launch {
                                if (audioRecorder.startRecording()) {
                                    isRecording = true
                                    error = null
                                    setStatus("🔄 正在识别语音...")
                                } else {
                                    error = "Failed to start recording"
                                    setStatus("❌ 无法开始录音")
                                }
                            }
                        } else {
                            error = "Microphone permission required"
                            setStatus("❌ 需要麦克风权限")
                        }
                    }
                },
                modifier = Modifier.size(72.dp),
                containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Text(
                    text = if (isRecording) "⏹" else "🎤",
                    fontSize = 28.sp
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
                Text(
                    text = "📋",
                    fontSize = 22.sp
                )
            }
        }
        
        Text(
            text = if (isRecording) "录音中...点击停止" else "🎤 录音　📋 粘贴翻译",
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
                        Text("📄", fontSize = 16.sp)
                    }
                    IconButton(
                        onClick = onTTS,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Text("🔊", fontSize = 16.sp)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 22.sp
            )
        }
    }
}
