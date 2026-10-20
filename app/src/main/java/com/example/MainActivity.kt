package com.example

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.bluetooth.BluetoothPermissionHelper
import com.example.ui.navigation.DualSyncAppContent
import com.example.ui.theme.DualSyncTheme
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.LibraryViewModel
import com.example.ui.viewmodel.PlayerViewModel
import com.example.ui.viewmodel.SettingsViewModel
import com.example.ui.viewmodel.SyncViewModel
import com.example.ui.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as DualSyncApp).container
        val factory = ViewModelFactory(appContainer)

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel(factory = factory)
            val userPrefs by settingsViewModel.userPreferences.collectAsState()

            val homeViewModel: HomeViewModel = viewModel(factory = factory)
            val playerViewModel: PlayerViewModel = viewModel(factory = factory)
            val libraryViewModel: LibraryViewModel = viewModel(factory = factory)
            val syncViewModel: SyncViewModel = viewModel(factory = factory)

            // Permission Launcher for Android 12+ (Bluetooth Scan/Connect) and Storage
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestMultiplePermissions()
            ) { _ ->
                homeViewModel.refreshDevices()
                libraryViewModel.refreshLibrary()
            }

            LaunchedEffect(Unit) {
                val permissionsToRequest = mutableListOf<String>()
                permissionsToRequest.addAll(BluetoothPermissionHelper.getRequiredBluetoothPermissions())
                permissionsToRequest.addAll(BluetoothPermissionHelper.getRequiredStoragePermissions())
                BluetoothPermissionHelper.getNotificationPermission()?.let {
                    permissionsToRequest.add(it)
                }

                permissionLauncher.launch(permissionsToRequest.toTypedArray())
            }

            DualSyncTheme(darkTheme = userPrefs.isDarkTheme) {
                DualSyncAppContent(
                    homeViewModel = homeViewModel,
                    playerViewModel = playerViewModel,
                    libraryViewModel = libraryViewModel,
                    syncViewModel = syncViewModel,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}
