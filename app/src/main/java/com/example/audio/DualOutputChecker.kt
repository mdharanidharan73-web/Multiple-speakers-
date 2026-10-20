package com.example.audio

import android.os.Build
import com.example.model.AudioRouteInfo
import java.util.Locale

data class DualOutputAuditResult(
    val isDualActive: Boolean,
    val activeCount: Int,
    val activeDeviceNames: List<String>,
    val statusTitle: String,
    val explanation: String,
    val recommendation: String,
    val oemFeatureName: String,
    val isLeAudioAuracastSupported: Boolean,
    val auracastNote: String
)

interface DualOutputChecker {
    fun checkDualOutputSupport(activeRoutes: List<AudioRouteInfo>): DualOutputAuditResult
}

class DualOutputCheckerImpl(
    private val manufacturerOverride: String? = null,
    private val sdkIntOverride: Int? = null
) : DualOutputChecker {

    override fun checkDualOutputSupport(activeRoutes: List<AudioRouteInfo>): DualOutputAuditResult {
        val btOutputs = activeRoutes.filter { it.isBluetooth && it.isSink }
        val activeCount = btOutputs.size
        val names = btOutputs.map { it.productName }

        val manufacturer = (manufacturerOverride ?: Build.MANUFACTURER ?: "").lowercase(Locale.ROOT)
        val oemFeature = when {
            manufacturer.contains("samsung") -> "Samsung Dual Audio (Media Output)"
            manufacturer.contains("xiaomi") || manufacturer.contains("redmi") -> "Xiaomi Sound Assistant / Multi-Speaker"
            manufacturer.contains("oneplus") || manufacturer.contains("oppo") -> "ColorOS / OxygenOS Dual-Channel Audio"
            manufacturer.contains("huawei") || manufacturer.contains("honor") -> "Huawei Dual Bluetooth Connect"
            manufacturer.contains("motorola") -> "Moto Multi-Bluetooth Sharing"
            else -> "Stock Android (No native multi-A2DP)"
        }

        val sdkInt = sdkIntOverride ?: Build.VERSION.SDK_INT
        val hasAuracastApi = sdkInt >= Build.VERSION_CODES.TIRAMISU
        val releaseStr = Build.VERSION.RELEASE ?: "13+"
        val auracastNote = if (hasAuracastApi) {
            "LE Audio Auracast framework present in Android $releaseStr (device-dependent, requires hardware and OEM stack support)."
        } else {
            "LE Audio / Auracast requires Android 13+ and dedicated Bluetooth 5.2+ LE Audio controller hardware."
        }

        if (activeCount >= 2) {
            return DualOutputAuditResult(
                isDualActive = true,
                activeCount = activeCount,
                activeDeviceNames = names,
                statusTitle = "Dual Output Active",
                explanation = "Your Android system is currently routing audio simultaneously to $activeCount Bluetooth sinks: ${names.joinToString(", ")}.",
                recommendation = "Both speakers are actively receiving audio. Enjoy synchronized music playback!",
                oemFeatureName = oemFeature,
                isLeAudioAuracastSupported = hasAuracastApi,
                auracastNote = auracastNote
            )
        } else if (activeCount == 1) {
            val singleName = names.firstOrNull() ?: "Connected Speaker"
            val oemGuide = when {
                manufacturer.contains("samsung") ->
                    "1. Swipe down Quick Settings panel.\n2. Tap 'Media Output'.\n3. Check BOTH of your connected speakers under Audio Output."
                manufacturer.contains("xiaomi") || manufacturer.contains("redmi") ->
                    "1. Open Settings > Sound & Vibration > Sound Assistant.\n2. Enable 'Multiple audio sources' or select both connected speakers."
                manufacturer.contains("oneplus") || manufacturer.contains("oppo") ->
                    "1. Open Settings > Bluetooth > Advanced Settings.\n2. Enable 'Dual-mode audio' to stream to two devices."
                else ->
                    "Stock Android's standard Bluetooth A2DP driver routes to only one sink at a time by design. To play on two speakers without OEM dual audio, consider an external dual Bluetooth transmitter (Mode B) or Auracast."
            }

            return DualOutputAuditResult(
                isDualActive = false,
                activeCount = 1,
                activeDeviceNames = names,
                statusTitle = "Single Output Active ($singleName)",
                explanation = "Standard Android A2DP restricts ordinary apps to 1 active audio output stream. Only \"$singleName\" is currently active.",
                recommendation = oemGuide,
                oemFeatureName = oemFeature,
                isLeAudioAuracastSupported = hasAuracastApi,
                auracastNote = auracastNote
            )
        } else {
            return DualOutputAuditResult(
                isDualActive = false,
                activeCount = 0,
                activeDeviceNames = emptyList(),
                statusTitle = "No Active Bluetooth Speakers",
                explanation = "No active Bluetooth audio sinks detected by Android AudioManager. Audio will play through phone built-in speakers.",
                recommendation = "Turn on your Bluetooth speakers, pair them in Android Settings, and tap 'Scan Bluetooth Devices'.",
                oemFeatureName = oemFeature,
                isLeAudioAuracastSupported = hasAuracastApi,
                auracastNote = auracastNote
            )
        }
    }
}
