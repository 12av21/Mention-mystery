# Build, Release & Google Play Publishing Guide

## 1. Local Android Build (Debug & Release)

### Prerequisites
- **JDK 17**
- **Android SDK** (`compileSdk 36`, `minSdk 24`, `targetSdk 36`)
- **Gradle 9.3.1** (`gradle` CLI)

### Debug APK (Local Testing & Verification)
```bash
gradle :app:testDebugUnitTest
gradle :app:assembleDebug
```
Output: `app/build/outputs/apk/debug/app-debug.apk`

### Signed Release APK (Direct Distribution / GitHub Releases)
Create an upload keystore locally (never commit it to Git):
```bash
keytool -genkeypair -v -keystore my-upload-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias upload
```
Set signing environment variables and build:
```bash
export KEYSTORE_PATH="$(pwd)/my-upload-key.jks"
export STORE_PASSWORD="<your-store-password>"
export KEY_PASSWORD="<your-key-password>"
gradle :app:assembleRelease
```
Generate the SHA-256 checksum:
```bash
cp app/build/outputs/apk/release/app-release.apk MansionMystery-v1.0.0.apk
sha256sum MansionMystery-v1.0.0.apk > SHA256SUMS.txt
```

---

## 2. Google Play Store Distribution (Signed AAB)

Google Play requires an **Android App Bundle (`.aab`)** rather than an `.apk`:

1. **Build the signed AAB**:
   ```bash
   gradle :app:bundleRelease
   ```
   Output: `app/build/outputs/bundle/release/app-release.aab`
2. **Play App Signing**:
   - Enroll in **Play App Signing** inside Google Play Console.
   - Upload `app-release.aab` (signed with your `upload` key) to an Internal or Closed Testing track first.
3. **Store Listing & Policy Compliance**:
   - Package ID: `com.mansionmystery.game`
   - App Title: `Mansion Mystery` (15 characters, compliant with the 30-character Play title cap).
   - Orientation: `sensorLandscape`.
   - Permissions: Zero dangerous runtime permissions requested; offline local saves only.

---

## 3. GitHub Actions CI/CD Secrets Setup

In your GitHub repository (`12av21/mansion-mystery`), open **Settings → Secrets and variables → Actions** and add:
- `ANDROID_KEYSTORE_BASE64`: Output of `base64 -w 0 my-upload-key.jks`
- `STORE_PASSWORD`: Keystore password
- `KEY_PASSWORD`: Upload key password

Once configured, pushing a tag such as `v1.0.0` automatically runs tests, builds the signed release APK/AAB, computes `SHA256SUMS.txt`, and publishes the GitHub Release.
