<div align="center">
  <img src="assets/logo.png" alt="Auto Optimiser logo" width="144" height="144" />
  <h1>Auto Optimiser</h1>
  <p><strong>Understand your device. Choose the action. Verify the result.</strong></p>
  <p>A local-first Android utility for app management, supported optimisation, storage review, battery observations, and device information.</p>
</div>

<p align="center">
  <a href="https://github.com/mkr-infinity/test/actions/workflows/android.yml"><img src="https://github.com/mkr-infinity/test/actions/workflows/android.yml/badge.svg" alt="Android CI" /></a>
  <a href="https://developer.android.com/guide/topics/permissions/overview"><img src="https://img.shields.io/badge/Android%20permissions-explained-306B5E" alt="Android permissions explained" /></a>
</p>

> Auto Optimiser is deliberately honest about Android's boundaries. It does not promise that killing apps makes a phone faster, does not pretend to clear private caches, and does not manufacture optimisation percentages.

## What it does

### Review and optimise apps

- Reads the installed-app list exposed by Android and caches stable metadata and icons.
- Search by app name or package name.
- Filter by all, user, system, running, or protected apps.
- Sort by name, APK size, or update time.
- Open an app, open its Android App Info page, or start Android's normal uninstall flow.
- Protect Auto Optimiser, launchers, keyboards, System UI, enabled accessibility services, system applications, and apps you choose.
- Select one or several eligible apps and review the queue before starting.

### Supported stop automation

When you explicitly start an optimisation queue, Auto Optimiser uses Android's supported App Info workflow:

1. Open the target app's App Info page.
2. Confirm the target package and app identity using accessibility semantics.
3. Find the labelled Force stop action.
4. Handle Android's confirmation dialog when it is exposed.
5. Verify the stopped state and disabled control before reporting success.
6. Return to Auto Optimiser and continue with the queue.

The queue is bounded, cancellable, timeout-protected, protected-app aware, and limited to 20 apps per operation. A floating accessibility overlay remains available while Android Settings is open. Unsupported or localised OEM layouts are skipped instead of being guessed at.

Accessibility access is disabled while idle and is used only for a queue you start. It is not used to read messages, passwords, contacts, or unrelated app content.

### Deep clean and file review

Deep clean uses Android's Storage Access Framework. You choose a folder; Auto Optimiser does not scan private app directories or unselected storage.

- Large files using a configurable threshold.
- Files under known Downloads folder ancestry.
- Images, videos, and audio.
- APK and archive files.
- Duplicate candidates using size, staged sampling, and full SHA-256 verification.
- Readable folder-relative paths, file size, MIME/type information, and duplicate retention details.
- Explicit selection, review, and real deletion through the document provider.

Deletion is conservative. The current scan, metadata, write grant, provider delete capability, and—when applicable—duplicate content are checked again. Canonical duplicate originals cannot be selected for deletion. A provider can still change between verification and deletion; the app explains this limitation and never treats a requested deletion as success until Android confirms it.

### Battery, memory, and device information

- Battery percentage and charging state.
- Temperature and Android battery health flag when exposed.
- Battery Saver state.
- Instantaneous current and charge counter when exposed; these are not charger-speed or full-capacity estimates.
- Up to 48 local battery/device observations from manual refreshes and scheduled checks. Gaps are not filled in as if they were history.
- Memory available, used, and total values from `ActivityManager`.
- Storage volume available, used, and total values.
- Manufacturer, model, Android release/API level, security patch, ABI, and CPU-core information.
- One bounded sample of this app's own CPU time. Android does not provide reliable device-wide or other-app CPU usage to an ordinary app, so no such number is invented.

### Lightweight scheduled analysis

Daily or weekly scheduling uses WorkManager to record a small local device observation. Android controls the exact execution time and may defer work when the battery is low.

Scheduled work does **not** stop apps, delete files, scan the whole filesystem, intercept the Home button, or run a permanent service.

## Description coverage and Android limits

The supplied legacy product description mentions several behaviours that a normal modern third-party Android app cannot safely guarantee. Auto Optimiser maps those ideas to supported workflows instead of presenting fake buttons:

| Legacy concept | Auto Optimiser behaviour |
| --- | --- |
| File Manager | User-granted folder review, open, selection, and confirmed deletion. It is not a private-data file browser. |
| One-tap optimisation | Analyse first, then review and explicitly start the supported App Info stop queue. |
| Battery saver | Reports Android battery data and opens Android Battery Saver / battery-usage settings. It does not claim a 1/10–1/3 saving. |
| Deep clean / hidden cache removal | Real selected-folder cleanup only. Other apps' private caches and `/data/data` are not accessible to a normal app. |
| Memory information | Real `ActivityManager` snapshot with an explanation that free RAM is not a speed score. |
| Battery history | Bounded local observations with gaps shown honestly; no fabricated continuous charging history. |
| Battery health / capacity | Android health flag and charge-counter values when available; no invented estimated mAh capacity or degradation percentage. |
| CPU information | Bounded Auto Optimiser self-sample and device CPU metadata; no false global CPU usage. |
| Device information | Dedicated device information screen with values exposed by Android. |
| Game Booster | Not claimed as implemented. Auto Optimiser does not alter game processes, thermal policy, scheduler settings, or graphics drivers. |
| Home-button / screen-off / memory-threshold triggers | Not enabled. The app does not intercept navigation or run hidden polling. |
| Status-bar / permanent overlay metrics | Not enabled. The only overlay is the temporary cancellation control during a user-started accessibility queue. |
| Ultra memory release | Not implemented. Android owns process lifecycle and third-party apps cannot safely force a global memory release. |

This is intentional: an unavailable operation is explained rather than simulated.

## Privacy and permissions

- No account is required.
- Device data, settings, observations, and protected-package choices stay in local app storage.
- The app does not collect contacts, location, passwords, messages, notification contents, or private app files.
- Internet access is not required by the core app. External links open through the Android browser. The developer avatar is bundled locally in the About screen.
- Installed-app discovery uses Android package visibility because that is the feature's purpose.
- Accessibility access is optional and required only for the user-started multi-app stop workflow.
- File access is granted per folder through Android's picker and can be revoked by the user.

## Visual identity

The logo is an original vector mark inspired by the visual language of a compact upright rocket and open orbit: a blue/cyan body and orbit, a short amber flame, rounded geometry, and no copied artwork. The reference image supplied during design is not bundled or pasted into the app.

Source assets:

- [`assets/logo.svg`](assets/logo.svg) — full README artwork
- [`assets/logo-mark.svg`](assets/logo-mark.svg) — transparent mark
- [`assets/logo.png`](assets/logo.png) — README preview raster
- [`scripts/generate_brand.py`](scripts/generate_brand.py) — regenerates matching Android vectors and SVGs

The Android launcher, splash screen, monochrome resource, onboarding, Home header, About page, and README all use the same original identity.

## Build and CI

[`.github/workflows/android.yml`](.github/workflows/android.yml) builds **only a signed release APK**. It runs on pushes to `main`, or manually from the **Actions** tab. It does not create a GitHub Release, tag, release notes, or a new version number. The app's required version metadata stays in `gradle.properties`.

The workflow installs Java 17, Gradle 8.9, Android SDK 35 and Build Tools 35.0.0, generates a temporary signing identity on the runner, runs `:app:assembleRelease`, verifies the APK signature, and uploads only `app-release.apk`. Debug builds and unit-test tasks are not part of this APK-only workflow.

**Download:** Open **Actions → Build release APK → a successful run → Artifacts → Auto-Optimiser-release-apk**. Extract the downloaded archive to get `app-release.apk`. Artifacts are kept for 14 days.

**Signing:** No keystore binary is committed or uploaded. The temporary key is removed after each run, and its random password is masked in logs. Each run has a **different signing identity**, so its APK cannot update a previous run's installation in place. Uninstalling the old app erases its local settings and history; use a stable private signing key instead if preserving installed-app updates matters. The temporary certificate is valid for 10,000 days; this does not make the key a stable distribution identity.

**Verification status:** Source and workflow checks are not a successful Android build. The next GitHub Actions run must pass before compilation and APK signing can be confirmed. No local build or push is performed by the coding agent.

## Developer

**Mohammad Kaif Raja**

- GitHub: <https://github.com/mkr-infinity>
- Instagram: <https://www.instagram.com/mkr_infinity>
- Telegram: <https://t.me/mkr_infinity>
- Website: <https://mkr-infinity.github.io>
- Support: <https://buymeacoffee.com/mkr_infinity>

Auto Optimiser is built independently. If you find it useful, you can support its continued development.
