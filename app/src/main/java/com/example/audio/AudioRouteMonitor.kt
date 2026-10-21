package com.example.audio

import android.content.Context
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Build
import com.example.model.AudioRouteInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface AudioRouteMonitor {
    val activeOutputRoutes: StateFlow<List<AudioRouteInfo>>
    val activeBluetoothOutputs: StateFlow<List<AudioRouteInfo>>
    val confirmedRoutedDevice: StateFlow<AudioRouteInfo?>
    val lastRouteUpdateTime: StateFlow<Long>
    val isPlaybackActive: StateFlow<Boolean>

    fun setConfirmedRoutedDevice(device: AudioDeviceInfo?, isPlaying: Boolean)
    fun refreshRoutes()
    fun release()
}

class AudioRouteMonitorImpl(
    private val context: Context,
    private val onRoutesChangedListener: ((List<AudioRouteInfo>, AudioRouteInfo?) -> Unit)? = null
) : AudioRouteMonitor {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val _activeOutputRoutes = MutableStateFlow<List<AudioRouteInfo>>(emptyList())
    override val activeOutputRoutes: StateFlow<List<AudioRouteInfo>> = _activeOutputRoutes.asStateFlow()

    private val _activeBluetoothOutputs = MutableStateFlow<List<AudioRouteInfo>>(emptyList())
    override val activeBluetoothOutputs: StateFlow<List<AudioRouteInfo>> = _activeBluetoothOutputs.asStateFlow()

    private val _confirmedRoutedDevice = MutableStateFlow<AudioRouteInfo?>(null)
    override val confirmedRoutedDevice: StateFlow<AudioRouteInfo?> = _confirmedRoutedDevice.asStateFlow()

    private val _lastRouteUpdateTime = MutableStateFlow(System.currentTimeMillis())
    override val lastRouteUpdateTime: StateFlow<Long> = _lastRouteUpdateTime.asStateFlow()

    private val _isPlaybackActive = MutableStateFlow(false)
    override val isPlaybackActive: StateFlow<Boolean> = _isPlaybackActive.asStateFlow()

    private val audioDeviceCallback = object : AudioDeviceCallback() {
        override fun onAudioDevicesAdded(addedDevices: Array<out AudioDeviceInfo>?) {
            _lastRouteUpdateTime.value = System.currentTimeMillis()
            refreshRoutes()
        }

        override fun onAudioDevicesRemoved(removedDevices: Array<out AudioDeviceInfo>?) {
            _lastRouteUpdateTime.value = System.currentTimeMillis()
            refreshRoutes()
        }
    }

    init {
        try {
            audioManager.registerAudioDeviceCallback(audioDeviceCallback, null)
        } catch (_: Exception) {}
        refreshRoutes()
    }

    override fun setConfirmedRoutedDevice(device: AudioDeviceInfo?, isPlaying: Boolean) {
        _isPlaybackActive.value = isPlaying
        _lastRouteUpdateTime.value = System.currentTimeMillis()

        if (device != null) {
            val mapped = mapDeviceInfo(device, isRouted = true)
            _confirmedRoutedDevice.value = mapped
        } else {
            _confirmedRoutedDevice.value = null
        }
        refreshRoutes()
    }

    override fun refreshRoutes() {
        try {
            val devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS)
            val currentRouted = _confirmedRoutedDevice.value

            val routeList = devices.map { device ->
                val isRouted = if (currentRouted != null) {
                    (currentRouted.id == device.id) ||
                            (!currentRouted.address.isNullOrBlank() && currentRouted.address == getDeviceAddressSafe(device))
                } else {
                    false
                }
                mapDeviceInfo(device, isRouted = isRouted)
            }

            _activeOutputRoutes.value = routeList
            val btList = routeList.filter { it.isBluetooth && it.isSink }
            _activeBluetoothOutputs.value = btList
            onRoutesChangedListener?.invoke(btList, _confirmedRoutedDevice.value)
        } catch (_: SecurityException) {
            // Handled safely without crashing
        } catch (_: Exception) {}
    }

    private fun getDeviceAddressSafe(info: AudioDeviceInfo): String? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.address
            } else {
                null
            }
        } catch (_: SecurityException) {
            null
        }
    }

    private fun mapDeviceInfo(info: AudioDeviceInfo, isRouted: Boolean = false): AudioRouteInfo {
        val type = info.type
        val isBt = isBluetoothDeviceType(type)
        val isBle = isBleDeviceType(type)
        val typeName = getDeviceTypeName(type)
        val name = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                info.productName?.toString() ?: typeName
            } else {
                typeName
            }
        } catch (_: SecurityException) {
            typeName
        }

        val address = getDeviceAddressSafe(info)

        return AudioRouteInfo(
            id = info.id,
            productName = name,
            type = type,
            typeName = typeName,
            isBluetooth = isBt,
            isBle = isBle,
            isSink = info.isSink,
            address = address,
            isCurrentlyRouted = isRouted
        )
    }

    private fun isBluetoothDeviceType(type: Int): Boolean {
        if (type == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP ||
            type == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
        ) {
            return true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                type == AudioDeviceInfo.TYPE_BLE_SPEAKER
            ) {
                return true
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (type == AudioDeviceInfo.TYPE_BLE_BROADCAST) {
                return true
            }
        }

        return false
    }

    private fun isBleDeviceType(type: Int): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (type == AudioDeviceInfo.TYPE_BLE_HEADSET ||
                type == AudioDeviceInfo.TYPE_BLE_SPEAKER
            ) {
                return true
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (type == AudioDeviceInfo.TYPE_BLE_BROADCAST) {
                return true
            }
        }
        return false
    }

    private fun getDeviceTypeName(type: Int): String {
        return when (type) {
            AudioDeviceInfo.TYPE_BUILTIN_SPEAKER -> "Phone Built-in Speaker"
            AudioDeviceInfo.TYPE_BUILTIN_EARPIECE -> "Earpiece"
            AudioDeviceInfo.TYPE_WIRED_HEADSET -> "Wired Headset"
            AudioDeviceInfo.TYPE_WIRED_HEADPHONES -> "Wired Headphones"
            AudioDeviceInfo.TYPE_BLUETOOTH_A2DP -> "Bluetooth A2DP Speaker"
            AudioDeviceInfo.TYPE_BLUETOOTH_SCO -> "Bluetooth SCO Headset"
            AudioDeviceInfo.TYPE_USB_DEVICE -> "USB Audio Device"
            AudioDeviceInfo.TYPE_USB_HEADSET -> "USB Headset"
            else -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && type == AudioDeviceInfo.TYPE_BLE_HEADSET) {
                    "Bluetooth LE Audio Headset"
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && type == AudioDeviceInfo.TYPE_BLE_SPEAKER) {
                    "Bluetooth LE Audio Speaker"
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && type == AudioDeviceInfo.TYPE_BLE_BROADCAST) {
                    "LE Audio Auracast Broadcast"
                } else {
                    "Audio Output ($type)"
                }
            }
        }
    }

    override fun release() {
        try {
            audioManager.unregisterAudioDeviceCallback(audioDeviceCallback)
        } catch (_: Exception) {}
    }
}
