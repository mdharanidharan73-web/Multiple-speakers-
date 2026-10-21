package com.example.model

enum class BondState(val label: String) {
    BONDED("Paired"),
    BONDING("Pairing..."),
    NONE("Not Paired")
}

enum class ConnectionState(val label: String) {
    CONNECTED("Connected"),
    CONNECTING("Connecting..."),
    DISCONNECTED("Disconnected")
}

/**
 * The 6 distinct stages of Bluetooth audio hardware status:
 * 1. DISCOVERED: Found during Bluetooth inquiry scan.
 * 2. PAIRED: Stored in phone's bonded device list.
 * 3. CONNECTED: Active Bluetooth ACL / profile connection.
 * 4. OUTPUT_AVAILABLE: Registered in AudioManager as an available audio output sink.
 * 5. SYSTEM_ROUTED: Selected by Android AudioPolicy / AudioTrack for stream routing.
 * 6. CONFIRMED_ACTIVE: Actively receiving audio buffers during live playback.
 * 7. NOT_RECEIVING_AUDIO: Connected & available, but Android is NOT routing sound here.
 */
enum class AudioRoutingStatus(val label: String) {
    DISCOVERED("Discovered"),
    PAIRED_OFFLINE("Paired (Offline)"),
    BT_CONNECTED("Bluetooth Connected"),
    OUTPUT_AVAILABLE("Audio Output Available"),
    SYSTEM_ROUTED("System Route Selected"),
    CONFIRMED_ACTIVE("Confirmed Active Output"),
    NOT_RECEIVING_AUDIO("Connected (Not Receiving Audio)"),
    ROUTING_UNKNOWN("Routing Status Unknown")
}

data class BluetoothAudioDevice(
    val name: String,
    val address: String,
    val bondState: BondState,
    val connectionState: ConnectionState,
    val isAvailableAsOutput: Boolean = false,
    val isAudioOutputActive: Boolean = false, // True only if confirmed receiving audio
    val brand: String,
    val batteryLevel: Int? = null, // null = "Not available", strictly queried from system APIs
    val deviceClassType: String = "Audio Device",
    val isBleAudio: Boolean = false
) {
    /**
     * Formats battery level strictly based on verified system telemetry.
     */
    val batteryDisplay: String
        get() = when {
            batteryLevel != null && batteryLevel in 0..100 -> "$batteryLevel%"
            else -> "Not available"
        }

    /**
     * Honest six-state status determination:
     */
    val routingStatus: AudioRoutingStatus
        get() = when {
            isAudioOutputActive -> AudioRoutingStatus.CONFIRMED_ACTIVE
            connectionState == ConnectionState.CONNECTED && isAvailableAsOutput -> AudioRoutingStatus.NOT_RECEIVING_AUDIO
            connectionState == ConnectionState.CONNECTED -> AudioRoutingStatus.BT_CONNECTED
            bondState == BondState.BONDED -> AudioRoutingStatus.PAIRED_OFFLINE
            bondState == BondState.BONDING -> AudioRoutingStatus.DISCOVERED
            else -> AudioRoutingStatus.DISCOVERED
        }

    val statusSummary: String
        get() = routingStatus.label
}
