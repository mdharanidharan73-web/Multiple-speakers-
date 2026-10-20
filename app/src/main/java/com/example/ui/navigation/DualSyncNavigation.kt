package com.example.ui.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.ui.components.MiniPlayerBar
import com.example.ui.screens.CompatibilityScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LibraryScreen
import com.example.ui.screens.NowPlayingScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SyncScreen
import com.example.ui.screens.TroubleshootingScreen
import com.example.ui.screens.TutorialScreen
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.PlayerViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SyncViewModel

enum class Screen(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    LIBRARY("Library", Icons.Default.LibraryMusic),
    SYNC("Sync", Icons.Default.Sync),
    COMPATIBILITY("Compatibility", Icons.Default.Tune),
    SETTINGS("Settings", Icons.Default.Settings),
    NOW_PLAYING("Now Playing", Icons.Default.Home),
    TUTORIAL("Tutorial", Icons.Default.Home),
    TROUBLESHOOTING("Troubleshooting", Icons.Default.Home)
}

@Composable
fun DualSyncAppContent(
    homeViewModel: HomeViewModel,
    playerViewModel: PlayerViewModel,
    libraryViewModel: LibraryViewModel,
    syncViewModel: SyncViewModel,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by rememberSaveable { mutableStateOf(Screen.HOME) }
    var previousScreen by rememberSaveable { mutableStateOf(Screen.HOME) }

    val currentTrack by playerViewModel.currentTrack.collectAsState()
    val isPlaying by playerViewModel.isPlaying.collectAsState()
    val amplitudes by playerViewModel.visualizerAmplitudes.collectAsState()
    val userPrefs by settingsViewModel.userPreferences.collectAsState()

    // Show tutorial on first launch if not yet completed
    val showTutorial = !userPrefs.isTutorialCompleted && currentScreen == Screen.TUTORIAL

    if (currentScreen == Screen.TUTORIAL) {
        TutorialScreen(
            onFinish = {
                settingsViewModel.setTutorialCompleted(true)
                currentScreen = Screen.HOME
            }
        )
        return
    }

    if (currentScreen == Screen.NOW_PLAYING) {
        NowPlayingScreen(
            playerViewModel = playerViewModel,
            onBackClicked = { currentScreen = previousScreen }
        )
        return
    }

    if (currentScreen == Screen.TROUBLESHOOTING) {
        TroubleshootingScreen(
            onBackClicked = { currentScreen = Screen.SETTINGS }
        )
        return
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            Column(modifier = Modifier.navigationBarsPadding()) {
                // Persistent Mini Player above Navigation Bar
                AnimatedVisibility(
                    visible = currentTrack != null,
                    enter = slideInVertically { it },
                    exit = slideOutVertically { it }
                ) {
                    MiniPlayerBar(
                        track = currentTrack,
                        isPlaying = isPlaying,
                        amplitudes = amplitudes,
                        onBarClicked = {
                            previousScreen = currentScreen
                            currentScreen = Screen.NOW_PLAYING
                        },
                        onPlayPauseClicked = { playerViewModel.togglePlayPause() },
                        onNextClicked = { playerViewModel.playNext() }
                    )
                }

                // Bottom 5-Tab Navigation Bar
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp,
                    modifier = Modifier.testTag("main_navigation_bar")
                ) {
                    val tabs = listOf(
                        Screen.HOME,
                        Screen.LIBRARY,
                        Screen.SYNC,
                        Screen.COMPATIBILITY,
                        Screen.SETTINGS
                    )
                    tabs.forEach { screen ->
                        NavigationBarItem(
                            selected = currentScreen == screen,
                            onClick = { currentScreen = screen },
                            icon = { Icon(screen.icon, contentDescription = screen.title) },
                            label = { Text(screen.title) },
                            modifier = Modifier.testTag("nav_tab_${screen.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.HOME -> HomeScreen(
                    homeViewModel = homeViewModel,
                    onNavigateToLibrary = { currentScreen = Screen.LIBRARY },
                    onNavigateToCompatibility = { currentScreen = Screen.COMPATIBILITY }
                )
                Screen.LIBRARY -> LibraryScreen(
                    libraryViewModel = libraryViewModel
                )
                Screen.SYNC -> SyncScreen(
                    syncViewModel = syncViewModel
                )
                Screen.COMPATIBILITY -> CompatibilityScreen()
                Screen.SETTINGS -> SettingsScreen(
                    settingsViewModel = settingsViewModel,
                    onNavigateToTroubleshooting = { currentScreen = Screen.TROUBLESHOOTING },
                    onNavigateToTutorial = { currentScreen = Screen.TUTORIAL }
                )
                else -> {}
            }
        }
    }
}
