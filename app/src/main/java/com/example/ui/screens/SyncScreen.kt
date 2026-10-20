package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SpatialAudio
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AudioRouteInfo
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusEmerald
import com.example.ui.viewmodel.CodecLatencyInfo
import com.example.ui.viewmodel.SyncViewModel

@Composable
fun SyncScreen(
    syncViewModel: SyncViewModel,
    modifier: Modifier = Modifier
) {
    val activeRoutes by syncViewModel.activeRoutes.collectAsState()
    val activeBtRoutes by syncViewModel.activeBluetoothRoutes.collectAsState()
    val isTonePlaying by syncViewModel.isTonePlaying.collectAsState()
    val statusMessage by syncViewModel.statusMessage.collectAsState()

    val isDualActive = activeBtRoutes.size >= 2

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("sync_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Audio Sync & Routes",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Live hardware audio routing telemetry",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = { syncViewModel.refresh() }, modifier = Modifier.testTag("sync_refresh_btn")) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh Routes")
                }
            }
        }

        // Live Sync Status Card
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isDualActive) StatusEmerald.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
                ),
                border = androidx.compose.foundation.BorderStroke(
                    width = 1.dp,
                    color = if (isDualActive) StatusEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDualActive) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isDualActive) StatusEmerald else StatusAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (isDualActive) "Dual Output Verified Active" else "Single Sink Active (${activeBtRoutes.size} BT Speaker)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = if (isDualActive) StatusEmerald else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isDualActive) {
                            "Android system is streaming audio simultaneously to multiple Bluetooth sinks: ${activeBtRoutes.joinToString { it.productName }}."
                        } else {
                            "Third-party apps cannot independently buffer separate A2DP sinks. Latency sliders are only genuine when OEM or LE Audio is verified."
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Active Output Devices List
        item {
            Text(
                text = "ACTIVE SYSTEM OUTPUT ROUTES (${activeRoutes.size})",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(activeRoutes) { route ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (route.isBluetooth) ElectricBlue.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                            .background(if (route.isBluetooth) StatusEmerald else ElectricBlue)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = route.productName,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${route.typeName} • Sink ID: ${route.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Diagnostic Sound Test Section
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "BUILT-IN HARDWARE TONE TESTS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Channel & Latency Calibration",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Play synthetic PCM tones to verify which speaker is receiving audio.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (statusMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = NeonViolet.copy(alpha = 0.2f),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = statusMessage!!,
                                modifier = Modifier.padding(10.dp),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = NeonViolet
                            )
                        }
                    }

                    // Test Buttons Grid
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { syncViewModel.playConcertPitchA4() },
                                modifier = Modifier.weight(1f).testTag("btn_tone_440"),
                                enabled = !isTonePlaying,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("440 Hz (A4)", fontSize = 12.sp)
                            }
                            OutlinedButton(
                                onClick = { syncViewModel.playCalibrationBeep() },
                                modifier = Modifier.weight(1f).testTag("btn_tone_1000"),
                                enabled = !isTonePlaying,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("1000 Hz Beep", fontSize = 12.sp)
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { syncViewModel.playLeftChannelTest() },
                                modifier = Modifier.weight(1f).testTag("btn_tone_left"),
                                enabled = !isTonePlaying,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue)
                            ) {
                                Text("Left (Speaker 1)", fontSize = 12.sp)
                            }
                            Button(
                                onClick = { syncViewModel.playRightChannelTest() },
                                modifier = Modifier.weight(1f).testTag("btn_tone_right"),
                                enabled = !isTonePlaying,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonViolet)
                            ) {
                                Text("Right (Speaker 2)", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Bluetooth Codec Latency Breakdown
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "BLUETOOTH CODEC LATENCY REFERENCE",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        items(syncViewModel.codecLatencyGuide) { codec ->
            CodecCard(codec = codec)
        }
    }
}

@Composable
private fun CodecCard(codec: CodecLatencyInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = codec.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = ElectricBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = codec.latencyRangeMs,
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = codec.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Sync Impact: ${codec.dualAudioFeasibility}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp
            )
        }
    }
}
