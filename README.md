# Solo Ledger

**A premium offline-first personal finance tracker for Android.**

Built with Jetpack Compose, Material 3, Room DB, and Kotlin Coroutines. No internet required — all data stays on your device.

![Build](https://github.com/mkr-infinity/test/actions/workflows/build.yml/badge.svg)

---

## Features

- **Home** — Quick balance overview with animated entry cards and category icons
- **Add Entry** — Floating action button opens a bottom sheet with amount, category, notes, and date picker
- **History** — Entries grouped by date with swipe-to-delete, restore, and permanent delete
- **Reports** — Bar chart analytics with 7-day / 3-month / all-time filters
- **Settings** — Toggle title, category, notes, and date visibility on entry cards
- **Profile** — 10 aesthetic themes (Midnight, Rose, Ocean, Forest, Dusk, Gold, Violet, Teal, Ember, Dark), currency picker (INR default + custom), dark mode toggle
- **Support** — Buy Me a Coffee, Patreon, and PayPal links in the profile screen

---

## Tech Stack

| Layer | Technology |
|---|---|
| UI | Jetpack Compose + Material 3 |
| Navigation | Navigation Compose |
| Database | Room (SQLite) |
| Async | Kotlin Coroutines + Flow |
| DI | Manual (singleton pattern) |
| Target SDK | 35 / Min SDK 26 |

---

## Screens

| Screen | Description |
|---|---|
| **Home** | Balance header + entry list |
| **Add Entry** | Bottom sheet — amount, category, notes, date |
| **History** | Date-grouped list, swipe actions, search |
| **Reports** | Animated bar chart with period filter |
| **Settings** | Card visibility toggles |
| **Profile** | Theme picker, currency picker, dark mode, support links |

---

## Build

```bash
./gradlew assembleDebug
```

Debug APK output:
```
app/build/outputs/apk/debug/app-debug.apk
```

---

## Project Structure

```
solo/
├── app/src/main/
│   ├── java/com/sololedger/
│   │   ├── MainActivity.kt          # Entry point
│   │   ├── SoloLedgerApp.kt       # Application class
│   │   ├── SoloApp.kt              # Main composable (5 screens)
│   │   ├── PrefStore.kt            # SharedPreferences wrapper
│   │   │   └── data/SoloData.kt   # Room entities, DAO, database, repository
│   │   └── ui/theme/Theme.kt      # Material 3 theme + aesthetics
│   └── res/
│       ├── drawable/               # Launcher icons
│       └── values/                # Colors, strings, themes XML
├── build.gradle.kts               # Root build config
├── settings.gradle.kts             # Plugin management
├── gradle.properties              # JVM args, caching
└── gradlew / gradlew.bat          # Gradle wrapper
```

---

## Architecture

- **SoloData.kt** — Room database with `LedgerEntry` entity, `LedgerDao`, `AppDatabase`, and `SoloRepository` (single source of truth)
- **PrefStore.kt** — DataStore-backed `UserPrefs` with LiveData-style observers for reactive UI updates
- **SoloApp.kt** — Single-file UI with `Stage` enum (Home, History, Reports, Settings, ProfileEdit) and `NavTab` bottom bar
- **Theme.kt** — 10 handcrafted `Aesthetic` themes (colors, gradient, blur, nav style) + `ComposeColorScheme` generator + `Theme.kt` Material 3 setup
