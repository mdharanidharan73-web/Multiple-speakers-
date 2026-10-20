package com.example.player

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.model.AudioTrack
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class PlayerController(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main)
) {

    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null

    private val _currentTrack = MutableStateFlow<AudioTrack?>(null)
    val currentTrack: StateFlow<AudioTrack?> = _currentTrack.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _playbackPositionMs = MutableStateFlow(0L)
    val playbackPositionMs: StateFlow<Long> = _playbackPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs: StateFlow<Long> = _durationMs.asStateFlow()

    private val _repeatMode = MutableStateFlow(Player.REPEAT_MODE_OFF)
    val repeatMode: StateFlow<Int> = _repeatMode.asStateFlow()

    private val _shuffleEnabled = MutableStateFlow(false)
    val shuffleEnabled: StateFlow<Boolean> = _shuffleEnabled.asStateFlow()

    private val _queue = MutableStateFlow<List<AudioTrack>>(emptyList())
    val queue: StateFlow<List<AudioTrack>> = _queue.asStateFlow()

    val visualizerAmplitudes: StateFlow<FloatArray> = PlaybackService.visualizerAudioProcessor.amplitudes

    private var progressTickerJob: Job? = null

    private val playerListener = object : Player.Listener {
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            _isPlaying.value = isPlaying
            if (isPlaying) {
                startProgressTicker()
            } else {
                stopProgressTicker()
            }
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            when (playbackState) {
                Player.STATE_READY -> {
                    mediaController?.let {
                        _durationMs.value = it.duration.coerceAtLeast(0L)
                        _playbackPositionMs.value = it.currentPosition.coerceAtLeast(0L)
                    }
                }
                Player.STATE_ENDED -> {
                    _isPlaying.value = false
                    stopProgressTicker()
                }
                else -> {}
            }
        }

        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            mediaItem?.let { item ->
                val track = _queue.value.firstOrNull { it.contentUri == item.mediaId }
                if (track != null) {
                    _currentTrack.value = track
                }
            }
        }
    }

    init {
        initializeController()
    }

    private fun initializeController() {
        val sessionToken = SessionToken(
            context,
            ComponentName(context, PlaybackService::class.java)
        )
        controllerFuture = MediaController.Builder(context, sessionToken).buildAsync()
        controllerFuture?.addListener({
            try {
                mediaController = controllerFuture?.get()
                mediaController?.addListener(playerListener)
                _isPlaying.value = mediaController?.isPlaying == true
            } catch (_: Exception) {}
        }, MoreExecutors.directExecutor())
    }

    private fun ensureServiceRunning() {
        val intent = Intent(context, PlaybackService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    fun playTrack(track: AudioTrack, fullQueue: List<AudioTrack> = listOf(track)) {
        ensureServiceRunning()
        _currentTrack.value = track
        _queue.value = fullQueue

        val player = mediaController ?: PlaybackService.activePlayer
        if (player != null) {
            val mediaItems = fullQueue.map { t ->
                MediaItem.Builder()
                    .setMediaId(t.contentUri)
                    .setUri(Uri.parse(t.contentUri))
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(t.title)
                            .setArtist(t.artist)
                            .setAlbumTitle(t.album)
                            .build()
                    )
                    .build()
            }

            val startIndex = fullQueue.indexOfFirst { it.id == track.id }.coerceAtLeast(0)
            player.setMediaItems(mediaItems, startIndex, 0L)
            player.prepare()
            player.play()
            _isPlaying.value = true
            _durationMs.value = track.durationMs
            startProgressTicker()
        }
    }

    fun togglePlayPause() {
        val player = mediaController ?: PlaybackService.activePlayer
        if (player != null) {
            if (player.isPlaying) {
                player.pause()
                _isPlaying.value = false
                stopProgressTicker()
            } else {
                player.play()
                _isPlaying.value = true
                startProgressTicker()
            }
        } else if (_currentTrack.value != null) {
            playTrack(_currentTrack.value!!)
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaController ?: PlaybackService.activePlayer
        player?.seekTo(positionMs)
        _playbackPositionMs.value = positionMs
    }

    fun playNext() {
        val player = mediaController ?: PlaybackService.activePlayer
        if (player?.hasNextMediaItem() == true) {
            player.seekToNextMediaItem()
        } else {
            val currentList = _queue.value
            val currentIndex = currentList.indexOfFirst { it.id == _currentTrack.value?.id }
            if (currentIndex >= 0 && currentIndex < currentList.size - 1) {
                playTrack(currentList[currentIndex + 1], currentList)
            }
        }
    }

    fun playPrevious() {
        val player = mediaController ?: PlaybackService.activePlayer
        if (player?.hasPreviousMediaItem() == true) {
            player.seekToPreviousMediaItem()
        } else {
            val currentList = _queue.value
            val currentIndex = currentList.indexOfFirst { it.id == _currentTrack.value?.id }
            if (currentIndex > 0) {
                playTrack(currentList[currentIndex - 1], currentList)
            }
        }
    }

    fun toggleShuffle() {
        val next = !_shuffleEnabled.value
        _shuffleEnabled.value = next
        (mediaController ?: PlaybackService.activePlayer)?.shuffleModeEnabled = next
    }

    fun cycleRepeatMode() {
        val nextMode = when (_repeatMode.value) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ALL
            Player.REPEAT_MODE_ALL -> Player.REPEAT_MODE_ONE
            else -> Player.REPEAT_MODE_OFF
        }
        _repeatMode.value = nextMode
        (mediaController ?: PlaybackService.activePlayer)?.repeatMode = nextMode
    }

    private fun startProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = scope.launch {
            while (isActive) {
                val player = mediaController ?: PlaybackService.activePlayer
                if (player != null && player.isPlaying) {
                    _playbackPositionMs.value = player.currentPosition.coerceAtLeast(0L)
                    _durationMs.value = player.duration.coerceAtLeast(0L)
                }
                delay(250)
            }
        }
    }

    private fun stopProgressTicker() {
        progressTickerJob?.cancel()
        progressTickerJob = null
    }

    fun release() {
        stopProgressTicker()
        mediaController?.removeListener(playerListener)
        controllerFuture?.let { MediaController.releaseFuture(it) }
    }
}
