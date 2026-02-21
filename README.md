# VoiceTranslator

AI-powered voice & text translator built with Kotlin Multiplatform + Compose.

## Features

- **Voice Translation** — Record speech, auto-detect language, translate to multiple languages simultaneously
- **Text Translation** — Paste from clipboard for instant translation
- **Text-to-Speech** — Listen to translations in native pronunciation
- **Multi-language UI** — App interface auto-adapts to device language (EN/ZH/TH/JA/KO/AR/FR/ES)
- **Dark/Light Mode** — Auto or manual theme switching
- **Activation System** — Secure API key encryption with activation code

## Platforms

| Platform | Format |
|----------|--------|
| Android | APK (signed) |
| iOS | Framework |
| Windows | MSI |
| macOS | DMG |
| Linux | DEB |
| Web | HTML |

## Tech Stack

- Kotlin 2.2.20
- Compose Multiplatform 1.10.1
- Ktor 3.1.1
- Gemini 2.5 Flash
- Material Design 3

## Build

```bash
# Android Release
./gradlew :composeApp:assembleRelease

# Desktop
./gradlew :composeApp:run

# macOS DMG
./gradlew :composeApp:packageDmg

# Windows MSI
gradlew.bat :composeApp:packageMsi

# Linux DEB
./gradlew :composeApp:packageDeb

# iOS Framework
./gradlew :composeApp:linkReleaseFrameworkIosArm64
```

## CI/CD

Tag `v*` triggers GitHub Actions — builds all platforms and creates a GitHub Release with artifacts.

## License

Private
