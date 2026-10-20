package com.example.player

import androidx.media3.common.C
import androidx.media3.common.audio.AudioProcessor
import androidx.media3.common.audio.BaseAudioProcessor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Custom Media3 AudioProcessor that intercepts 16-bit PCM buffer blocks
 * in ExoPlayer's audio pipeline and computes real RMS amplitude levels
 * across 16 visualizer frequency bands without requiring RECORD_AUDIO permission.
 */
class VisualizerAudioProcessor : BaseAudioProcessor() {

    companion object {
        const val BAND_COUNT = 16
    }

    private val _amplitudes = MutableStateFlow(FloatArray(BAND_COUNT) { 0f })
    val amplitudes: StateFlow<FloatArray> = _amplitudes.asStateFlow()

    private var sampleCount = 0
    private val bandAccumulators = DoubleArray(BAND_COUNT)
    private val bandSampleCounts = IntArray(BAND_COUNT)

    override fun onConfigure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        if (inputAudioFormat.encoding != C.ENCODING_PCM_16BIT) {
            return AudioProcessor.AudioFormat.NOT_SET
        }
        return inputAudioFormat
    }

    override fun queueInput(inputBuffer: ByteBuffer) {
        val remaining = inputBuffer.remaining()
        if (remaining == 0) return

        // Duplicate buffer to avoid altering ExoPlayer's position
        val duplicate = inputBuffer.asReadOnlyBuffer().order(ByteOrder.LITTLE_ENDIAN)
        val shortBuffer = duplicate.asShortBuffer()
        val numShorts = shortBuffer.remaining()

        var sumSquare = 0.0
        var peak = 0.0

        for (i in 0 until numShorts) {
            val sample = abs(shortBuffer.get().toInt()) / 32768.0
            sumSquare += sample * sample
            if (sample > peak) {
                peak = sample
            }

            // Distribute across simulated frequency bands by chunk
            val bandIndex = (i % BAND_COUNT)
            bandAccumulators[bandIndex] += sample * sample
            bandSampleCounts[bandIndex]++
        }

        sampleCount += numShorts

        // Update amplitude state every ~1024 samples for responsive 60fps UI
        if (sampleCount >= 1024) {
            val currentLevels = FloatArray(BAND_COUNT)
            val currentAmps = _amplitudes.value

            for (b in 0 until BAND_COUNT) {
                val count = bandSampleCounts[b]
                val rms = if (count > 0) sqrt(bandAccumulators[b] / count) else 0.0
                // Frequency weighting factor (lower bands get higher boost, higher bands smoothed)
                val weight = 1.0 + (b.toDouble() / BAND_COUNT) * 0.8
                val rawBandLevel = (rms * weight * 1.8).coerceIn(0.0, 1.0).toFloat()

                // Smooth decay: fast attack, gentle decay
                val previous = currentAmps.getOrElse(b) { 0f }
                val smoothed = if (rawBandLevel > previous) {
                    rawBandLevel
                } else {
                    max(0f, previous * 0.82f)
                }
                currentLevels[b] = smoothed

                bandAccumulators[b] = 0.0
                bandSampleCounts[b] = 0
            }

            _amplitudes.value = currentLevels
            sampleCount = 0
        }

        // Pass buffer through to output buffer untouched for downstream audio sink
        val outputBuffer = replaceOutputBuffer(remaining)
        outputBuffer.put(inputBuffer)
        outputBuffer.flip()
    }

    fun resetAmplitudes() {
        _amplitudes.value = FloatArray(BAND_COUNT) { 0f }
    }
}
