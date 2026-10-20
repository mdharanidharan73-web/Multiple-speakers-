package com.example.model

data class AudioTrack(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val isSynthesized: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            return "%d:%02d".format(minutes, seconds)
        }
}

data class SpeakerSlot(
    val slotNumber: Int,
    val device: BluetoothAudioDevice? = null
)

data class AudioRouteInfo(
    val id: Int,
    val productName: String,
    val type: Int,
    val typeName: String,
    val isBluetooth: Boolean,
    val isBle: Boolean,
    val isSink: Boolean,
    val address: String? = null
)
