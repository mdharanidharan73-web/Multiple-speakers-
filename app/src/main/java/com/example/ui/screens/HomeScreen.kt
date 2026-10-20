package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BluetoothSearching
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SettingsBluetooth
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetooth.BrandDetector
import com.example.model.BluetoothAudioDevice
import com.example.model.BondState
import com.example.model.ConnectionState
import com.example.ui.components.DualOutputAuditDialog
import com.example.ui.components.SpeakerCard
import com.example.ui.components.UiErrorDialog
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusEmerald
import com.example.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    onNavigateToLibrary: () -> Unit,
    onNavigateToCompatibility: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by homeViewModel.uiState.collectAsState()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // App Title & Tagline
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "DUALSYNC AUDIO",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.5.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\"One Song. Multiple Speakers.\"",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ElectricBlue,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // Live Dual Audio Status Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = if (uiState.isDualOutputActive) StatusEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (uiState.isDualOutputActive) StatusEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (uiState.isDualOutputActive) StatusEmerald else StatusAmber)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (uiState.isDualOutputActive) "Dual Output Verified Active" else "Single/Default Audio Output",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = if (uiState.isDualOutputActive) StatusEmerald else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (uiState.isDualOutputActive) {
                                "Active on ${uiState.activeBtOutputCount} Bluetooth sinks simultaneously."
                            } else {
                                "Standard Android routes audio to 1 sink. Tap 'Check Support' for OEM multi-audio."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Speaker Slot 1
        item {
            SpeakerCard(
                slot = uiState.slot1,
                onSelectSpeakerClicked = { homeViewModel.openDevicePicker(1) },
                onClearSlotClicked = { homeViewModel.clearSlot(1) },
                onOpenSettingsClicked = {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }

        // Speaker Slot 2
        item {
            SpeakerCard(
                slot = uiState.slot2,
                onSelectSpeakerClicked = { homeViewModel.openDevicePicker(2) },
                onClearSlotClicked = { homeViewModel.clearSlot(2) },
                onOpenSettingsClicked = {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }

        // The Four Explicit Action Buttons from User Specification
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Controls & Diagnostics",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.SemiBold
                )

                // Button 1: Scan Bluetooth Devices
                Button(
                    onClick = { homeViewModel.openDevicePicker(1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_scan_bluetooth_devices"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Scan Bluetooth Devices", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                // Button 2: Check Dual Audio Support
                Button(
                    onClick = { homeViewModel.checkDualAudioSupport() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_check_dual_audio_support"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                ) {
                    Icon(Icons.Default.GraphicEq, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Check Dual Audio Support", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                // Button 3: Open Audio Output Settings
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_open_audio_output_settings"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.SettingsBluetooth, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Open Audio Output Settings", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                // Button 4: Play Music (Play on Both Speakers)
                Button(
                    onClick = {
                        homeViewModel.playOnBothSpeakers()
                        onNavigateToLibrary()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("btn_play_music"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (uiState.isDualOutputActive) StatusEmerald else ElectricBlue
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (uiState.isDualOutputActive) "Play on Both Speakers" else "Play Music",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    // Device Picker Modal Bottom Sheet
    if (uiState.showDevicePickerModal) {
        ModalBottomSheet(
            onDismissRequest = { homeViewModel.closeDevicePicker() },
            sheetState = bottomSheetState,
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            DevicePickerContent(
                targetSlot = uiState.pickingSlotNumber,
                pairedDevices = uiState.pairedDevices,
                discoveredDevices = uiState.discoveredDevices,
                isScanning = uiState.isScanning,
                onStartScan = { homeViewModel.startDiscovery() },
                onRefresh = { homeViewModel.refreshDevices() },
                onSelectDevice = { device ->
                    homeViewModel.selectSpeakerForSlot(device, uiState.pickingSlotNumber)
                },
                onPairDevice = { device ->
                    homeViewModel.pairDevice(device.address)
                },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }
    }

    // Dual Output Audit Dialog
    if (uiState.showAuditDialog) {
        DualOutputAuditDialog(
            auditResult = uiState.auditResult,
            onDismiss = { homeViewModel.dismissAuditDialog() },
            onOpenSettingsClicked = {
                val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(intent)
            },
            onViewCompatibilityClicked = {
                homeViewModel.dismissAuditDialog()
                onNavigateToCompatibility()
            }
        )
    }

    // UiError Dialog
    uiState.currentError?.let { err ->
        UiErrorDialog(
            error = err,
            onDismiss = { homeViewModel.dismissError() },
            onActionClicked = { action ->
                homeViewModel.executeRecoveryAction(context, action)
            }
        )
    }
}

@Composable
private fun DevicePickerContent(
    targetSlot: Int,
    pairedDevices: List<BluetoothAudioDevice>,
    discoveredDevices: List<BluetoothAudioDevice>,
    isScanning: Boolean,
    onStartScan: () -> Unit,
    onRefresh: () -> Unit,
    onSelectDevice: (BluetoothAudioDevice) -> Unit,
    onPairDevice: (BluetoothAudioDevice) -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Assign Speaker to Slot $targetSlot",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Select an audio device",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row {
                IconButton(onClick = onRefresh, modifier = Modifier.testTag("refresh_devices_btn")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                }
                IconButton(onClick = onOpenSettings, modifier = Modifier.testTag("open_bt_settings_btn")) {
                    Icon(Icons.Default.Settings, contentDescription = "Bluetooth Settings")
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Scan Action button
        Button(
            onClick = onStartScan,
            enabled = !isScanning,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("start_scan_btn"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
        ) {
            if (isScanning) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scanning Nearby Devices...")
            } else {
                Icon(Icons.AutoMirrored.Filled.BluetoothSearching, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Scan for Nearby Speakers")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(350.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Paired Devices
            item {
                Text(
                    text = "PAIRED AUDIO DEVICES (${pairedDevices.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (pairedDevices.isEmpty()) {
                item {
                    Text(
                        text = "No paired Bluetooth audio devices found. Pair a speaker in Android Settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(pairedDevices) { device ->
                    DeviceRowItem(
                        device = device,
                        isDiscovered = false,
                        onSelect = { onSelectDevice(device) },
                        onPair = { onPairDevice(device) }
                    )
                }
            }

            // Discovered Devices
            item {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "DISCOVERED NEARBY DEVICES (${discoveredDevices.size})",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (discoveredDevices.isEmpty()) {
                item {
                    Text(
                        text = if (isScanning) "Searching for speakers..." else "Tap 'Scan for Nearby Speakers' above.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            } else {
                items(discoveredDevices) { device ->
                    DeviceRowItem(
                        device = device,
                        isDiscovered = true,
                        onSelect = { onSelectDevice(device) },
                        onPair = { onPairDevice(device) }
                    )
                }
            }
        }
    }
}

@Composable
private fun DeviceRowItem(
    device: BluetoothAudioDevice,
    isDiscovered: Boolean,
    onSelect: () -> Unit,
    onPair: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
            .testTag("device_item_${device.address}"),
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
                imageVector = Icons.Default.Speaker,
                contentDescription = null,
                tint = ElectricBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = device.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${device.brand} • ${device.statusSummary}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (device.bondState != BondState.BONDED) {
                OutlinedButton(
                    onClick = onPair,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("Pair", fontSize = 12.sp)
                }
            } else {
                Button(
                    onClick = onSelect,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                ) {
                    Text("Assign", fontSize = 12.sp)
                }
            }
        }
    }
}
