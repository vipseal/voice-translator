package com.hhaigc.translator

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import com.hhaigc.translator.service.AudioRecorder
import com.hhaigc.translator.store.SettingsStore

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        AudioRecorder.onPermissionResult(isGranted)
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize context-dependent services
        AudioRecorder.initWithContext(this)
        AudioRecorder.setPermissionRequester { 
            requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
        SettingsStore.initWithContext(this)
        
        setContent {
            App()
        }
    }
}
