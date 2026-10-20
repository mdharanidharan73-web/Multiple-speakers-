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

data class BluetoothAudioDevice(
    val name: String,
    val address: String,
    val bondState: BondState,
    val connectionState: ConnectionState,
    val isAudioOutputActive: Boolean,
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
     * Three-state chip status breakdown for honest display:
     * 1. Paired (Bonded in system)
     * 2. Connected (A2DP / ACL connected)
     * 3. Active Output (Audio currently streaming to this sink)
     */
    val statusSummary: String
        get() = when {
            isAudioOutputActive -> "Active Output"
            connectionState == ConnectionState.CONNECTED -> "Connected (Inactive Route)"
            bondState == BondState.BONDED -> "Paired (Offline)"
            bondState == BondState.BONDING -> "Pairing..."
            else -> "Disconnected"
        }
}
