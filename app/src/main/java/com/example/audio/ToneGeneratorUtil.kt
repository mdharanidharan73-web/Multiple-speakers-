package com.example.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.sin

object ToneGeneratorUtil {

    private const val SAMPLE_RATE = 44100

    /**
     * Synthesizes and plays a pure PCM sine tone directly through AudioTrack.
     * @param frequencyHz frequency in Hertz (e.g. 440 for A4, 1000 for diagnostic beep)
     * @param durationMs duration in milliseconds
     * @param pan -1.0 for Left channel only, +1.0 for Right channel only, 0.0 for Center/Both
     */
    suspend fun playTone(
        frequencyHz: Double,
        durationMs: Int,
        pan: Float = 0f
    ) = withContext(Dispatchers.IO) {
        val numSamples = (SAMPLE_RATE * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples * 2) // Stereo: L, R interleaved

        val leftGain = ((1.0f - pan).coerceIn(0f, 2f) / 2f)
        val rightGain = ((1.0f + pan).coerceIn(0f, 2f) / 2f)

        for (i in 0 until numSamples) {
            val angle = 2.0 * Math.PI * i * frequencyHz / SAMPLE_RATE
            // Apply slight attack and release envelope to prevent audible clicks
            val envelope = when {
                i < 400 -> i / 400.0
                i > numSamples - 400 -> (numSamples - i) / 400.0
                else -> 1.0
            }
            val sampleVal = (sin(angle) * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()

            buffer[i * 2] = (sampleVal * leftGain).toInt().toShort()
            buffer[i * 2 + 1] = (sampleVal * rightGain).toInt().toShort()
        }

        val minBufferSize = AudioTrack.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_OUT_STEREO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                    .build()
            )
            .setBufferSizeInBytes(buffer.size * 2.coerceAtLeast(minBufferSize))
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()

        try {
            track.write(buffer, 0, buffer.size)
            track.play()
            kotlinx.coroutines.delay(durationMs.toLong() + 50)
        } finally {
            try {
                track.stop()
                track.release()
            } catch (_: Exception) {}
        }
    }
}
