# Auto Optimizer

A lightweight, open-source Android optimizer inspired by the feature set and UX patterns of the legacy Auto Optimizer app. This is an independent implementation; it does not include the original app's source code, proprietary assets, signing keys, or decompiled implementation.

## Current v0.1.0
- Lightweight custom Canvas dashboard
- RAM, storage, battery percentage and battery temperature
- Deep Clean entry point
- Accessibility-service foundation for Settings automation
- Force-stop node lookup using `com.android.settings:id/force_stop_button` with text fallback
- About page with mkr_infinity social links and a lightweight MK avatar
- No Compose, Firebase, analytics, ads, or always-on optimizer service

## Planned modules
File Manager, Process Manager, Connection Control, Auto Restart, Auto Terminate, Device Info, Memory Info, Battery Info, CPU Info, App Info, Auto-rotate, Rotation Control, Video Enhancer, Touch Block, Split Screen, YouTube automation, Screenshot, Clipboard, History/Reconnect, automation profiles, and optional root-only features.

## Accessibility

Enable **Auto Optimizer Accessibility** only if you want automated Settings actions. The service retrieves visible Settings UI nodes to locate permitted buttons; it does not upload screen contents.

## GitHub Actions

`.github/workflows/android-release.yml` builds, signs, verifies, and uploads the release APK. Pushing a `v*` tag also creates a GitHub Release with the APK attached.