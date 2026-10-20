package com.example.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import com.example.model.BluetoothAudioDevice
import com.example.model.BondState
import com.example.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface BluetoothRepository {
    val pairedDevices: StateFlow<List<BluetoothAudioDevice>>
    val discoveredDevices: StateFlow<List<BluetoothAudioDevice>>
    val isScanning: StateFlow<Boolean>
    val isBluetoothEnabled: StateFlow<Boolean>
    val activeOutputAddresses: StateFlow<Set<String>>

    fun startDiscovery()
    fun stopDiscovery()
    fun createBond(address: String): Boolean
    fun getDevice(address: String): BluetoothAudioDevice?
    fun updateActiveOutputAddresses(addresses: Set<String>)
    fun refreshDevices()
    fun release()
}

class BluetoothRepositoryImpl(
    private val context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : BluetoothRepository {

    private val bluetoothManager: BluetoothManager? =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
    private val bluetoothAdapter: BluetoothAdapter? = bluetoothManager?.adapter

    private val _pairedDevices = MutableStateFlow<List<BluetoothAudioDevice>>(emptyList())
    override val pairedDevices: StateFlow<List<BluetoothAudioDevice>> = _pairedDevices.asStateFlow()

    private val _discoveredDevices = MutableStateFlow<List<BluetoothAudioDevice>>(emptyList())
    override val discoveredDevices: StateFlow<List<BluetoothAudioDevice>> = _discoveredDevices.asStateFlow()

    private val _isScanning = MutableStateFlow(false)
    override val isScanning: StateFlow<Boolean> = _isScanning.asStateFlow()

    private val _isBluetoothEnabled = MutableStateFlow(bluetoothAdapter?.isEnabled == true)
    override val isBluetoothEnabled: StateFlow<Boolean> = _isBluetoothEnabled.asStateFlow()

    private val _activeOutputAddresses = MutableStateFlow<Set<String>>(emptySet())
    override val activeOutputAddresses: StateFlow<Set<String>> = _activeOutputAddresses.asStateFlow()

    private val batteryLevelMap = mutableMapOf<String, Int>()
    private var a2dpProfileProxy: BluetoothA2dp? = null

    private val profileListener = object : BluetoothProfile.ServiceListener {
        override fun onServiceConnected(profile: Int, proxy: BluetoothProfile) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProfileProxy = proxy as BluetoothA2dp
                refreshDevices()
            }
        }

        override fun onServiceDisconnected(profile: Int) {
            if (profile == BluetoothProfile.A2DP) {
                a2dpProfileProxy = null
                refreshDevices()
            }
        }
    }

    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                    _isBluetoothEnabled.value = (state == BluetoothAdapter.STATE_ON)
                    refreshDevices()
                }

                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _isScanning.value = true
                }

                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _isScanning.value = false
                }

                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }

                    device?.let { handleFoundDevice(it) }
                }

                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    refreshDevices()
                }

                BluetoothDevice.ACTION_ACL_CONNECTED,
                BluetoothDevice.ACTION_ACL_DISCONNECTED,
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                    refreshDevices()
                }

                "android.bluetooth.device.action.BATTERY_LEVEL_CHANGED" -> {
                    val device: BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    }
                    val battery = intent.getIntExtra("android.bluetooth.device.extra.BATTERY_LEVEL", -1)
                    if (device != null && battery in 0..100) {
                        batteryLevelMap[device.address] = battery
                        refreshDevices()
                    }
                }
            }
        }
    }

    init {
        bluetoothAdapter?.getProfileProxy(context, profileListener, BluetoothProfile.A2DP)

        val filter = IntentFilter().apply {
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
            addAction("android.bluetooth.device.action.BATTERY_LEVEL_CHANGED")
        }
        context.registerReceiver(receiver, filter)

        refreshDevices()
    }

    override fun updateActiveOutputAddresses(addresses: Set<String>) {
        _activeOutputAddresses.value = addresses
        refreshDevices()
    }

    @SuppressLint("MissingPermission")
    override fun refreshDevices() {
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
            _pairedDevices.value = emptyList()
            return
        }

        if (!BluetoothPermissionHelper.hasBluetoothPermissions(context)) {
            return
        }

        try {
            val connectedA2dpDevices = a2dpProfileProxy?.connectedDevices?.map { it.address }?.toSet() ?: emptySet()
            val bonded = bluetoothAdapter.bondedDevices.orEmpty()
            val pairedList = bonded
                .filter { isAudioDevice(it) }
                .map { device ->
                    mapToDomainDevice(device, connectedA2dpDevices)
                }
            _pairedDevices.value = pairedList
        } catch (_: SecurityException) {
            // Permission not granted or revoked
        }
    }

    @SuppressLint("MissingPermission")
    private fun handleFoundDevice(device: BluetoothDevice) {
        if (!BluetoothPermissionHelper.hasBluetoothPermissions(context)) return

        try {
            val name = device.name
            if (name.isNullOrBlank() || !isAudioDevice(device)) return

            val connectedA2dpDevices = a2dpProfileProxy?.connectedDevices?.map { it.address }?.toSet() ?: emptySet()
            val domainDevice = mapToDomainDevice(device, connectedA2dpDevices)

            val current = _discoveredDevices.value.toMutableList()
            val index = current.indexOfFirst { it.address == domainDevice.address }
            if (index >= 0) {
                current[index] = domainDevice
            } else {
                current.add(domainDevice)
            }
            _discoveredDevices.value = current
        } catch (_: SecurityException) {
        }
    }

    @SuppressLint("MissingPermission")
    private fun mapToDomainDevice(
        device: BluetoothDevice,
        connectedA2dpAddresses: Set<String>
    ): BluetoothAudioDevice {
        val name = try { device.name ?: "Unknown Speaker" } catch (_: SecurityException) { "Unknown Speaker" }
        val address = device.address
        val bondState = when (device.bondState) {
            BluetoothDevice.BOND_BONDED -> BondState.BONDED
            BluetoothDevice.BOND_BONDING -> BondState.BONDING
            else -> BondState.NONE
        }

        val isConnected = connectedA2dpAddresses.contains(address)
        val isOutputActive = _activeOutputAddresses.value.contains(address)
        val brand = BrandDetector.detectBrand(name)
        val battery = batteryLevelMap[address]

        val deviceClassType = when (device.bluetoothClass?.deviceClass) {
            BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER -> "Loudspeaker"
            BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES -> "Headphones"
            BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET -> "Headset"
            BluetoothClass.Device.AUDIO_VIDEO_CAR_AUDIO -> "Car Audio"
            else -> "Audio Sink"
        }

        return BluetoothAudioDevice(
            name = name,
            address = address,
            bondState = bondState,
            connectionState = if (isConnected) ConnectionState.CONNECTED else ConnectionState.DISCONNECTED,
            isAudioOutputActive = isOutputActive,
            brand = brand,
            batteryLevel = battery,
            deviceClassType = deviceClassType,
            isBleAudio = false
        )
    }

    @SuppressLint("MissingPermission")
    private fun isAudioDevice(device: BluetoothDevice): Boolean {
        try {
            val name = device.name
            if (BrandDetector.isAudioDevice(name)) return true

            val btClass = device.bluetoothClass ?: return false
            val major = btClass.majorDeviceClass
            if (major == BluetoothClass.Device.Major.AUDIO_VIDEO) return true

            val deviceClass = btClass.deviceClass
            return deviceClass == BluetoothClass.Device.AUDIO_VIDEO_LOUDSPEAKER ||
                    deviceClass == BluetoothClass.Device.AUDIO_VIDEO_HEADPHONES ||
                    deviceClass == BluetoothClass.Device.AUDIO_VIDEO_WEARABLE_HEADSET
        } catch (_: SecurityException) {
            return false
        }
    }

    @SuppressLint("MissingPermission")
    override fun startDiscovery() {
        if (!BluetoothPermissionHelper.hasBluetoothPermissions(context)) return
        try {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
            _discoveredDevices.value = emptyList()
            bluetoothAdapter?.startDiscovery()
        } catch (_: SecurityException) {
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun stopDiscovery() {
        if (!BluetoothPermissionHelper.hasBluetoothPermissions(context)) return
        try {
            bluetoothAdapter?.cancelDiscovery()
            _isScanning.value = false
        } catch (_: SecurityException) {
            _isScanning.value = false
        }
    }

    @SuppressLint("MissingPermission")
    override fun createBond(address: String): Boolean {
        if (!BluetoothPermissionHelper.hasBluetoothPermissions(context)) return false
        return try {
            val device = bluetoothAdapter?.getRemoteDevice(address)
            device?.createBond() ?: false
        } catch (_: Exception) {
            false
        }
    }

    override fun getDevice(address: String): BluetoothAudioDevice? {
        return _pairedDevices.value.firstOrNull { it.address == address }
            ?: _discoveredDevices.value.firstOrNull { it.address == address }
    }

    override fun release() {
        try {
            context.unregisterReceiver(receiver)
        } catch (_: Exception) {}

        if (a2dpProfileProxy != null) {
            bluetoothAdapter?.closeProfileProxy(BluetoothProfile.A2DP, a2dpProfileProxy)
            a2dpProfileProxy = null
        }
    }
}
