package com.hhaigc.translator.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hhaigc.translator.model.Language
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
    val audioRecorder = remember { AudioRecorder() }
    val geminiService = remember { GeminiService() }
    val settingsStore = remember { SettingsStore() }
    
    var isRecording by remember { mutableStateOf(false) }
    var sourceText by remember { mutableStateOf("") }
    var translations by remember { mutableStateOf(mapOf<String, String>()) }
    var enabledLanguages by remember { mutableStateOf(emptyList<Language>()) }
    var isProcessing by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    
    // Collect enabled languages
    LaunchedEffect(Unit) {
        settingsStore.getEnabledLanguages().collect { languages ->
            enabledLanguages = languages.filter { it.isEnabled }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with settings button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "VoiceTranslator",
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Source text card
        if (sourceText.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Original",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = {
                                // TODO: Copy to clipboard
                            }
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Text(
                        text = sourceText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Translation cards grid
        if (translations.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(enabledLanguages.filter { translations.containsKey(it.code) }) { language ->
                    TranslationCard(
                        language = language,
                        translation = translations[language.code] ?: "",
                        onCopy = {
                            // TODO: Copy to clipboard
                        },
                        onTTS = {
                            // TODO: Text-to-Speech
                        }
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (isProcessing) "Processing..." else "Tap the record button to start",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
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
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Record button
        FloatingActionButton(
            onClick = {
                if (isRecording) {
                    scope.launch {
                        isRecording = false
                        isProcessing = true
                        error = null
                        
                        try {
                            val audioData = audioRecorder.stopRecording()
                            if (audioData != null) {
                                // Transcribe audio
                                val transcriptionResult = geminiService.transcribeAudio(audioData)
                                transcriptionResult.fold(
                                    onSuccess = { transcribedText ->
                                        sourceText = transcribedText
                                        
                                        // Translate to enabled languages
                                        val targetLanguages = enabledLanguages.map { it.code }
                                        val translationResult = geminiService.translateText(transcribedText, targetLanguages)
                                        
                                        translationResult.fold(
                                            onSuccess = { translationsMap ->
                                                translations = translationsMap
                                            },
                                            onFailure = { exception ->
                                                error = "Translation failed: ${exception.message}"
                                            }
                                        )
                                    },
                                    onFailure = { exception ->
                                        error = "Transcription failed: ${exception.message}"
                                    }
                                )
                            } else {
                                error = "Failed to record audio"
                            }
                        } catch (e: Exception) {
                            error = "Recording error: ${e.message}"
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
                            } else {
                                error = "Failed to start recording"
                            }
                        }
                    } else {
                        error = "Microphone permission required"
                    }
                }
            },
            modifier = Modifier
                .size(80.dp),
            containerColor = if (isRecording) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        ) {
            Icon(
                imageVector = if (isRecording) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = if (isRecording) "Stop Recording" else "Start Recording",
                modifier = Modifier.size(32.dp)
            )
        }
        
        Text(
            text = if (isRecording) "Recording..." else "Tap to record",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            modifier = Modifier.padding(top = 8.dp)
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
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = language.flag,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = language.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
                Row {
                    Icon(
                        Icons.Default.Share,
                        contentDescription = "Copy",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onCopy() }
                            .padding(end = 4.dp)
                    )
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = "TTS",
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onTTS() }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = translation,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 12.sp,
                maxLines = 3,
                modifier = Modifier.weight(1f)
            )
        }
    }
}