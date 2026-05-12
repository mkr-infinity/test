# Solo Ledger — Android App

A premium, offline-first personal expense tracker with glassmorphism UI, 5 beautiful aesthetic themes, and full data privacy.

## Features

- **5 Aesthetic Themes**: Aurora, Ocean, Sunset, Forest, Midnight
- **Light & Dark Mode** toggle per theme
- **Add/Edit/Delete** income & expense entries
- **Custom amount, category, notes, and date** per entry
- **Custom currency** — choose from 10 presets or type your own symbol
- **Home screen** with hero balance card and recent entries
- **Search** entries instantly
- **History** grouped by date with category icons
- **Reports** with bar chart (All / 7 days / 3 months / Custom period)
- **Bin** — soft delete, restore, or permanently delete entries
- **Settings** — toggle title, category, notes, date visibility
- **Profile Editor** — name and avatar selection
- **Support Me** — links to 5 support platforms
- **100% Offline** — Room database, no backend required
- **No emojis** — uses vector icons throughout

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Database | Room (SQLite) |
| Preferences | DataStore |
| Architecture | MVVM + Clean Architecture |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |

## Project Structure

```
solo/
├── app/
│   ├── src/main/
│   │   ├── java/com/sololedger/
│   │   │   ├── MainActivity.kt          # Entry point
│   │   │   ├── SoloApp.kt               # Main app, all screens (2851 lines)
│   │   │   ├── data/
│   │   │   │   └── SoloData.kt          # Room DB, DAO, entities, preferences
│   │   │   └── ui/theme/
│   │   │       └── Theme.kt             # Colors, typography, themes
│   │   ├── res/
│   │   │   ├── drawable/                # Vector icons (no emoji)
│   │   │   ├── mipmap-*/                # App icons
│   │   │   └── values/                  # Strings, themes
│   │   └── AndroidManifest.xml
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── gradle/
│   └── wrapper/
├── .github/workflows/
│   └── build.yml                        # CI: debug + release APK build
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── local.properties                      # Android SDK path (auto-generated)
└── README.md
```

## Building

### Android Studio (Recommended)

1. **File → Open** → select the `solo` folder
2. Wait for Gradle sync to complete
3. **Run → Run 'app'** (Shift+F10) — builds and launches debug APK

### Command Line

```bash
cd solo

# Debug APK (no signing)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
  ./gradlew assembleDebug

# Release APK (requires signing config in signing.properties)
JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64 \
  ./gradlew assembleRelease
```

### GitHub Actions (Automatic)

Push to `main` or open a PR — automatically builds:
- Debug APK on every push
- Release APK (signed) when secrets are configured

**Required GitHub Secrets:**
| Secret | Description |
|---|---|
| `KEYSTORE_BASE64` | Base64-encoded `.jks` keystore file |
| `KEYSTORE_PASSWORD` | Keystore password |
| `KEY_ALIAS` | Key alias name |
| `KEY_PASSWORD` | Key password |

Generate a keystore locally:
```bash
keytool -genkey -v -keystore signing.jks \
  -alias your-alias -keyalg RSA -keysize 2048 -validity 10000 \
  -storetype JKS
```

Then base64-encode it:
```bash
base64 -w0 signing.jks  # copy output → GitHub Secret KEYSTORE_BASE64
```

For **local development**, create `app/signing.properties`:
```properties
storeFile=../signing.jks
storePassword=yourpassword
keyAlias=your-alias
keyPassword=yourpassword
debugKeyFile=../debug.jks
debugStorePassword=androiddebug
debugKeyAlias=androiddebugkey
debugKeyPassword=androiddebug
```

## APK Outputs

After building:
- **Debug**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release**: `app/build/outputs/apk/release/app-release.apk`

## Screenshots

The app features glassmorphism cards, gradient backgrounds per theme, animated bottom navigation, smooth transitions, and a premium fintech feel throughout.

## License

MIT License