<h1 align="center">🎙️ Voice Translator</h1>

<p align="center">
  AI-powered voice & text translator built with Kotlin Multiplatform + Compose Multiplatform.<br/>
  One codebase — runs on Android, iOS, Windows, macOS, Linux & Web.
</p>

<p align="center">
  <a href="https://github.com/vipseal/voice-translator/releases">
    <img src="https://img.shields.io/github/v/release/vipseal/voice-translator?style=flat-square" alt="Release"/>
  </a>
  <img src="https://img.shields.io/badge/kotlin-2.2.20-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/compose-1.10.1-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose"/>
  <img src="https://img.shields.io/badge/gemini-2.5_flash-886FBF?style=flat-square&logo=google&logoColor=white" alt="Gemini"/>
</p>

---

## ✨ Features

- 🎙️ **Voice Translation** — Record speech, auto-detect language, translate to multiple languages in one shot
- 📋 **Text Translation** — Paste text from clipboard for instant translation
- 🔊 **Text-to-Speech** — Listen to translations with native pronunciation via Web Speech API / platform TTS
- 🌍 **15 Languages** — English, Chinese, Japanese, Korean, Thai, Arabic, French, Spanish, German, Russian, Vietnamese, Portuguese, Hindi, Indonesian, Turkish
- 🌐 **Multi-language UI** — Interface auto-adapts to device language (8 locales)
- 🎨 **Dark / Light / Auto Theme** — Material Design 3 with custom pill-style theme switcher
- 🔑 **Activation System** — Secure AES-encrypted API key with activation code
- 🏁 **Country Flags** — Native flag images (no emoji dependency)
- 🔤 **Full Unicode Support** — Merged multi-script font covering Latin, CJK, Korean, Thai, Arabic, Hindi, Cyrillic & more

## 📱 Platforms

| Platform | Format | Status |
|----------|--------|--------|
| 🤖 Android | APK (signed) | ✅ |
| 🍎 iOS | .app / IPA | ✅ |
| 🪟 Windows | MSI installer | ✅ |
| 🍏 macOS | DMG | ✅ |
| 🐧 Linux | DEB package | ✅ |
| 🌐 Web | WASM (Kotlin/Wasm) | ✅ |

## 🖼️ Screenshots

<p align="center">
  <em>Dark mode — voice recording with multi-language results</em>
</p>

## 🛠️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Kotlin 2.2.20 |
| UI Framework | Compose Multiplatform 1.10.1 |
| Networking | Ktor 3.1.1 |
| AI Model | Google Gemini 2.5 Flash |
| Design | Material Design 3 |
| Build | Gradle 8.12 |
| CI/CD | GitHub Actions |
| Web Target | Kotlin/Wasm (wasmJs) |

## 🏗️ Architecture

```
composeApp/
├── commonMain/          # Shared code (95%+)
│   ├── model/           # Data models, Language definitions
│   ├── screen/          # TranslatorScreen, SettingsScreen
│   ├── service/         # GeminiService, AudioRecorder, TTS, Clipboard
│   ├── theme/           # Material 3 theming
│   └── ui/              # Shared composables (FlagImage, etc.)
├── androidMain/         # Android-specific (AudioRecorder, TTS, Clipboard)
├── iosMain/             # iOS-specific (AVAudioRecorder, AVSpeechSynthesizer)
├── desktopMain/         # Desktop-specific (javax.sound, AWT clipboard)
└── wasmJsMain/          # Web-specific (MediaRecorder, Web Speech API, Web Audio)
```

**Key patterns:**
- `expect`/`actual` for platform-specific services (AudioRecorder, SoundService, ClipboardService, TTS, HapticFeedback)
- Single merged font (`app_font.ttf`, 5.7MB) for WASM to handle multi-script rendering
- Flag PNG images instead of emoji for cross-platform consistency

## 🚀 Build & Run

### Prerequisites
- JDK 17+
- Android SDK (for Android builds)
- Xcode (for iOS builds, macOS only)

### Commands

```bash
# Run desktop app
./gradlew :composeApp:run

# Android APK (release, signed)
./gradlew :composeApp:assembleRelease

# macOS DMG
./gradlew :composeApp:packageDmg

# Windows MSI
gradlew.bat :composeApp:packageMsi

# Linux DEB
./gradlew :composeApp:packageDeb

# Web (WASM) distribution
./gradlew :composeApp:wasmJsBrowserDistribution
# Output: composeApp/build/dist/wasmJs/productionExecutable/

# iOS framework
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```

## 🔄 CI/CD

Push a `v*` tag to trigger GitHub Actions, which builds all platforms and creates a GitHub Release with artifacts.

```bash
# Trigger a release
git tag v0.0.5
git push origin v0.0.5

# Or selectively build specific platforms
gh workflow run release.yml -f targets=web
gh workflow run release.yml -f targets=android,web
```

**Supported targets:** `android`, `web`, `linux`, `macos`, `windows`, `ios`, `all`

## 🌐 Live Demo

**Web version:** [https://moltbot.happylife.ink/translator/](https://moltbot.happylife.ink/translator/)

## 📄 License

Private — All rights reserved.

