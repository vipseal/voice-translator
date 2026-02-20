# 🌐 VoiceTranslator (语音翻译)

A Kotlin Multiplatform voice translation app powered by Gemini AI.

## Features
- 🎤 Voice recording with real-time transcription
- 🌍 Translate to 15+ languages simultaneously
- 📋 Clipboard text translation
- ⚙️ Customizable language settings
- 🔊 Text-to-speech playback

## Platforms
- 📱 Android (Compose)
- 🍎 iOS (Compose/SwiftUI)
- 🖥️ Desktop (Compose - Windows/macOS/Linux)
- 🌐 Web (WASM)

## Tech Stack
- Kotlin 2.1.0
- Compose Multiplatform 1.7.3
- Ktor Client
- Gemini 2.5 Flash API
- kotlinx.serialization

## Build

```bash
# Android APK
./gradlew :composeApp:assembleDebug

# Desktop
./gradlew :composeApp:run

# Web
./gradlew :composeApp:wasmJsBrowserDistribution
```

## License
Private
