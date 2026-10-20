package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.LibraryRepository
import com.example.data.room.PlaylistEntity
import com.example.data.room.PlaylistItemEntity
import com.example.model.AudioTrack
import com.example.player.PlayerController
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SortMode {
    TITLE,
    ARTIST,
    DURATION
}

data class LibraryUiState(
    val selectedTab: Int = 0, // 0: All Songs, 1: Sample Tracks, 2: Playlists
    val searchQuery: String = "",
    val sortMode: SortMode = SortMode.TITLE,
    val showCreatePlaylistDialog: Boolean = false,
    val showAddToPlaylistDialog: Boolean = false,
    val trackToAddToPlaylist: AudioTrack? = null
)

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    val localSongs: StateFlow<List<AudioTrack>> = combine(
        libraryRepository.localSongs,
        _uiState
    ) { songs, state ->
        filterAndSortTracks(songs, state.searchQuery, state.sortMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sampleTracks: StateFlow<List<AudioTrack>> = combine(
        libraryRepository.sampleTracks,
        _uiState
    ) { tracks, state ->
        filterAndSortTracks(tracks, state.searchQuery, state.sortMode)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val playlists: Flow<List<PlaylistEntity>> = libraryRepository.allPlaylists

    private fun filterAndSortTracks(
        tracks: List<AudioTrack>,
        query: String,
        sortMode: SortMode
    ): List<AudioTrack> {
        val filtered = if (query.isBlank()) {
            tracks
        } else {
            val q = query.lowercase()
            tracks.filter {
                it.title.lowercase().contains(q) ||
                        it.artist.lowercase().contains(q) ||
                        it.album.lowercase().contains(q)
            }
        }

        return when (sortMode) {
            SortMode.TITLE -> filtered.sortedBy { it.title.lowercase() }
            SortMode.ARTIST -> filtered.sortedBy { it.artist.lowercase() }
            SortMode.DURATION -> filtered.sortedByDescending { it.durationMs }
        }
    }

    fun selectTab(index: Int) {
        _uiState.value = _uiState.value.copy(selectedTab = index)
    }

    fun setSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun setSortMode(mode: SortMode) {
        _uiState.value = _uiState.value.copy(sortMode = mode)
    }

    fun playTrack(track: AudioTrack, queue: List<AudioTrack>) {
        playerController.playTrack(track, queue)
    }

    fun refreshLibrary() {
        viewModelScope.launch {
            libraryRepository.refreshLibrary()
        }
    }

    fun openCreatePlaylistDialog() {
        _uiState.value = _uiState.value.copy(showCreatePlaylistDialog = true)
    }

    fun closeCreatePlaylistDialog() {
        _uiState.value = _uiState.value.copy(showCreatePlaylistDialog = false)
    }

    fun createPlaylist(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            libraryRepository.createPlaylist(name.trim())
            closeCreatePlaylistDialog()
        }
    }

    fun openAddToPlaylistDialog(track: AudioTrack) {
        _uiState.value = _uiState.value.copy(
            showAddToPlaylistDialog = true,
            trackToAddToPlaylist = track
        )
    }

    fun closeAddToPlaylistDialog() {
        _uiState.value = _uiState.value.copy(
            showAddToPlaylistDialog = false,
            trackToAddToPlaylist = null
        )
    }

    fun addTrackToPlaylist(playlistId: Long) {
        val track = _uiState.value.trackToAddToPlaylist ?: return
        viewModelScope.launch {
            libraryRepository.addTrackToPlaylist(playlistId, track)
            closeAddToPlaylistDialog()
        }
    }

    fun deletePlaylist(playlistId: Long) {
        viewModelScope.launch {
            libraryRepository.deletePlaylist(playlistId)
        }
    }

    fun getPlaylistItems(playlistId: Long): Flow<List<PlaylistItemEntity>> {
        return libraryRepository.getPlaylistItems(playlistId)
    }
}
