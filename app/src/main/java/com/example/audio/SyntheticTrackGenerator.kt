package com.example.audio

import android.content.Context
import com.example.model.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin
import kotlin.random.Random

object SyntheticTrackGenerator {

    private const val SAMPLE_RATE = 44100
    private const val NUM_CHANNELS = 2 // Stereo
    private const val BITS_PER_SAMPLE = 16

    /**
     * Synthesizes offline audio tracks completely in code without downloading any media.
     * Writes standards-compliant 16-bit stereo PCM WAV files with genuine musical notes,
     * chords, bass pulses, and stereo separation tests.
     */
    suspend fun getOrGenerateSampleTracks(context: Context): List<AudioTrack> = withContext(Dispatchers.IO) {
        val tracksDir = File(context.cacheDir, "synthetic_tracks")
        if (!tracksDir.exists()) {
            tracksDir.mkdirs()
        }

        val track1File = File(tracksDir, "neon_synth_odyssey.wav")
        if (!track1File.exists() || track1File.length() < 1000) {
            generateSynthOdyssey(track1File, durationSeconds = 12)
        }

        val track2File = File(tracksDir, "deep_bass_groove.wav")
        if (!track2File.exists() || track2File.length() < 1000) {
            generateBassGroove(track2File, durationSeconds = 10)
        }

        val track3File = File(tracksDir, "dualsync_stereo_test.wav")
        if (!track3File.exists() || track3File.length() < 1000) {
            generateStereoCalibration(track3File, durationSeconds = 9)
        }

        listOf(
            AudioTrack(
                id = -101L,
                title = "Neon Synth Odyssey",
                artist = "DualSync Synth Engine",
                album = "In-Code Master Pieces",
                durationMs = 12_000L,
                contentUri = track1File.toURI().toString(),
                isSynthesized = true
            ),
            AudioTrack(
                id = -102L,
                title = "Deep Bass Groove",
                artist = "DualSync Synth Engine",
                album = "In-Code Master Pieces",
                durationMs = 10_000L,
                contentUri = track2File.toURI().toString(),
                isSynthesized = true
            ),
            AudioTrack(
                id = -103L,
                title = "Stereo Sync Calibration",
                artist = "DualSync Diagnostic Tools",
                album = "Hardware Channel Tests",
                durationMs = 9_000L,
                contentUri = track3File.toURI().toString(),
                isSynthesized = true
            )
        )
    }

    private fun generateSynthOdyssey(outputFile: File, durationSeconds: Int) {
        val totalFrames = SAMPLE_RATE * durationSeconds
        val buffer = ShortArray(totalFrames * NUM_CHANNELS)

        // Chord frequencies (A Minor: A3, C4, E4 / F Major: F3, A3, C4 / C Major: C3, E3, G3 / G Major: G3, B3, D4)
        val chords = listOf(
            doubleArrayOf(220.0, 261.63, 329.63), // Am
            doubleArrayOf(174.61, 220.0, 261.63), // F
            doubleArrayOf(130.81, 164.81, 196.00), // C
            doubleArrayOf(196.00, 246.94, 293.66)  // G
        )

        val beatFrames = (SAMPLE_RATE * 0.35).toInt() // Tempo ~85 BPM

        for (frame in 0 until totalFrames) {
            val chordIndex = ((frame / (beatFrames * 4)) % chords.size)
            val currentChord = chords[chordIndex]
            val subBeat = (frame / (beatFrames / 2)) % currentChord.size
            val arpeggioFreq = currentChord[subBeat] * 2.0

            val t = frame.toDouble() / SAMPLE_RATE

            // Harmonic chord pad
            var pad = 0.0
            for (freq in currentChord) {
                pad += sin(2.0 * Math.PI * freq * t)
            }
            pad = (pad / currentChord.size) * 0.35

            // Arpeggio lead
            val lead = sin(2.0 * Math.PI * arpeggioFreq * t) * 0.25

            // Rhythm kick pulse every beat
            val beatPhase = frame % beatFrames
            val kick = if (beatPhase < 2500) {
                val decay = 1.0 - (beatPhase / 2500.0)
                sin(2.0 * Math.PI * (120.0 * decay + 45.0) * (beatPhase.toDouble() / SAMPLE_RATE)) * 0.45 * decay
            } else 0.0

            val sample = ((pad + lead + kick).coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.85).toInt().toShort()

            // Slight stereo spread: L has lead slightly higher, R has pad slightly higher
            buffer[frame * 2] = ((sample * 0.95).toInt()).toShort()
            buffer[frame * 2 + 1] = ((sample * 0.90).toInt()).toShort()
        }

        writeWavFile(outputFile, buffer, SAMPLE_RATE, NUM_CHANNELS)
    }

    private fun generateBassGroove(outputFile: File, durationSeconds: Int) {
        val totalFrames = SAMPLE_RATE * durationSeconds
        val buffer = ShortArray(totalFrames * NUM_CHANNELS)

        val bassFreqs = doubleArrayOf(55.0, 65.41, 73.42, 82.41) // A1, C2, D2, E2
        val stepFrames = SAMPLE_RATE / 2

        for (frame in 0 until totalFrames) {
            val bassIndex = (frame / stepFrames) % bassFreqs.size
            val freq = bassFreqs[bassIndex]
            val t = frame.toDouble() / SAMPLE_RATE

            // Sub bass wave + 2nd harmonic
            val sub = sin(2.0 * Math.PI * freq * t) * 0.55
            val harmonic = sin(2.0 * Math.PI * (freq * 2) * t) * 0.25

            // Stereo hi-hat tick
            val tickPhase = frame % (stepFrames / 2)
            val hiHat = if (tickPhase < 600) {
                (Random.nextDouble(-0.15, 0.15) * (1.0 - tickPhase / 600.0))
            } else 0.0

            val leftOut = ((sub + harmonic + hiHat).coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.8).toInt().toShort()
            val rightOut = ((sub + harmonic - hiHat).coerceIn(-1.0, 1.0) * Short.MAX_VALUE * 0.8).toInt().toShort()

            buffer[frame * 2] = leftOut
            buffer[frame * 2 + 1] = rightOut
        }

        writeWavFile(outputFile, buffer, SAMPLE_RATE, NUM_CHANNELS)
    }

    private fun generateStereoCalibration(outputFile: File, durationSeconds: Int) {
        val totalFrames = SAMPLE_RATE * durationSeconds
        val buffer = ShortArray(totalFrames * NUM_CHANNELS)

        // Cycle:
        // Seconds 0-3: Left channel tone (Speaker 1 test)
        // Seconds 3-6: Right channel tone (Speaker 2 test)
        // Seconds 6-9: Stereo unison harmonic chord (Both speakers simultaneous)
        val phaseFrames = SAMPLE_RATE * 3

        for (frame in 0 until totalFrames) {
            val phase = frame / phaseFrames
            val t = frame.toDouble() / SAMPLE_RATE

            when (phase) {
                0 -> {
                    // Left channel only (440 Hz)
                    val sample = (sin(2.0 * Math.PI * 440.0 * t) * Short.MAX_VALUE * 0.7).toInt().toShort()
                    buffer[frame * 2] = sample
                    buffer[frame * 2 + 1] = 0
                }
                1 -> {
                    // Right channel only (660 Hz)
                    val sample = (sin(2.0 * Math.PI * 660.0 * t) * Short.MAX_VALUE * 0.7).toInt().toShort()
                    buffer[frame * 2] = 0
                    buffer[frame * 2 + 1] = sample
                }
                else -> {
                    // Both channels in phase (880 Hz + 440 Hz)
                    val sample = ((sin(2.0 * Math.PI * 440.0 * t) * 0.4 + sin(2.0 * Math.PI * 880.0 * t) * 0.3)
                            * Short.MAX_VALUE).toInt().toShort()
                    buffer[frame * 2] = sample
                    buffer[frame * 2 + 1] = sample
                }
            }
        }

        writeWavFile(outputFile, buffer, SAMPLE_RATE, NUM_CHANNELS)
    }

    private fun writeWavFile(file: File, pcmData: ShortArray, sampleRate: Int, channels: Int) {
        val byteData = ByteArray(pcmData.size * 2)
        ByteBuffer.wrap(byteData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().put(pcmData)

        val totalDataLen = byteData.size
        val totalAudioLen = totalDataLen + 36

        FileOutputStream(file).use { out ->
            val header = ByteArray(44)
            val byteRate = sampleRate * channels * (BITS_PER_SAMPLE / 8)
            val blockAlign = channels * (BITS_PER_SAMPLE / 8)

            // RIFF header
            header[0] = 'R'.code.toByte()
            header[1] = 'I'.code.toByte()
            header[2] = 'F'.code.toByte()
            header[3] = 'F'.code.toByte()
            header[4] = (totalAudioLen and 0xff).toByte()
            header[5] = ((totalAudioLen shr 8) and 0xff).toByte()
            header[6] = ((totalAudioLen shr 16) and 0xff).toByte()
            header[7] = ((totalAudioLen shr 24) and 0xff).toByte()
            // WAVE
            header[8] = 'W'.code.toByte()
            header[9] = 'A'.code.toByte()
            header[10] = 'V'.code.toByte()
            header[11] = 'E'.code.toByte()
            // 'fmt ' chunk
            header[12] = 'f'.code.toByte()
            header[13] = 'm'.code.toByte()
            header[14] = 't'.code.toByte()
            header[15] = ' '.code.toByte()
            header[16] = 16 // 4 bytes: size of 'fmt ' chunk
            header[17] = 0
            header[18] = 0
            header[19] = 0
            header[20] = 1 // format = 1 (PCM)
            header[21] = 0
            header[22] = channels.toByte()
            header[23] = 0
            header[24] = (sampleRate and 0xff).toByte()
            header[25] = ((sampleRate shr 8) and 0xff).toByte()
            header[26] = ((sampleRate shr 16) and 0xff).toByte()
            header[27] = ((sampleRate shr 24) and 0xff).toByte()
            header[28] = (byteRate and 0xff).toByte()
            header[29] = ((byteRate shr 8) and 0xff).toByte()
            header[30] = ((byteRate shr 16) and 0xff).toByte()
            header[31] = ((byteRate shr 24) and 0xff).toByte()
            header[32] = blockAlign.toByte()
            header[33] = 0
            header[34] = BITS_PER_SAMPLE.toByte()
            header[35] = 0
            // 'data' chunk
            header[36] = 'd'.code.toByte()
            header[37] = 'a'.code.toByte()
            header[38] = 't'.code.toByte()
            header[39] = 'a'.code.toByte()
            header[40] = (totalDataLen and 0xff).toByte()
            header[41] = ((totalDataLen shr 8) and 0xff).toByte()
            header[42] = ((totalDataLen shr 16) and 0xff).toByte()
            header[43] = ((totalDataLen shr 24) and 0xff).toByte()

            out.write(header)
            out.write(byteData)
        }
    }
}
