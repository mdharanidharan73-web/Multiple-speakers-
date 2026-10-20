package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRouteMonitor
import com.example.audio.ToneGeneratorUtil
import com.example.model.AudioRouteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CodecLatencyInfo(
    val name: String,
    val latencyRangeMs: String,
    val description: String,
    val dualAudioFeasibility: String
)

class SyncViewModel(
    private val audioRouteMonitor: AudioRouteMonitor
) : ViewModel() {

    val activeRoutes: StateFlow<List<AudioRouteInfo>> = audioRouteMonitor.activeOutputRoutes
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeBluetoothRoutes: StateFlow<List<AudioRouteInfo>> = audioRouteMonitor.activeBluetoothOutputs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isTonePlaying = MutableStateFlow(false)
    val isTonePlaying: StateFlow<Boolean> = _isTonePlaying.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    val codecLatencyGuide = listOf(
        CodecLatencyInfo(
            name = "SBC (Subband Codec)",
            latencyRangeMs = "150 – 250 ms",
            description = "Default Bluetooth audio codec present on 100% of speakers. High variable buffer latency.",
            dualAudioFeasibility = "High drift if speakers differ in internal DSP buffering."
        ),
        CodecLatencyInfo(
            name = "AAC (Advanced Audio Coding)",
            latencyRangeMs = "100 – 220 ms",
            description = "Common on Apple, Sony, and premium portable speakers. Android latency depends on SoC encoder.",
            dualAudioFeasibility = "Moderate sync drift between different brands."
        ),
        CodecLatencyInfo(
            name = "aptX / aptX HD",
            latencyRangeMs = "70 – 120 ms",
            description = "Qualcomm proprietary codec with fixed packet framing. Lower and more predictable latency.",
            dualAudioFeasibility = "Better sync stability if both speakers support aptX."
        ),
        CodecLatencyInfo(
            name = "aptX Low Latency (LL)",
            latencyRangeMs = "30 – 40 ms",
            description = "Ultra-low latency designed for gaming and TV broadcast. Ideal for dual-transmitter rigs.",
            dualAudioFeasibility = "Near-zero perceptible echo when both receivers use aptX-LL."
        ),
        CodecLatencyInfo(
            name = "LDAC (Sony High-Res)",
            latencyRangeMs = "120 – 200 ms",
            description = "High bit-rate codec (up to 990 kbps). Requires strong RF link; buffer can introduce latency.",
            dualAudioFeasibility = "Requires identical sampling profiles to avoid phase cancellations."
        ),
        CodecLatencyInfo(
            name = "LC3 (LE Audio / Auracast)",
            latencyRangeMs = "20 – 30 ms",
            description = "Modern Bluetooth 5.2+ standard. Native multi-stream and broadcast support with deterministic sync.",
            dualAudioFeasibility = "Hardware synchronized across arbitrary Auracast receivers."
        )
    )

    fun playConcertPitchA4() {
        playTone(440.0, 1000, 0f, "Playing 440 Hz (Concert A4) on all active outputs...")
    }

    fun playCalibrationBeep() {
        playTone(1000.0, 800, 0f, "Playing 1000 Hz diagnostic calibration beep...")
    }

    fun playLeftChannelTest() {
        playTone(440.0, 1200, -1.0f, "Playing Left Channel Only (Speaker 1 test)...")
    }

    fun playRightChannelTest() {
        playTone(660.0, 1200, 1.0f, "Playing Right Channel Only (Speaker 2 test)...")
    }

    private fun playTone(freq: Double, durationMs: Int, pan: Float, message: String) {
        if (_isTonePlaying.value) return
        viewModelScope.launch {
            _isTonePlaying.value = true
            _statusMessage.value = message
            try {
                ToneGeneratorUtil.playTone(freq, durationMs, pan)
            } finally {
                _isTonePlaying.value = false
                _statusMessage.value = null
            }
        }
    }

    fun refresh() {
        audioRouteMonitor.refreshRoutes()
    }
}
