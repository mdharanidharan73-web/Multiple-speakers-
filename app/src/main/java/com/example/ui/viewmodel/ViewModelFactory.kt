package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.di.AppContainer

class ViewModelFactory(
    private val appContainer: AppContainer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(HomeViewModel::class.java) -> {
                HomeViewModel(
                    bluetoothRepository = appContainer.bluetoothRepository,
                    audioRouteMonitor = appContainer.audioRouteMonitor,
                    dualOutputChecker = appContainer.dualOutputChecker,
                    userPreferencesRepository = appContainer.userPreferencesRepository,
                    playerController = appContainer.playerController
                ) as T
            }
            modelClass.isAssignableFrom(PlayerViewModel::class.java) -> {
                PlayerViewModel(
                    playerController = appContainer.playerController
                ) as T
            }
            modelClass.isAssignableFrom(LibraryViewModel::class.java) -> {
                LibraryViewModel(
                    libraryRepository = appContainer.libraryRepository,
                    playerController = appContainer.playerController
                ) as T
            }
            modelClass.isAssignableFrom(SyncViewModel::class.java) -> {
                SyncViewModel(
                    audioRouteMonitor = appContainer.audioRouteMonitor
                ) as T
            }
            modelClass.isAssignableFrom(SettingsViewModel::class.java) -> {
                SettingsViewModel(
                    userPreferencesRepository = appContainer.userPreferencesRepository
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
