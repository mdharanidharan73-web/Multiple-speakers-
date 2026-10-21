package com.example.audio

import android.os.Build
import com.example.model.AudioRouteInfo
import java.util.Locale

enum class DualOutputState {
    NO_BLUETOOTH_OUTPUT,
    SINGLE_OUTPUT_AVAILABLE,
    TWO_AVAILABLE_ONE_ACTIVE,
    DUAL_ROUTING_CONFIRMED,
    ROUTING_UNVERIFIED
}

data class DualOutputAuditResult(
    val state: DualOutputState,
    val isDualActive: Boolean,
    val availableCount: Int,
    val availableDeviceNames: List<String>,
    val confirmedActiveDeviceName: String?,
    val inactiveDeviceName: String?,
    val statusTitle: String,
    val explanation: String,
    val recommendation: String,
    val oemFeatureName: String,
    val isLeAudioAuracastSupported: Boolean,
    val auracastNote: String
) {
    val activeCount: Int
        get() = if (isDualActive) availableCount else if (confirmedActiveDeviceName != null) 1 else if (availableCount > 0) 1 else 0

    val activeDeviceNames: List<String>
        get() = when {
            isDualActive -> availableDeviceNames
            confirmedActiveDeviceName != null -> listOf(confirmedActiveDeviceName)
            availableDeviceNames.isNotEmpty() -> listOf(availableDeviceNames.first())
            else -> emptyList()
        }
}

interface DualOutputChecker {
    fun checkDualOutputSupport(
        availableRoutes: List<AudioRouteInfo>,
        confirmedRouted: AudioRouteInfo? = null,
        isPlaybackActive: Boolean = false,
        isOemDualAudioActive: Boolean = false
    ): DualOutputAuditResult
}

class DualOutputCheckerImpl(
    private val manufacturerOverride: String? = null,
    private val sdkIntOverride: Int? = null
) : DualOutputChecker {

    override fun checkDualOutputSupport(
        availableRoutes: List<AudioRouteInfo>,
        confirmedRouted: AudioRouteInfo?,
        isPlaybackActive: Boolean,
        isOemDualAudioActive: Boolean
    ): DualOutputAuditResult {
        val btOutputs = availableRoutes.filter { it.isBluetooth && it.isSink }
        val availableCount = btOutputs.size
        val availableNames = btOutputs.map { it.productName }

        val manufacturer = (manufacturerOverride ?: Build.MANUFACTURER ?: "").lowercase(Locale.ROOT)
        val oemFeature = when {
            manufacturer.contains("samsung") -> "Samsung Dual Audio (Media Output)"
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> "Xiaomi Sound Assistant / Multi-Speaker"
            manufacturer.contains("oneplus") || manufacturer.contains("oppo") -> "ColorOS / OxygenOS Dual-Channel Audio"
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> "Huawei Dual Bluetooth Connect"
            manufacturer.contains("motorola") -> "Moto Multi-Bluetooth Sharing"
            else -> "Stock Android (Standard single A2DP sink)"
        }

        val oemInstructions = when {
            manufacturer.contains("samsung") ->
                "1. Swipe down the Quick Settings / notification shade.\n2. Tap 'Media Output'.\n3. Check BOTH of your connected speakers under Audio Output."
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ->
                "1. Open Settings > Sound & Vibration > Sound Assistant.\n2. Enable 'Multiple audio sources' or select both connected speakers."
            manufacturer.contains("oneplus") || manufacturer.contains("oppo") ->
                "1. Open Settings > Bluetooth > Advanced Settings.\n2. Enable 'Dual-mode audio' to stream to two devices."
            else ->
                "Standard Android A2DP restricts apps to a single audio sink by default. To broadcast to both speakers, use an external dual Bluetooth transmitter (Mode B) or Auracast."
        }

        val sdkInt = sdkIntOverride ?: Build.VERSION.SDK_INT
        val hasAuracastApi = sdkInt >= Build.VERSION_CODES.TIRAMISU
        val releaseStr = Build.VERSION.RELEASE ?: "13+"
        val auracastNote = if (hasAuracastApi) {
            "LE Audio Auracast framework present in Android $releaseStr (device-dependent, requires hardware and OEM stack support)."
        } else {
            "LE Audio / Auracast requires Android 13+ and dedicated Bluetooth 5.2+ LE Audio controller hardware."
        }

        // Case 1: No Bluetooth outputs available
        if (availableCount == 0) {
            return DualOutputAuditResult(
                state = DualOutputState.NO_BLUETOOTH_OUTPUT,
                isDualActive = false,
                availableCount = 0,
                availableDeviceNames = emptyList(),
                confirmedActiveDeviceName = null,
                inactiveDeviceName = null,
                statusTitle = "No Active Bluetooth Speakers",
                explanation = "No Bluetooth audio sinks detected by Android AudioManager. Audio will play through the phone's built-in speaker.",
                recommendation = "Power on both Bluetooth speakers, pair them in Android Settings, and tap 'Scan Bluetooth Devices'.",
                oemFeatureName = oemFeature,
                isLeAudioAuracastSupported = hasAuracastApi,
                auracastNote = auracastNote
            )
        }

        // Case 2: Only 1 Bluetooth output available
        if (availableCount == 1) {
            val singleName = availableNames.firstOrNull() ?: "Connected Speaker"
            return DualOutputAuditResult(
                state = DualOutputState.SINGLE_OUTPUT_AVAILABLE,
                isDualActive = false,
                availableCount = 1,
                availableDeviceNames = availableNames,
                confirmedActiveDeviceName = singleName,
                inactiveDeviceName = null,
                statusTitle = "Single Output Active ($singleName)",
                explanation = "Only \"$singleName\" is currently connected to your phone. Connect your second speaker to attempt dual playback.",
                recommendation = "Connect your second speaker in Android Bluetooth Settings.",
                oemFeatureName = oemFeature,
                isLeAudioAuracastSupported = hasAuracastApi,
                auracastNote = auracastNote
            )
        }

        // Case 3: 2 or more Bluetooth outputs available
        // Check if verified multiple routing is confirmed by OEM or Auracast
        if (isOemDualAudioActive) {
            return DualOutputAuditResult(
                state = DualOutputState.DUAL_ROUTING_CONFIRMED,
                isDualActive = true,
                availableCount = availableCount,
                availableDeviceNames = availableNames,
                confirmedActiveDeviceName = availableNames.firstOrNull(),
                inactiveDeviceName = null,
                statusTitle = "Dual Output Active",
                explanation = "Your system's dual-audio routing is confirmed active across multiple Bluetooth outputs: ${availableNames.joinToString(", ")}.",
                recommendation = "Both speakers are actively receiving audio. Latencies may differ depending on speaker DSP buffers.",
                oemFeatureName = oemFeature,
                isLeAudioAuracastSupported = hasAuracastApi,
                auracastNote = auracastNote
            )
        }

        // When playback is active and we have confirmed routed device:
        if (isPlaybackActive && confirmedRouted != null) {
            val activeName = confirmedRouted.productName
            val inactive = availableNames.firstOrNull { it != activeName } ?: "Second Speaker"

            return DualOutputAuditResult(
                state = DualOutputState.TWO_AVAILABLE_ONE_ACTIVE,
                isDualActive = false,
                availableCount = availableCount,
                availableDeviceNames = availableNames,
                confirmedActiveDeviceName = activeName,
                inactiveDeviceName = inactive,
                statusTitle = "Routing to 1 Output Only ($activeName)",
                explanation = "Both speakers are connected, but your phone is routing music to only one Bluetooth output (\"$activeName\"). The other speaker (\"$inactive\") is receiving no audio.",
                recommendation = oemInstructions,
                oemFeatureName = oemFeature,
                isLeAudioAuracastSupported = hasAuracastApi,
                auracastNote = auracastNote
            )
        }

        // Case 4: 2 outputs available, but playback not started or routing unverified yet
        val firstSpeaker = availableNames.getOrNull(0) ?: "Speaker 1"
        val secondSpeaker = availableNames.getOrNull(1) ?: "Speaker 2"

        return DualOutputAuditResult(
            state = DualOutputState.ROUTING_UNVERIFIED,
            isDualActive = false,
            availableCount = availableCount,
            availableDeviceNames = availableNames,
            confirmedActiveDeviceName = null,
            inactiveDeviceName = null,
            statusTitle = "2 Speakers Connected (Routing Unverified)",
            explanation = "Both \"$firstSpeaker\" and \"$secondSpeaker\" are connected via Bluetooth. By default, Android routes playback to only one sink unless OEM Dual Audio is turned on.",
            recommendation = oemInstructions,
            oemFeatureName = oemFeature,
            isLeAudioAuracastSupported = hasAuracastApi,
            auracastNote = auracastNote
        )
    }
}
