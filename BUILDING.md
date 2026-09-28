# Building Auto Optimizer

## GitHub Actions

The repository includes a GitHub Actions workflow that builds a release APK on pushes to `main`, `v*` tags, and manual workflow dispatches.

The workflow uses JDK 17, Gradle 8.10.2, builds the release APK, generates an ephemeral temporary release keystore, signs with Android `apksigner`, verifies it, and uploads the signed APK as an Actions artifact. A `v*` tag also creates a GitHub Release with the APK attached.

The temporary keystore is never committed and is destroyed with the runner. Each workflow run therefore has a different signing identity. This is suitable for testing/sideloading, not production updates.

## Get the APK

Open **Actions** → **Android Release APK**, run the workflow, and download the APK artifact.

For a GitHub Release:

```bash
git tag v0.1.0
git push origin v0.1.0
```

The APK will be attached automatically.

## Production signing

For Play Store or long-term updates, replace the ephemeral key with a permanent signing key stored in GitHub Secrets. Never commit a production keystore or passwords.