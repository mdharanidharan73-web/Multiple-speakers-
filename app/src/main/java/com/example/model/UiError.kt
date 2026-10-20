package com.example.model

/**
 * Sealed representation of every user-visible error state with
 * human-readable explanation and an actionable recovery mechanism.
 */
sealed class UiError(
    val title: String,
    val message: String,
    val actionLabel: String,
    val recoveryAction: RecoveryAction
) {
    enum class RecoveryAction {
        ENABLE_BLUETOOTH,
        REQUEST_PERMISSIONS,
        OPEN_APP_SETTINGS,
        OPEN_BLUETOOTH_SETTINGS,
        SCAN_DEVICES,
        RECONNECT_SPEAKER,
        NAVIGATE_COMPATIBILITY,
        REFRESH_LIBRARY,
        RESUME_PLAYBACK,
        DISMISS
    }

    object BluetoothDisabled : UiError(
        title = "Bluetooth Disabled",
        message = "Bluetooth is currently turned off. Turn it on to scan and manage your speakers.",
        actionLabel = "Turn On Bluetooth",
        recoveryAction = RecoveryAction.ENABLE_BLUETOOTH
    )

    object PermissionsDenied : UiError(
        title = "Permissions Needed",
        message = "DualSync Audio needs Bluetooth and Storage/Media permissions to find speakers and play local music.",
        actionLabel = "Grant Permissions",
        recoveryAction = RecoveryAction.REQUEST_PERMISSIONS
    )

    object PermissionsPermanentlyDenied : UiError(
        title = "Permissions Blocked",
        message = "Required permissions were denied permanently. Please enable them in Android App Settings.",
        actionLabel = "Open App Settings",
        recoveryAction = RecoveryAction.OPEN_APP_SETTINGS
    )

    data class SpeakerDisconnected(val speakerName: String) : UiError(
        title = "Speaker Disconnected",
        message = "\"$speakerName\" disconnected from your phone. Ensure it is powered on and in range.",
        actionLabel = "Reconnect",
        recoveryAction = RecoveryAction.RECONNECT_SPEAKER
    )

    data class SpeakerOutOfRange(val speakerName: String) : UiError(
        title = "Speaker Out of Range",
        message = "\"$speakerName\" is unreachable or low on battery. Bring it closer to your device.",
        actionLabel = "Scan Again",
        recoveryAction = RecoveryAction.SCAN_DEVICES
    )

    data class UnsupportedDualHardware(val activeCount: Int, val details: String) : UiError(
        title = "Single Audio Route Active",
        message = if (activeCount == 0) {
            "No active Bluetooth audio output found. Connect a Bluetooth speaker first."
        } else {
            "Only 1 Bluetooth output is active. Standard stock Android A2DP restricts apps to a single audio sink. Use OEM Dual Audio (Samsung/Xiaomi/OnePlus), Auracast, or external transmitters."
        },
        actionLabel = "View Dual Audio Guide",
        recoveryAction = RecoveryAction.NAVIGATE_COMPATIBILITY
    )

    data class MusicFileNotFound(val trackTitle: String) : UiError(
        title = "Track Not Found",
        message = "\"$trackTitle\" could not be located on storage. The file may have been moved or removed.",
        actionLabel = "Refresh Library",
        recoveryAction = RecoveryAction.REFRESH_LIBRARY
    )

    data class ConnectionFailed(val deviceName: String, val reason: String) : UiError(
        title = "Bluetooth Connection Failed",
        message = "Could not connect to $deviceName ($reason). Pair the speaker in Android Bluetooth Settings.",
        actionLabel = "Open Bluetooth Settings",
        recoveryAction = RecoveryAction.OPEN_BLUETOOTH_SETTINGS
    )

    object AudioRouteLost : UiError(
        title = "Audio Route Disconnected",
        message = "The active speaker was disconnected during playback. Playback has been paused safely.",
        actionLabel = "Resume Playback",
        recoveryAction = RecoveryAction.RESUME_PLAYBACK
    )

    object PlaybackFocusLoss : UiError(
        title = "Playback Interrupted",
        message = "Another app, navigation prompt, or phone call took audio focus.",
        actionLabel = "Resume",
        recoveryAction = RecoveryAction.RESUME_PLAYBACK
    )
}
