package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import com.example.model.AudioTrack
import com.example.player.PlayerController
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(
    private val playerController: PlayerController
) : ViewModel() {

    val currentTrack: StateFlow<AudioTrack?> = playerController.currentTrack
    val isPlaying: StateFlow<Boolean> = playerController.isPlaying
    val playbackPositionMs: StateFlow<Long> = playerController.playbackPositionMs
    val durationMs: StateFlow<Long> = playerController.durationMs
    val repeatMode: StateFlow<Int> = playerController.repeatMode
    val shuffleEnabled: StateFlow<Boolean> = playerController.shuffleEnabled
    val visualizerAmplitudes: StateFlow<FloatArray> = playerController.visualizerAmplitudes
    val queue: StateFlow<List<AudioTrack>> = playerController.queue

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun seekTo(positionMs: Long) {
        playerController.seekTo(positionMs)
    }

    fun playNext() {
        playerController.playNext()
    }

    fun playPrevious() {
        playerController.playPrevious()
    }

    fun toggleShuffle() {
        playerController.toggleShuffle()
    }

    fun cycleRepeatMode() {
        playerController.cycleRepeatMode()
    }

    fun playTrack(track: AudioTrack, fullQueue: List<AudioTrack> = listOf(track)) {
        playerController.playTrack(track, fullQueue)
    }
}
