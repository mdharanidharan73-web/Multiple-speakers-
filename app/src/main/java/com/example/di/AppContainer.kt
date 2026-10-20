package com.example.di

import android.content.Context
import androidx.room.Room
import com.example.audio.AudioRouteMonitor
import com.example.audio.AudioRouteMonitorImpl
import com.example.audio.DualOutputChecker
import com.example.audio.DualOutputCheckerImpl
import com.example.bluetooth.BluetoothRepository
import com.example.bluetooth.BluetoothRepositoryImpl
import com.example.data.LibraryRepository
import com.example.data.LibraryRepositoryImpl
import com.example.data.preferences.UserPreferencesRepository
import com.example.data.room.AppDatabase
import com.example.data.room.PlaylistDao
import com.example.player.PlayerController

interface AppContainer {
    val database: AppDatabase
    val playlistDao: PlaylistDao
    val userPreferencesRepository: UserPreferencesRepository
    val bluetoothRepository: BluetoothRepository
    val audioRouteMonitor: AudioRouteMonitor
    val dualOutputChecker: DualOutputChecker
    val libraryRepository: LibraryRepository
    val playerController: PlayerController
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "dualsync_database"
        ).fallbackToDestructiveMigration(dropAllTables = true).build()
    }

    override val playlistDao: PlaylistDao by lazy {
        database.playlistDao()
    }

    override val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context)
    }

    override val bluetoothRepository: BluetoothRepository by lazy {
        BluetoothRepositoryImpl(context)
    }

    override val audioRouteMonitor: AudioRouteMonitor by lazy {
        AudioRouteMonitorImpl(context) { activeRoutes ->
            val addresses = activeRoutes.mapNotNull { it.address }.toSet()
            bluetoothRepository.updateActiveOutputAddresses(addresses)
        }
    }

    override val dualOutputChecker: DualOutputChecker by lazy {
        DualOutputCheckerImpl()
    }

    override val libraryRepository: LibraryRepository by lazy {
        LibraryRepositoryImpl(context, playlistDao)
    }

    override val playerController: PlayerController by lazy {
        PlayerController(context)
    }
}
