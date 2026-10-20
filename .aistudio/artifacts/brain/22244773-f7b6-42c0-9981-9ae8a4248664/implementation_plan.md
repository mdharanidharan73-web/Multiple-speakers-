# DualSync Audio — Multi Bluetooth Speaker Player (Revised)

DualSync Audio is an Android local music player and Bluetooth manager built with Jetpack Compose, Media3 ExoPlayer, Room, DataStore, and modern Android Bluetooth and audio routing APIs. It is engineered with strict technical honesty: it allows simultaneous playback on two Bluetooth speakers (e.g., boAt and Sony) **only** when genuine dual audio output is verified through the Android system routing layer (OEM Dual Audio / LE Audio Auracast broadcast). Otherwise, it diagnoses the hardware/software constraints and offers practical physical alternatives.

---

## User Review & Critical Decisions

> [!IMPORTANT]
> The following revisions and specifications have been incorporated directly from user feedback:

- **Sealed Error Model & Recovery Actions (`UiError`)**: Every potential runtime error (Bluetooth disabled, permissions denied/permanently denied, speaker disconnected/out of range, unsupported dual-hardware, missing audio files, connection failures, route loss, and audio focus interruptions) is modeled as a typed sealed class with an executable user recovery action.
- **Home Screen Actions**: The Home screen explicitly features all four designated controls:
  1. `[Scan Bluetooth Devices]`
  2. `[Check Dual Audio Support]`
  3. `[Open Audio Output Settings]`
  4. `[Play Music]`
- **Real Battery State Reporting**: Battery level is queried solely through public system APIs and broadcasts (`BluetoothDevice.ACTION_BATTERY_LEVEL_CHANGED` / battery intent extras). If unexposed by the device, it displays "Not available". No values are ever fabricated.
- **Version-Guarded Manifest Permissions**:
  - `POST_NOTIFICATIONS` (Android 13+)
  - `FOREGROUND_SERVICE` and `FOREGROUND_SERVICE_MEDIA_PLAYBACK`
  - `READ_EXTERNAL_STORAGE` (`maxSdkVersion="32"`)
  - `READ_MEDIA_AUDIO` (Android 13+)
  - `BLUETOOTH_SCAN` (`usesPermissionFlags="neverForLocation"`, Android 12+) and `BLUETOOTH_CONNECT` (Android 12+)
  - `BLUETOOTH` and `BLUETOOTH_ADMIN` (`maxSdkVersion="30"`)
  - `ACCESS_FINE_LOCATION` and `ACCESS_COARSE_LOCATION` (`maxSdkVersion="30"`)
- **In-Code Synthesized Sample Tracks**: Built-in audio tracks (multi-channel synth wave, bass groove, rhythm beat) and diagnostic test tones (440Hz A4 sine, 1kHz calibration, stereo left/right sweeps) are synthesized mathematically in Kotlin code into in-memory/cache WAV audio streams—no downloaded or fake media files.
- **API-Guarded BLE Audio Routing**: All `TYPE_BLE_HEADSET`, `TYPE_BLE_SPEAKER`, and `TYPE_BLE_BROADCAST` constants are strictly guarded by `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` / `TIRAMISU`, and Auracast/LE Audio features are labeled as "device-dependent (requires OEM hardware and firmware support)".
- **Dependency Injection**: Unified Dependency Injection using Hilt / clean App Container architecture across `di/`, `data/`, `bluetooth/`, `audio/`, `player/`, and `ui/`.
- **CI/CD, Physical Test Plan & Documentation**: Delivery includes a comprehensive `README.md`, a `.github/workflows/android.yml` configuration building a debug APK and uploading artifacts, and the full physical-phone testing protocol.

---

## 1. Overview & Core Concept

- **Core Mission**: Allow users to play music on two distinct Bluetooth speakers when supported by the device, while explaining limitations transparently when standard stock Android A2DP restricts output to a single sink.
- **Three-State Distinction**: Clearly separates:
  1. **Paired**: Bonded in Android Bluetooth database.
  2. **Connected**: Active Bluetooth profile ACL/A2DP connection.
  3. **Active Audio Output**: Currently recognized as an active sink by `AudioManager`.
- **Primary Value**: No false promises, no placebo sliders, but a robust music player with deep Bluetooth diagnostic intelligence.

---

## 2. User Experience & Visual Design

### Screen Structure & Navigation
1. **Home Screen**:
   - Title: `DUALSYNC AUDIO` | Tagline: `"One Song. Multiple Speakers."`
   - Dual Speaker Cards: Slot 1 and Slot 2 with Device Name, Brand chip, Battery status ("Not available" if unexposed), and 3-state badge (`Paired`, `Connected`, `Active Output`, `Disconnected`).
   - Four Core Action Buttons:
     - `[Scan Bluetooth Devices]`
     - `[Check Dual Audio Support]`
     - `[Open Audio Output Settings]`
     - `[Play Music]`
   - Mini Player Bar with live multi-bar PCM visualizer.
2. **Library Screen**:
   - MediaStore local audio track list with sorting (Title, Artist, Duration).
   - In-code synthesized sample tracks (Synthwave Grooves & Test Chords).
   - Playlist manager powered by Room (Create, Rename, Delete, Add/Remove tracks).
3. **Now Playing Sheet / Screen**:
   - Track details, album artwork generator, seek bar, play/pause/prev/next, repeat, shuffle, volume slider.
   - **Dynamic Multi-Bar Equalizer**: Real PCM amplitude stream measured directly via custom Media3 `AudioProcessor`.
4. **Sync & Latency Diagnostic Screen**:
   - Live list of active audio output routes from `AudioDeviceCallback`.
   - Codec latency breakdown (SBC: ~150-250ms, AAC: ~100-200ms, aptX: ~70-120ms, aptX-LL: ~30-40ms, LDAC: ~100-200ms, LC3: ~20-30ms).
   - Built-in sound test triggers: Left channel tone, Right channel tone, Dual-frequency sync chirp.
5. **Compatibility Screen (Alternative Playback Modes)**:
   - **Mode A (Native Dual Audio)**: Samsung Dual Audio / Xiaomi / OnePlus guide + live hardware audit.
   - **Mode B (External Dual Bluetooth Transmitter)**: Complete physical setup guide with 3.5mm/USB-C transmitter and latency advice.
   - **Mode C (Wi-Fi Synchronized Playback)**: Architectural specification for dual-device network synchronization with NTP timestamping and jitter buffers.
6. **Settings & Troubleshooting Screen**:
   - Theme toggle (Dark / Light).
   - Bluetooth troubleshooting wizard: Reset adapter, unpair/re-pair guide, clear audio cache, distance & interference checks.
   - Permission status audit.

### Visual Styling & Theme
- **Dark Theme Accent**: Obsidian `#0B0F19`, Surface `#131B2E`, Electric Blue `#2F6BFF`, Neon Violet `#8B5CF6`, Success Emerald `#10B981`, Warning Amber `#F59E0B`.
- **M3 Guidelines**: Edge-to-edge layout (`enableEdgeToEdge`), safe inset management (`WindowInsets.safeDrawing`), minimum 48dp touch targets, responsive card layouts (`widthIn(max = 600.dp)`).

---

## 3. Error Handling Architecture (`UiError` Model)

Every failure state maps to a typed `UiError` with a non-empty human message, technical explanation, and interactive recovery action:

```kotlin
sealed class UiError(val title: String, val message: String, val actionLabel: String) {
    object BluetoothDisabled : UiError("Bluetooth is Off", "Enable Bluetooth to scan and connect speakers.", "Turn On")
    object PermissionsDenied : UiError("Permissions Required", "Bluetooth scan/connect permissions are needed.", "Grant")
    object PermissionsPermanentlyDenied : UiError("Permission Blocked", "Permissions are permanently denied. Open app settings to grant them.", "Open Settings")
    data class SpeakerDisconnected(val name: String) : UiError("Speaker Disconnected", "$name disconnected unexpectedly.", "Reconnect")
    data class SpeakerOutOfRange(val name: String) : UiError("Signal Lost", "$name is out of range or powered off.", "Retry Scan")
    object UnsupportedDualHardware : UiError("Single Audio Stream Active", "Android currently routes audio to only one speaker. Check OEM Dual Audio or Auracast settings.", "View Guide")
    data class MusicFileNotFound(val uri: String) : UiError("File Missing", "The selected audio file is no longer accessible on storage.", "Refresh Library")
    data class ConnectionFailed(val reason: String) : UiError("Connection Error", "Could not establish connection: $reason", "Troubleshoot")
    object RouteLost : UiError("Audio Route Lost", "Bluetooth output route was disconnected mid-playback.", "Switch to Speaker")
    object AudioFocusLoss : UiError("Playback Paused", "Another application or phone call requested audio focus.", "Resume")
}
```

---

## 4. Technical Architecture & Component Mapping

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Jetpack Compose UI Layer                        │
│   HomeScreen  │  LibraryScreen  │  NowPlaying  │  Sync  │ Settings     │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Collects StateFlows / Dispatches Intent
┌───────────────────────────────────▼────────────────────────────────────┐
│                              ViewModels                                │
│   HomeViewModel  │  PlayerViewModel  │  LibraryViewModel  │ SyncVM    │
└───────────────┬───────────────────────────────┬────────────────────────┘
                │ Inject / Observe              │ MediaController Token
┌───────────────▼──────────────┐ ┌──────────────▼────────────────────────┐
│ Bluetooth & Audio Subsystem  │ │ Media3 Playback Service               │
│ - BluetoothRepositoryImpl    │ │ - PlaybackService (MediaSessionService│
│ - AudioRouteMonitorImpl      │ │ - ExoPlayer with Custom AudioProcessor│
│ - DualOutputCheckerImpl      │ │ - Real PCM Amplitude Buffer Reader    │
│ - BrandDetector              │ │ - AudioFocus & BecomingNoisy Receiver │
│ - ToneGeneratorUtil          │ │ - In-Code Synthesized Track Generator │
└───────────────┬──────────────┘ └──────────────┬────────────────────────┘
                │ System APIs                   │ Storage
┌───────────────▼──────────────┐ ┌──────────────▼────────────────────────┐
│ Android System Layer         │ │ Room & Preferences DataStore          │
│ - BluetoothAdapter / Broadcast│ │ - AppDatabase (Playlist & Item DAOs)  │
│ - AudioManager / Callback    │ │ - DataStore (Slots, Theme, Tutorial)  │
│ - MediaStore Audio Provider  │ │ - Hilt / AppContainer DI Modules      │
└──────────────────────────────┘ └───────────────────────────────────────┘
```

---

## 5. Deliverables & Verification Plan

1. **Gradle Build Verification**:
   - Update `app/build.gradle.kts` and `gradle/libs.versions.toml` with Media3 (`1.5.1`), Room (`2.7.0`), DataStore (`1.1.7`), Navigation Compose, and compile using `compile_applet`.
2. **Robolectric Unit Tests**:
   - `BrandDetectorTest`: Validates detection for boAt, Sony, JBL, Bose, Marshall, Samsung, Soundcore, etc.
   - `DualOutputCheckerTest`: Validates output route counting, BLE audio detection with API-level gating, and diagnostic error generation.
3. **Documentation & CI/CD**:
   - `README.md` with complete architecture notes, hardware limits explanation, build guide, and the 9-step Physical Phone Test Plan.
   - `.github/workflows/android.yml` for automated GitHub Actions debug APK builds and artifact uploads.
