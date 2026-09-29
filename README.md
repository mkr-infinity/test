# Auto Optimiser

Auto Optimiser is a local-first Android utility for inspecting device state and choosing supported actions. It deliberately treats Android's boundaries as part of the product: no private app data is read, RAM is not "boosted", and a result is only recorded after the system UI confirms the action.

## Implemented workflows

- On-demand device analysis for memory, storage, and battery values exposed by Android.
- Installed application inventory with cached icons, user/system protection, search, filters, sorting, details, launch, App Info, and Android-mediated uninstall.
- User-protected apps plus automatic protection for Auto Optimiser, launchers, enabled accessibility-service packages, the active keyboard, System UI, and system applications.
- Accessibility-backed, user-started multi-app stop queue. It opens each package's Android App Info page, matches accessibility semantics for Force stop/Stop, confirms when Android asks, verifies the control state, returns, and records success/skipped/failed outcomes. Every step has bounded timeouts and cancellation.
- Storage Access Framework folder scanning for large files, downloads, media, APKs, archives, staged duplicate detection, and user-confirmed deletion. Scans do not cross into other apps' private directories.
- Battery snapshot reporting without misleading capacity or boost claims.
- Optional WorkManager periodic *lightweight analysis only*. It never silently stops apps or runs a permanent service.
- System/light/dark theme, onboarding permission explanations, real build metadata, developer links, GitHub avatar with an offline identity-derived fallback, and Buy Me a Coffee link.

## Android limitations shown in the product

A normal third-party app cannot freely clear `/data/data/<other-app>` or every app cache. Auto Optimiser therefore routes users to Android App Info where appropriate and does not report a fake cache-clearing result. Per-app battery history and exact installed size are also returned only where the target Android release exposes them.

## Build

The repository includes a GitHub Actions workflow at `.github/workflows/android.yml`. It uses Java 17 and Gradle 8.9, generates a temporary one-day PKCS12 keystore on the runner, and uploads a signed release APK as an artifact. The keystore is intentionally not committed; each workflow run has a new signing identity, so a real distribution key should replace the temporary CI configuration before publishing updates to an existing package.

No account or cloud service is required by the app. Core analysis and automation work offline; only the optional GitHub avatar request and external developer links use the network.
