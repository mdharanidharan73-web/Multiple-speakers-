# DualSync Audio — Multi Bluetooth Speaker Player
> *"One Song. Multiple Speakers."*

DualSync Audio is a functional Android local music player and Bluetooth manager designed to connect and control two Bluetooth speakers from different manufacturers (e.g., boAt and Sony). Built with Jetpack Compose, Media3 ExoPlayer, Room, DataStore, and modern Android Bluetooth and audio routing APIs.

---

## Technical Honesty & Hardware Reality

1. **Paired ≠ Connected ≠ Active Output**:
   - **Paired**: Stored in Android's bonded Bluetooth database.
   - **Connected**: Actively linked via Bluetooth ACL / A2DP profile.
   - **Active Output**: Verified by `AudioManager` as a live audio sink.
   These three states are tracked and displayed independently in the UI.

2. **Android A2DP Routing Limits**:
   Standard stock Android A2DP restricts third-party applications to streaming to **only one Bluetooth sink** at a time. Third-party applications cannot independently open multiple A2DP sockets without custom OEM middleware or LE Audio hardware. DualSync Audio never fakes simultaneous streaming: it only reports "Dual output active" when two Bluetooth sinks are genuinely active in `AudioManager.getDevices(GET_DEVICES_OUTPUTS)`.

3. **No Placebo Sliders**:
   Because third-party apps cannot independently adjust latency buffers per hardware sink on stock Android, latency information is displayed as verified reference data. Placebo delay sliders are deliberately omitted.

4. **Real Battery Reporting**:
   Speaker battery level is queried solely through public system broadcasts (`ACTION_BATTERY_LEVEL_CHANGED`). If not exposed by the speaker, it displays `"Not available"`.

5. **In-Code Synthesized Sample Tracks**:
   Built-in tracks ("Neon Synth Odyssey", "Deep Bass Groove", "Stereo Sync Calibration") are synthesized mathematically in Kotlin into genuine 16-bit stereo PCM WAV files—no fake placeholders or network downloads.

---

## Features

- **Bluetooth Discovery & Brand Detection**:
  - Automatically identifies manufacturers (boAt, Sony, JBL, Bose, Marshall, Samsung, Soundcore, Realme, OnePlus, Xiaomi, Beats, etc.).
  - Real-time bonding and discovery management via `BluetoothAdapter` and lifecycle-safe `BroadcastReceiver`.
- **Dual Speaker Slots**:
  - Assign Speaker 1 and Speaker 2.
  - Three distinct status badges: Paired, Connected, Active Output.
  - Slot settings persisted across launches via Jetpack DataStore.
- **Media3 ExoPlayer Music Player**:
  - Foreground `MediaSessionService` with background playback, lock screen controls, and notification controls.
  - Audio focus management (`AUDIOFOCUS_GAIN`, `AUDIOFOCUS_LOSS_TRANSIENT`) and `ACTION_AUDIO_BECOMING_NOISY` safety handling.
  - Real-time **Dynamic Multi-Bar Equalizer** driven by a custom Media3 `AudioProcessor` measuring PCM amplitude (no RECORD_AUDIO permission needed).
  - MediaStore library scanning with search and sorting.
  - Custom playlists saved in Room database.
- **Audio Sync & Diagnostics**:
  - Live active output routes.
  - Built-in hardware test tones (440 Hz Concert A4, 1000 Hz diagnostic beep, Left/Right channel isolation).
  - Detailed Bluetooth codec latency comparison (SBC, AAC, aptX, aptX-LL, LDAC, LC3).
- **Alternative Playback Methods**:
  - **Mode A (Native Dual Audio)**: Step-by-step guides for Samsung OneUI Dual Audio, Xiaomi Sound Assistant, and Android 13+ Auracast.
  - **Mode B (External Dual Transmitter)**: Guide for physical 3.5mm/USB-C dual-link aptX-LL transmitters.
  - **Mode C (Wi-Fi Synchronized Playback)**: Architectural specification for dual-receiver NTP clock synchronization.

---

## Project Structure

```
com.example/
├── MainActivity.kt               # Edge-to-edge entry point & permission launcher
├── DualSyncApp.kt                # Application subclass initializing AppContainer
├── di/
│   └── AppContainer.kt           # Singleton constructor dependency injection
├── model/
│   ├── UiError.kt                # Sealed error model with executable recovery actions
│   ├── BluetoothAudioDevice.kt   # Three-state device representation & battery
│   └── AudioModels.kt            # AudioTrack, SpeakerSlot, AudioRouteInfo
├── bluetooth/
│   ├── BrandDetector.kt          # Manufacturer prefix matching
│   ├── BluetoothRepository.kt    # BluetoothAdapter discovery & profile proxies
│   └── BluetoothPermissionHelper.kt # Version-aware permission management
├── audio/
│   ├── AudioRouteMonitor.kt      # AudioDeviceCallback & route tracking
│   ├── DualOutputChecker.kt      # Dual-sink audit & OEM detection
│   ├── ToneGeneratorUtil.kt      # Real-time PCM sine tone generator
│   └── SyntheticTrackGenerator.kt # Mathematical in-code WAV synthesis
├── player/
│   ├── VisualizerAudioProcessor.kt # Media3 PCM amplitude interceptor
│   ├── PlaybackService.kt        # MediaSessionService with audio sink
│   └── PlayerController.kt       # Playback orchestration & progress ticker
├── data/
│   ├── LibraryRepository.kt      # Song & playlist data repository
│   ├── media/MediaStoreScanner.kt # MediaStore audio content provider query
│   ├── preferences/UserPreferencesRepository.kt # DataStore settings
│   └── room/PlaylistDatabase.kt  # Room entities, DAO, and database
└── ui/
    ├── theme/                    # Electric Blue & Neon Violet palette
    ├── navigation/               # 5-tab NavigationBar & MiniPlayer host
    ├── components/               # SpeakerCard, MultiBarVisualizer, MiniPlayerBar
    ├── screens/                  # Home, Library, NowPlaying, Sync, Compat, Settings
    └── viewmodel/                # Home, Player, Library, Sync, Settings ViewModels
```

---

## Building and Installing

### Prerequisites
- Android Studio Ladybug or newer
- JDK 17
- Android SDK 36 (Android 16 preview / Android 15 stable)
- Device running minSdk 29 (Android 10) or higher

### Gradle Commands
```bash
# Run unit & Robolectric tests
gradle :app:testDebugUnitTest

# Assemble debug APK
gradle :app:assembleDebug
```
The generated APK will be located at:
`app/build/outputs/apk/debug/app-debug.apk`

---

## Physical Phone Test Plan

Execute this 9-step verification protocol on a real Android device:

1. **Install Debug APK**:
   - Transfer `app-debug.apk` to a physical Android device.
   - Enable "Install unknown apps" and install.
2. **Pair Two Bluetooth Speakers**:
   - Turn on two Bluetooth speakers from different brands (e.g. boAt Stone and Sony SRS-XB13).
   - Pair both in Android Settings > Bluetooth.
3. **Verify Three-State Distinction**:
   - Open DualSync Audio.
   - Tap **Scan Bluetooth Devices** and assign Speaker 1 and Speaker 2.
   - Confirm the UI displays:
     - `Paired`: Yes
     - `Connected`: Yes
     - `Active Output`: Only one speaker shows "Active Output" by default on stock Android.
4. **Test with Manufacturer Dual Audio**:
   - **Samsung**: Swipe down notification panel > Tap "Media Output" > Select both speakers. Verify that the app's status chip immediately flips to **"Dual Output Active"** with both devices listed.
   - **Xiaomi / OnePlus**: Enable Sound Assistant or Dual-Channel Audio.
5. **Test Permission Denial & Permanent Denial**:
   - Revoke Bluetooth permissions in Android Settings > Apps > DualSync Audio.
   - Relaunch the app. Tap "Scan Bluetooth Devices".
   - Confirm the `PermissionsNeeded` dialog appears with the "Grant Permissions" recovery button.
6. **Test with Bluetooth Radio Turned Off**:
   - Turn off Bluetooth in quick settings.
   - Confirm the `BluetoothDisabled` dialog appears with the "Turn On Bluetooth" action.
7. **Power Off Speaker Mid-Playback**:
   - Start playback of a synthesized track or local song.
   - Power off Speaker 1.
   - Confirm playback pauses gracefully without crashing (`AudioRouteLost` / `BecomingNoisy`).
8. **Lock Screen, Notification & Background Playback**:
   - Lock the phone during playback.
   - Verify lock-screen media controls (play/pause, next, track title).
   - Verify playback continues smoothly in background.
9. **Incoming Call & Audio Focus Loss**:
   - Receive an incoming phone call during playback.
   - Confirm ExoPlayer pauses immediately on `AUDIOFOCUS_LOSS_TRANSIENT` and resumes when the call ends.
