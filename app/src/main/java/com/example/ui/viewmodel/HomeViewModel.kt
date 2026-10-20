package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioRouteMonitor
import com.example.audio.DualOutputAuditResult
import com.example.audio.DualOutputChecker
import com.example.bluetooth.BluetoothPermissionHelper
import com.example.bluetooth.BluetoothRepository
import com.example.data.preferences.UserPreferencesRepository
import com.example.model.BluetoothAudioDevice
import com.example.model.BondState
import com.example.model.ConnectionState
import com.example.model.SpeakerSlot
import com.example.model.UiError
import com.example.player.PlayerController
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private data class BluetoothStateBundle(
    val paired: List<BluetoothAudioDevice>,
    val discovered: List<BluetoothAudioDevice>,
    val isScanning: Boolean,
    val isBluetoothEnabled: Boolean
)

data class HomeUiState(
    val slot1: SpeakerSlot = SpeakerSlot(1),
    val slot2: SpeakerSlot = SpeakerSlot(2),
    val pairedDevices: List<BluetoothAudioDevice> = emptyList(),
    val discoveredDevices: List<BluetoothAudioDevice> = emptyList(),
    val isScanning: Boolean = false,
    val isBluetoothEnabled: Boolean = true,
    val isDualOutputActive: Boolean = false,
    val activeBtOutputCount: Int = 0,
    val auditResult: DualOutputAuditResult? = null,
    val currentError: UiError? = null,
    val showDevicePickerModal: Boolean = false,
    val pickingSlotNumber: Int = 1,
    val showAuditDialog: Boolean = false
)

class HomeViewModel(
    private val bluetoothRepository: BluetoothRepository,
    private val audioRouteMonitor: AudioRouteMonitor,
    private val dualOutputChecker: DualOutputChecker,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val playerController: PlayerController
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val btStateBundleFlow = combine(
            bluetoothRepository.pairedDevices,
            bluetoothRepository.discoveredDevices,
            bluetoothRepository.isScanning,
            bluetoothRepository.isBluetoothEnabled
        ) { paired, discovered, scanning, btEnabled ->
            BluetoothStateBundle(paired, discovered, scanning, btEnabled)
        }

        // Collect Bluetooth & Audio Route state updates live
        viewModelScope.launch {
            combine(
                btStateBundleFlow,
                audioRouteMonitor.activeBluetoothOutputs,
                userPreferencesRepository.userPreferencesFlow
            ) { btBundle, activeBtOutputs, prefs ->
                val paired = btBundle.paired
                val discovered = btBundle.discovered
                val scanning = btBundle.isScanning
                val btEnabled = btBundle.isBluetoothEnabled

                val activeAddresses = activeBtOutputs.mapNotNull { it.address }.toSet()

                // Resolve Speaker Slot 1
                val slot1Device = if (prefs.speaker1Address != null) {
                    resolveSlotDevice(prefs.speaker1Address, prefs.speaker1Name, paired, activeAddresses)
                } else null

                // Resolve Speaker Slot 2
                val slot2Device = if (prefs.speaker2Address != null) {
                    resolveSlotDevice(prefs.speaker2Address, prefs.speaker2Name, paired, activeAddresses)
                } else null

                val audit = dualOutputChecker.checkDualOutputSupport(audioRouteMonitor.activeOutputRoutes.value)

                HomeUiState(
                    slot1 = SpeakerSlot(1, slot1Device),
                    slot2 = SpeakerSlot(2, slot2Device),
                    pairedDevices = paired,
                    discoveredDevices = discovered,
                    isScanning = scanning,
                    isBluetoothEnabled = btEnabled,
                    isDualOutputActive = audit.isDualActive,
                    activeBtOutputCount = audit.activeCount,
                    auditResult = audit,
                    currentError = _uiState.value.currentError,
                    showDevicePickerModal = _uiState.value.showDevicePickerModal,
                    pickingSlotNumber = _uiState.value.pickingSlotNumber,
                    showAuditDialog = _uiState.value.showAuditDialog
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }

    private fun resolveSlotDevice(
        address: String,
        fallbackName: String?,
        pairedList: List<BluetoothAudioDevice>,
        activeAddresses: Set<String>
    ): BluetoothAudioDevice {
        val found = pairedList.firstOrNull { it.address == address }
        return found ?: BluetoothAudioDevice(
            name = fallbackName ?: "Configured Speaker",
            address = address,
            bondState = BondState.BONDED,
            connectionState = ConnectionState.DISCONNECTED,
            isAudioOutputActive = activeAddresses.contains(address),
            brand = "Unknown",
            batteryLevel = null
        )
    }

    fun openDevicePicker(slotNumber: Int) {
        _uiState.value = _uiState.value.copy(
            showDevicePickerModal = true,
            pickingSlotNumber = slotNumber
        )
        refreshDevices()
    }

    fun closeDevicePicker() {
        _uiState.value = _uiState.value.copy(showDevicePickerModal = false)
        bluetoothRepository.stopDiscovery()
    }

    fun selectSpeakerForSlot(device: BluetoothAudioDevice, slotNumber: Int) {
        viewModelScope.launch {
            if (slotNumber == 1) {
                userPreferencesRepository.saveSpeaker1(device.address, device.name)
            } else {
                userPreferencesRepository.saveSpeaker2(device.address, device.name)
            }
            closeDevicePicker()
        }
    }

    fun clearSlot(slotNumber: Int) {
        viewModelScope.launch {
            if (slotNumber == 1) {
                userPreferencesRepository.saveSpeaker1(null, null)
            } else {
                userPreferencesRepository.saveSpeaker2(null, null)
            }
        }
    }

    fun startDiscovery() {
        bluetoothRepository.startDiscovery()
    }

    fun refreshDevices() {
        bluetoothRepository.refreshDevices()
        audioRouteMonitor.refreshRoutes()
    }

    fun pairDevice(address: String) {
        bluetoothRepository.createBond(address)
    }

    fun checkDualAudioSupport() {
        val audit = dualOutputChecker.checkDualOutputSupport(audioRouteMonitor.activeOutputRoutes.value)
        _uiState.value = _uiState.value.copy(
            auditResult = audit,
            showAuditDialog = true
        )
    }

    fun dismissAuditDialog() {
        _uiState.value = _uiState.value.copy(showAuditDialog = false)
    }

    fun playOnBothSpeakers() {
        val audit = dualOutputChecker.checkDualOutputSupport(audioRouteMonitor.activeOutputRoutes.value)
        if (audit.isDualActive) {
            // Simultaneous output verified by live system routing!
            playerController.togglePlayPause()
        } else {
            // Honest notification and diagnosis
            _uiState.value = _uiState.value.copy(
                currentError = UiError.UnsupportedDualHardware(
                    activeCount = audit.activeCount,
                    details = audit.explanation
                ),
                showAuditDialog = true
            )
        }
    }

    fun dismissError() {
        _uiState.value = _uiState.value.copy(currentError = null)
    }

    fun executeRecoveryAction(context: Context, action: UiError.RecoveryAction) {
        dismissError()
        when (action) {
            UiError.RecoveryAction.ENABLE_BLUETOOTH,
            UiError.RecoveryAction.OPEN_BLUETOOTH_SETTINGS -> {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            UiError.RecoveryAction.OPEN_APP_SETTINGS -> {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", context.packageName, null)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            UiError.RecoveryAction.SCAN_DEVICES -> {
                startDiscovery()
            }
            UiError.RecoveryAction.RECONNECT_SPEAKER -> {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            }
            UiError.RecoveryAction.RESUME_PLAYBACK -> {
                playerController.togglePlayPause()
            }
            UiError.RecoveryAction.NAVIGATE_COMPATIBILITY -> {
                _uiState.value = _uiState.value.copy(showAuditDialog = true)
            }
            UiError.RecoveryAction.REFRESH_LIBRARY -> {
                // Handled in library screen
            }
            UiError.RecoveryAction.REQUEST_PERMISSIONS -> {
                // Triggered in UI activity
            }
            UiError.RecoveryAction.DISMISS -> {}
        }
    }
}
