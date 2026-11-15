# Mansion Mystery: The Last Secret

![Platform](https://img.shields.io/badge/Platform-Android%20(7.0%2B)-3DDC84?logo=android&logoColor=white)
![Language](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF?logo=kotlin&logoColor=white)
![UI](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4)
![Release](https://img.shields.io/badge/Release-v1.0.0-D4AF37)
![License](https://img.shields.io/badge/License-MIT-blue)

![Ravenshollow Manor](app/src/main/res/drawable/img_manor_hero.jpg)

A Gothic Victorian 2D top-down detective stealth and puzzle adventure set inside **Ravenshollow Manor**. Infiltrate locked wings, evade patrolling guards with wall-occluded lantern cones, decode five room-specific locks, link forensic clues on your Evidence Board, and unseal the Grand Secret Room to recover the stolen Star-Compass.

> **Implementation Note:** This repository contains a **native Android implementation** written in **Kotlin + Jetpack Compose 2D Canvas** (Gradle Kotlin DSL), rather than a Unity C# project. All gameplay mechanics, stealth AI, puzzles, procedural audio, and JSON saves are built directly on Android APIs with zero WebView wrappers.

---

## Key Features

- **Nine-Zone Top-Down Mansion**: Explore the **Main Entrance**, **Grand Foyer**, **Mansion Corridors**, **Library**, **Study**, **Conservatory**, **Kitchen**, **Portrait Gallery**, and **Grand Secret Room** with smooth camera tracking, flickering sconces, and 2D circle-vs-tile collision.
- **Five Unique Room Puzzles**:
  - **Library (`The Spine Cipher`)**: Arrange folio volumes in sequence using the engraved verse.
  - **Study (`The Alibi Contradiction`)**: Cross-check statements and desk notes to identify the forger and exact entry time.
  - **Conservatory (`Lunar Bloom Matrix`)**: Toggle matching botanical tiles on a 3×3 moonlit grid.
  - **Kitchen (`The Midnight Nightcap`)**: Reconstruct the 4-step apothecary tea preparation sequence.
  - **Gallery (`Ancestral Crest Seal`)**: Pair three ancestral portraits with their heraldic crests in chronological order.
- **Guard AI & Raycast Flashlight Stealth**:
  - Two patrolling guards (`Warden Barnaby` and `Constable Vance`) powered by a 6-state finite state machine (`IDLE`, `PATROL`, `INVESTIGATE`, `SUSPICIOUS`, `CHASE`, `RETURN_TO_PATROL`).
  - Dynamic raycast flashlight cones occluded by solid walls and locked doors.
  - Three vigilance modes (**Casual Sleuth**, **Inspector**, **Master Thief**) and safe checkpoint recovery that preserves clues and unlocked rooms.
- **Detective Casebook & Evidence Board**:
  - Collect 5 unique clues with discovery timestamps, forensic descriptions, and generated portraits.
  - Weave investigative links between clue pairs on the interactive **Evidence Board** before entering the Grand Secret Room.
- **Landscape Mobile Controls & Accessibility**:
  - Locked `sensorLandscape` orientation with `enableEdgeToEdge()` and `WindowInsets.safeDrawing` padding.
  - Touch virtual analog joystick (configurable sensitivity), contextual **Interact** / **Inspect** buttons, and hardware keyboard WASD/Arrow + `E`/`Space` support.
- **Synthesized Audio & Atomic Local Saves**:
  - Procedural `AudioTrack` Victorian drone, wind ambience, footsteps, lock/unlock chimes, and alert stings with Master, Ambience, Effects, and Mute controls.
  - Versioned Moshi JSON persistence (`mansion_save_v1.json`) with atomic temp-file writes and automatic `.bak` backup recovery.

---

## Gameplay Instructions

1. Launch **Mansion Mystery** and select **Guard Vigilance** difficulty on the Main Menu, then tap **New Investigation** (or **Continue Case** when a save exists).
2. Use the left-hand **virtual joystick** (or `WASD` / Arrow keys) to move through the Foyer and Corridors while staying outside guard lantern cones.
3. Approach a locked wing door and tap **Interact** to open its puzzle lock. Reveal hints with **Consult Notes** if needed.
4. Step inside the unlocked room, locate the glowing clue, and tap **Interact** or **Inspect** to log it into your **Evidence** casebook.
5. Open **Evidence → Evidence Board** to connect related clues and check your progress on the **Blueprint** map.
6. Once all **5 room locks** are solved and all **5 clues** are collected, unlock the **Grand Secret Room** in the north corridor and solve the final deduction to recover the Star-Compass.

---

## Android Requirements & Configuration

| Setting | Value |
| --- | --- |
| **Application Name** | `Mansion Mystery` |
| **Package ID** | `com.mansionmystery.game` |
| **Version** | `1.0.0` (`versionCode = 1`) |
| **Minimum Android** | Android 7.0 (`minSdk 24`) |
| **Target / Compile SDK** | Android 16 (`targetSdk 36`, `compileSdk 36.1`) |
| **Orientation** | Locked Landscape (`sensorLandscape`) |
| **Permissions** | None required (offline local persistence & audio) |

---

## Download & Installation

1. Visit the [**Releases**](https://github.com/12av21/mansion-mystery/releases) page on `12av21/mansion-mystery`.
2. Download `MansionMystery-v1.0.0.apk` and `SHA256SUMS.txt` from **v1.0.0 – Initial Android Release**.
3. Verify the APK integrity before installing:
   ```bash
   sha256sum -c SHA256SUMS.txt
   ```
4. Transfer the APK to your Android device (Android 7.0+), allow installation from your file manager/browser when prompted, install, and launch **Mansion Mystery**.

---

## Tech Stack & Project Structure

- **Language & UI**: Kotlin `2.2.10`, Jetpack Compose (BOM `2024.09.00`), Material 3, local bundled `Cinzel` & `Plus Jakarta Sans` fonts
- **Serialization & Build**: Moshi (`moshi-kotlin` + KSP codegen), Android Gradle Plugin `9.1.1`, Gradle `9.3.1`
- **Testing**: JUnit 4 + Robolectric `4.16.1`

```text
mansion-mystery/
├── .github/
│   ├── ISSUE_TEMPLATE/
│   ├── workflows/android-build.yml
│   └── pull_request_template.md
├── Documentation/
│   ├── BUILD_AND_PLAY_STORE_GUIDE.md
│   └── RELEASE_NOTES_v1.0.0.md
├── app/
│   ├── build.gradle.kts
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/
│       │   │   ├── audio/        # Synthesized AudioTrack ambience & SFX
│       │   │   ├── data/         # Atomic JSON + .bak SaveRepository
│       │   │   ├── model/        # World, puzzle, guard, and save models
│       │   │   ├── mystery/      # 5 room puzzles, clues, evidence links, validators
│       │   │   ├── ui/           # Canvas renderer, HUD, screens, modals, theme
│       │   │   ├── viewmodel/    # Frame loop, input, stealth & progression state
│       │   │   ├── world/        # 40x26 tile map, collision, raycast & guard FSM
│       │   │   └── MainActivity.kt
│       │   └── res/              # Adaptive icon, fonts, generated artwork, strings
│       └── test/                 # Robolectric unit & regression tests
├── gradle/libs.versions.toml
├── CONTRIBUTING.md
├── LICENSE
├── SECURITY.md
└── settings.gradle.kts
```

---

## Build & Verification Instructions

```bash
# Run unit & Robolectric tests
gradle :app:testDebugUnitTest

# Build Debug APK for local verification
gradle :app:assembleDebug

# Build Signed Release APK & Play Store AAB (requires upload keystore env vars)
export KEYSTORE_PATH=/path/to/my-upload-key.jks
export STORE_PASSWORD=***
export KEY_PASSWORD=***
gradle :app:assembleRelease :app:bundleRelease
```

See [Documentation/BUILD_AND_PLAY_STORE_GUIDE.md](Documentation/BUILD_AND_PLAY_STORE_GUIDE.md) for full CI secret setup (`ANDROID_KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_PASSWORD`) and Google Play Store `.aab` publishing instructions.

---

## Known Limitations

- **Native Kotlin/Compose Engine**: Built natively in Kotlin and Jetpack Compose rather than Unity 2D / C#.
- **Pure JVM/Kotlin Bytecode**: Because the app uses no C/C++ NDK shared libraries, the compiled APK runs natively on `arm64-v8a` and other Android ABIs without architecture-specific `.so` binaries.
- **Visual Assets & Screenshots**: Main menu hero, portrait, and heirloom illustrations are bundled in `app/src/main/res/drawable/`; in-game runtime screenshots should be captured on a physical device or Android Emulator before attaching to the GitHub Release description.
- **Release Signing**: Automated signed release builds on GitHub Actions require configuring your private upload keystore in repository Actions secrets (`ANDROID_KEYSTORE_BASE64`, `STORE_PASSWORD`, `KEY_PASSWORD`).

---

## Credits & License

- **Developer**: [12av21](https://github.com/12av21)
- **License**: Released under the [MIT License](LICENSE).
