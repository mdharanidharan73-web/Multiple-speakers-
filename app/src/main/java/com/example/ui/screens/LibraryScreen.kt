package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.room.PlaylistEntity
import com.example.model.AudioTrack
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.SortMode

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    libraryViewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by libraryViewModel.uiState.collectAsState()
    val localSongs by libraryViewModel.localSongs.collectAsState()
    val sampleTracks by libraryViewModel.sampleTracks.collectAsState()
    val playlists by libraryViewModel.playlists.collectAsState(initial = emptyList())

    var playlistNameInput by remember { mutableStateOf("") }

    Box(modifier = modifier.fillMaxSize().testTag("library_screen")) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: Title & Refresh button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Music Library",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { libraryViewModel.refreshLibrary() }, modifier = Modifier.testTag("library_refresh_btn")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Library")
                }
            }

            // Tabs: All Songs, Synthesized Tracks, Playlists
            SecondaryTabRow(
                selectedTabIndex = uiState.selectedTab,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { libraryViewModel.selectTab(0) },
                    text = { Text("Songs (${localSongs.size})") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { libraryViewModel.selectTab(1) },
                    text = { Text("Synthetic (${sampleTracks.size})") }
                )
                Tab(
                    selected = uiState.selectedTab == 2,
                    onClick = { libraryViewModel.selectTab(2) },
                    text = { Text("Playlists (${playlists.size})") }
                )
            }

            // Search Bar & Sort Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { libraryViewModel.setSearchQuery(it) },
                    placeholder = { Text("Search songs, artists, albums...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("library_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = {
                        val next = when (uiState.sortMode) {
                            SortMode.TITLE -> SortMode.ARTIST
                            SortMode.ARTIST -> SortMode.DURATION
                            SortMode.DURATION -> SortMode.TITLE
                        }
                        libraryViewModel.setSortMode(next)
                    },
                    modifier = Modifier.testTag("library_sort_btn")
                ) {
                    Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort Mode")
                }
            }

            // Tab Content
            when (uiState.selectedTab) {
                0 -> {
                    // Local Storage Songs
                    TrackList(
                        tracks = localSongs,
                        emptyMessage = "No local music found on device. Use Synthetic tab or add MP3s to device storage.",
                        onTrackClicked = { track ->
                            libraryViewModel.playTrack(track, localSongs)
                        },
                        onAddToPlaylist = { track ->
                            libraryViewModel.openAddToPlaylistDialog(track)
                        }
                    )
                }
                1 -> {
                    // Synthesized in-code tracks
                    TrackList(
                        tracks = sampleTracks,
                        emptyMessage = "Synthesizing demo audio tracks...",
                        onTrackClicked = { track ->
                            libraryViewModel.playTrack(track, sampleTracks)
                        },
                        onAddToPlaylist = { track ->
                            libraryViewModel.openAddToPlaylistDialog(track)
                        }
                    )
                }
                2 -> {
                    // Playlists
                    PlaylistList(
                        playlists = playlists,
                        onDeletePlaylist = { libraryViewModel.deletePlaylist(it) },
                        onCreatePlaylist = { libraryViewModel.openCreatePlaylistDialog() }
                    )
                }
            }
        }

        // FAB to create playlist on Playlists tab
        if (uiState.selectedTab == 2) {
            FloatingActionButton(
                onClick = { libraryViewModel.openCreatePlaylistDialog() },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 20.dp, bottom = 120.dp)
                    .testTag("create_playlist_fab"),
                containerColor = ElectricBlue
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Playlist")
            }
        }
    }

    // Create Playlist Dialog
    if (uiState.showCreatePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { libraryViewModel.closeCreatePlaylistDialog() },
            title = { Text("Create New Playlist") },
            text = {
                OutlinedTextField(
                    value = playlistNameInput,
                    onValueChange = { playlistNameInput = it },
                    label = { Text("Playlist Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("playlist_name_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        libraryViewModel.createPlaylist(playlistNameInput)
                        playlistNameInput = ""
                    },
                    modifier = Modifier.testTag("confirm_create_playlist")
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { libraryViewModel.closeCreatePlaylistDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Add To Playlist Dialog
    if (uiState.showAddToPlaylistDialog) {
        AlertDialog(
            onDismissRequest = { libraryViewModel.closeAddToPlaylistDialog() },
            title = { Text("Add to Playlist") },
            text = {
                if (playlists.isEmpty()) {
                    Text("No playlists created yet. Create a playlist first.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth().height(200.dp)) {
                        items(playlists) { playlist ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { libraryViewModel.addTrackToPlaylist(playlist.id) },
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Text(
                                    text = playlist.name,
                                    modifier = Modifier.padding(12.dp),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { libraryViewModel.closeAddToPlaylistDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun TrackList(
    tracks: List<AudioTrack>,
    emptyMessage: String,
    onTrackClicked: (AudioTrack) -> Unit,
    onAddToPlaylist: (AudioTrack) -> Unit
) {
    if (tracks.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emptyMessage,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tracks) { track ->
                TrackRowItem(
                    track = track,
                    onTrackClicked = { onTrackClicked(track) },
                    onAddToPlaylist = { onAddToPlaylist(track) }
                )
            }
        }
    }
}

@Composable
private fun TrackRowItem(
    track: AudioTrack,
    onTrackClicked: () -> Unit,
    onAddToPlaylist: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTrackClicked)
            .testTag("track_row_${track.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (track.isSynthesized) Icons.Default.Waves else Icons.Default.MusicNote,
                contentDescription = null,
                tint = if (track.isSynthesized) NeonViolet else ElectricBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = track.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${track.artist} • ${track.durationFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onAddToPlaylist, modifier = Modifier.testTag("add_to_playlist_btn_${track.id}")) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.PlaylistAdd,
                    contentDescription = "Add to playlist",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PlaylistList(
    playlists: List<PlaylistEntity>,
    onDeletePlaylist: (Long) -> Unit,
    onCreatePlaylist: () -> Unit
) {
    if (playlists.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "No custom playlists yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onCreatePlaylist) {
                    Text("Create Your First Playlist")
                }
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(playlists) { playlist ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = playlist.name, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text(text = "Custom Playlist", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = { onDeletePlaylist(playlist.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Playlist", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
