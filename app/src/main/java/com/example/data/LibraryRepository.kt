package com.example.data

import android.content.Context
import com.example.audio.SyntheticTrackGenerator
import com.example.data.media.MediaStoreScanner
import com.example.data.room.PlaylistDao
import com.example.data.room.PlaylistEntity
import com.example.data.room.PlaylistItemEntity
import com.example.model.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface LibraryRepository {
    val localSongs: StateFlow<List<AudioTrack>>
    val sampleTracks: StateFlow<List<AudioTrack>>
    val allPlaylists: Flow<List<PlaylistEntity>>

    suspend fun refreshLibrary()
    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>>
    suspend fun createPlaylist(name: String): Long
    suspend fun addTrackToPlaylist(playlistId: Long, track: AudioTrack)
    suspend fun removePlaylistItem(itemId: Long)
    suspend fun deletePlaylist(playlistId: Long)
}

class LibraryRepositoryImpl(
    private val context: Context,
    private val playlistDao: PlaylistDao,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : LibraryRepository {

    private val mediaStoreScanner = MediaStoreScanner(context)

    private val _localSongs = MutableStateFlow<List<AudioTrack>>(emptyList())
    override val localSongs: StateFlow<List<AudioTrack>> = _localSongs.asStateFlow()

    private val _sampleTracks = MutableStateFlow<List<AudioTrack>>(emptyList())
    override val sampleTracks: StateFlow<List<AudioTrack>> = _sampleTracks.asStateFlow()

    override val allPlaylists: Flow<List<PlaylistEntity>> = playlistDao.getAllPlaylists()

    init {
        scope.launch {
            refreshLibrary()
        }
    }

    override suspend fun refreshLibrary() {
        val sampleList = SyntheticTrackGenerator.getOrGenerateSampleTracks(context)
        _sampleTracks.value = sampleList

        val deviceSongs = mediaStoreScanner.scanAudioFiles()
        _localSongs.value = deviceSongs
    }

    override fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> {
        return playlistDao.getItemsForPlaylist(playlistId)
    }

    override suspend fun createPlaylist(name: String): Long {
        return playlistDao.insertPlaylist(PlaylistEntity(name = name))
    }

    override suspend fun addTrackToPlaylist(playlistId: Long, track: AudioTrack) {
        val item = PlaylistItemEntity(
            playlistId = playlistId,
            trackId = track.id,
            trackTitle = track.title,
            trackArtist = track.artist,
            trackAlbum = track.album,
            trackDurationMs = track.durationMs,
            trackUri = track.contentUri
        )
        playlistDao.insertPlaylistItem(item)
    }

    override suspend fun removePlaylistItem(itemId: Long) {
        playlistDao.deletePlaylistItem(itemId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistDao.deletePlaylist(playlistId)
    }
}
