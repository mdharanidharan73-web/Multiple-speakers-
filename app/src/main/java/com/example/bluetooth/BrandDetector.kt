package com.example.bluetooth

import java.util.Locale

object BrandDetector {

    /**
     * Determines the audio hardware brand from the Bluetooth advertised name.
     * Matches known audio manufacturer naming conventions and product models.
     */
    fun detectBrand(deviceName: String?): String {
        if (deviceName.isNullOrBlank()) return "Unknown"
        val lower = deviceName.lowercase(Locale.ROOT)

        return when {
            lower.contains("boat") -> "boAt"
            lower.contains("sony") || lower.startsWith("srs-") || lower.startsWith("wh-") || lower.startsWith("wf-") -> "Sony"
            lower.contains("jbl") || lower.contains("charge ") || lower.contains("flip ") || lower.contains("pulse ") || lower.contains("boombox") -> "JBL"
            lower.contains("bose") || lower.contains("soundlink") || lower.contains("quietcomfort") -> "Bose"
            lower.contains("marshall") || lower.contains("emberton") || lower.contains("stanmore") || lower.contains("kilburn") -> "Marshall"
            lower.contains("samsung") || lower.contains("galaxy buds") || lower.contains("level box") -> "Samsung"
            lower.contains("harman") || lower.contains("kardon") || lower.contains("onyx") -> "Harman Kardon"
            lower.contains("soundcore") || lower.contains("anker") || lower.contains("flare") || lower.contains("motion+") -> "Soundcore"
            lower.contains("realme") -> "Realme"
            lower.contains("oneplus") -> "OnePlus"
            lower.contains("xiaomi") || lower.startsWith("mi ") || lower.contains("redmi") -> "Xiaomi"
            lower.contains("beats") || lower.contains("pill") -> "Beats"
            lower.contains("ultimate ears") || lower.startsWith("ue ") || lower.contains("wonderboom") -> "Ultimate Ears"
            lower.contains("sennheiser") -> "Sennheiser"
            lower.contains("google") || lower.contains("pixel buds") -> "Google"
            lower.contains("tribit") -> "Tribit"
            lower.contains("bang & olufsen") || lower.contains("b&o") -> "B&O"
            else -> "Unknown"
        }
    }

    /**
     * Identifies if a device name or class represents an audio-capable speaker or headphone.
     */
    fun isAudioDevice(deviceName: String?, majorDeviceClass: Int? = null): Boolean {
        if (deviceName.isNullOrBlank()) return false
        val brand = detectBrand(deviceName)
        if (brand != "Unknown") return true

        // Check common audio keywords
        val lower = deviceName.lowercase(Locale.ROOT)
        val audioKeywords = listOf(
            "speaker", "soundbar", "audio", "headphone", "earphone", "buds",
            "bt speaker", "stereo", "boom", "music", "hifi", "sound"
        )
        return audioKeywords.any { lower.contains(it) }
    }
}
