# Mansion Mystery: The Last Secret — v1.0.0

**Release Title:** `v1.0.0 – Initial Android Release`  
**Package ID:** `com.mansionmystery.game`  
**Target Platform:** Android 7.0+ (`minSdk 24`, `targetSdk 36`, Landscape)

## Highlights

- **Nine-Zone Top-Down Mansion Exploration**: Explore the Main Entrance, Grand Foyer, Corridors, Library, Study, Conservatory, Kitchen, Portrait Gallery, and Grand Secret Room with real-time 2D collision and smooth camera tracking.
- **Five Unique Room Puzzles**: Spine Cipher (Library), Alibi Deduction (Study), Lunar Bloom Matrix (Conservatory), Apothecary Nightcap Sequence (Kitchen), and Ancestral Crest Matching (Gallery), complete with progressive hints.
- **Guard AI & Raycast Flashlight Stealth**: Two patrolling guards (`Warden Barnaby` and `Constable Vance`) with a 6-state FSM, wall-occluded raycast vision cones, three difficulty modes, and safe checkpoint recovery.
- **Detective Casebook & Evidence Board**: Collect 5 unique clues with discovery timestamps and link them on the Evidence Board before solving the final deduction in the Grand Secret Room.
- **Synthesized Victorian Audio & Atomic JSON Saves**: Procedural `AudioTrack` ambience/SFX with Master, Ambience, Effects, and Mute controls, plus atomic JSON persistence with `.bak` recovery.

## Installation & Verification

1. Download `MansionMystery-v1.0.0.apk` and `SHA256SUMS.txt` from the release assets.
2. Verify the checksum before sideloading:
   ```bash
   sha256sum -c SHA256SUMS.txt
   ```
3. Enable **Install unknown apps** if sideloading outside Google Play, install the APK, and launch **Mansion Mystery** in Landscape.
