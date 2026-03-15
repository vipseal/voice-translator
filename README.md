<h1 align="center">🎙️ Voice Translator</h1>

<p align="center">
  AI-powered voice & text translator built with Kotlin Multiplatform + Compose Multiplatform.<br/>
  One codebase — runs on Android, iOS, Windows, macOS, Linux & Web.
</p>

<p align="center">
  <a href="https://github.com/vipseal/voice-translator/releases">
    <img src="https://img.shields.io/github/v/release/vipseal/voice-translator?style=flat-square" alt="Release"/>
  </a>
  <a href="https://github.com/vipseal/voice-translator/actions/workflows/test.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/vipseal/voice-translator/test.yml?style=flat-square&label=tests" alt="Tests"/>
  </a>
  <img src="https://img.shields.io/badge/kotlin-2.2.20-7F52FF?style=flat-square&logo=kotlin&logoColor=white" alt="Kotlin"/>
  <img src="https://img.shields.io/badge/compose-1.10.1-4285F4?style=flat-square&logo=jetpackcompose&logoColor=white" alt="Compose"/>
  <img src="https://img.shields.io/badge/gemini-2.5_flash-886FBF?style=flat-square&logo=google&logoColor=white" alt="Gemini"/>
  <img src="https://img.shields.io/github/license/vipseal/voice-translator?style=flat-square" alt="License"/>
</p>

---

## ✨ Features

- 🎙️ **Voice Translation** — Record speech, auto-detect language, translate to multiple languages in one shot
- 📋 **Text Translation** — Paste text from clipboard for instant translation
- 🔊 **Text-to-Speech** — Listen to translations with native pronunciation via Web Speech API / platform TTS
- 🌍 **15 Languages** — English, Chinese, Japanese, Korean, Thai, Arabic, French, Spanish, German, Russian, Vietnamese, Portuguese, Hindi, Indonesian, Turkish
- 🌐 **Multi-language UI** — Interface auto-adapts to device language (8 locales: EN, ZH, TH, JA, KO, AR, FR, ES)
- 🎨 **Dark / Light / Auto Theme** — Material Design 3 with custom pill-style theme switcher
- 🔑 **Secure Activation** — Encrypted API key injected at build time, decrypted client-side with activation code
- 🔄 **Reset Authentication** — Reset activation from Settings to re-enter credentials
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
│   ├── crypto/          # XOR encryption, SHA-256, activation logic
│   ├── i18n/            # 8-locale internationalization
│   ├── model/           # Data models, Language definitions
│   ├── screen/          # ActivationScreen, TranslatorScreen, SettingsScreen
│   ├── service/         # GeminiService, AudioRecorder, TTS, Clipboard
│   ├── store/           # Settings persistence (per-platform)
│   ├── theme/           # Material 3 theming
│   └── ui/              # Shared composables (FlagImage, etc.)
├── commonTest/          # Cross-platform unit tests (52 tests)
├── androidMain/         # Android-specific (AudioRecorder, TTS, Clipboard)
├── desktopMain/         # Desktop-specific (javax.sound, AWT clipboard)
├── desktopTest/         # Desktop-specific tests (8 tests)
├── iosMain/             # iOS-specific (AVAudioRecorder, AVSpeechSynthesizer)
└── wasmJsMain/          # Web-specific (MediaRecorder, Web Speech API, Web Audio)
```

**Key patterns:**
- `expect`/`actual` for platform-specific services (AudioRecorder, SoundService, ClipboardService, TTS, HapticFeedback)
- Single merged font (`app_font.ttf`, 5.7MB) for WASM to handle multi-script rendering
- Flag PNG images instead of emoji for cross-platform consistency
- Build-time secret injection — no API keys in source code

## 🔒 Security

API keys are **never stored in source code**. The encrypted key blob is injected at build time via the `ENCRYPTED_GEMINI_KEY` environment variable:

- **CI builds**: Read from GitHub Secrets
- **Local builds**: Set the env var before building
- **Client-side**: Decrypted with user's activation code (XOR + SHA-256)

```bash
# For local development
export ENCRYPTED_GEMINI_KEY="your-encrypted-blob"
./gradlew :composeApp:run
```

## 🧪 Testing

60 tests across 6 test suites covering crypto, models, services, i18n, and persistence:

```bash
# Run all tests
./gradlew desktopTest

# Tests run automatically on every push/PR via GitHub Actions
```

| Suite | Tests | Coverage |
|-------|-------|----------|
| CryptoUtilsTest | 10 | SHA-256 vectors, encryption/decryption |
| LanguageTest | 9 | Model completeness, serialization |
| ResultModelsTest | 6 | Translation/Transcription serialization |
| GeminiServiceTest | 17 | Mock HTTP, JSON parsing, all API methods |
| AppStringsTest | 10 | All 8 locales × 53 string fields |
| SettingsStoreTest | 8 | Persistence, theme, activation, languages |

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

- **Tests** run on every push to `main` and every PR
- **Release builds** trigger on `v*` tags — builds all platforms and creates a GitHub Release

```bash
# Trigger a release
git tag v0.1.0
git push origin v0.1.0

# Or selectively build specific platforms
gh workflow run release.yml -f targets=web
gh workflow run release.yml -f targets=android,web
```

**Supported targets:** `android`, `web`, `linux`, `macos`, `windows`, `ios`, `all`

## 🌐 Live Demo

**Web version:** [https://moltbot.happylife.ink/translator/](https://moltbot.happylife.ink/translator/)

## 📄 License

MIT License — see [LICENSE](LICENSE) for details.
