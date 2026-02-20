package com.hhaigc.translator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.hhaigc.translator.service.AudioRecorder
import com.hhaigc.translator.store.SettingsStore

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize context-dependent services
        AudioRecorder.initWithContext(this)
        SettingsStore.initWithContext(this)
        
        setContent {
            App()
        }
    }
}