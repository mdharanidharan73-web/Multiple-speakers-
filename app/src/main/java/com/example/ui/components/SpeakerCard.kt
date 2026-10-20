package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.BatteryUnknown
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Speaker
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BluetoothAudioDevice
import com.example.model.BondState
import com.example.model.ConnectionState
import com.example.model.SpeakerSlot
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusEmerald
import com.example.ui.theme.StatusRose

@Composable
fun SpeakerCard(
    slot: SpeakerSlot,
    onSelectSpeakerClicked: () -> Unit,
    onClearSlotClicked: () -> Unit,
    onOpenSettingsClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val device = slot.device
    val isSlotAssigned = device != null

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("speaker_card_slot_${slot.slotNumber}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (device?.isAudioOutputActive == true) StatusEmerald else MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Slot badge and Brand
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(if (slot.slotNumber == 1) ElectricBlue else NeonViolet),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${slot.slotNumber}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Speaker Slot ${slot.slotNumber}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                if (isSlotAssigned) {
                    IconButton(
                        onClick = onClearSlotClicked,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("clear_slot_${slot.slotNumber}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear slot",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (device != null) {
                // Speaker Name and Brand Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speaker,
                        contentDescription = "Speaker",
                        tint = if (device.isAudioOutputActive) StatusEmerald else ElectricBlue,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = device.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = device.brand,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                text = "•  ${device.deviceClassType}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Three-State Status Badges Row (Paired ≠ Connected ≠ Active Output)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 1. Paired state
                    StatusPill(
                        label = device.bondState.label,
                        isActive = device.bondState == BondState.BONDED,
                        activeColor = ElectricBlue,
                        inactiveColor = MaterialTheme.colorScheme.outline
                    )

                    // 2. Connected state
                    StatusPill(
                        label = device.connectionState.label,
                        isActive = device.connectionState == ConnectionState.CONNECTED,
                        activeColor = StatusAmber,
                        inactiveColor = MaterialTheme.colorScheme.outline
                    )

                    // 3. Active Output state
                    StatusPill(
                        label = if (device.isAudioOutputActive) "Active Output" else "Not Outputting",
                        isActive = device.isAudioOutputActive,
                        activeColor = StatusEmerald,
                        inactiveColor = MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Battery Status (Strictly from system, never invented)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (device.batteryLevel != null) Icons.Default.BatteryFull else Icons.AutoMirrored.Filled.BatteryUnknown,
                        contentDescription = "Battery",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Battery: ${device.batteryDisplay}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bottom Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onSelectSpeakerClicked,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("change_speaker_${slot.slotNumber}"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Change", fontSize = 13.sp)
                    }

                    Button(
                        onClick = onOpenSettingsClicked,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("connect_speaker_${slot.slotNumber}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (device.connectionState == ConnectionState.CONNECTED) MaterialTheme.colorScheme.surfaceVariant else ElectricBlue
                        )
                    ) {
                        Text(
                            text = if (device.connectionState == ConnectionState.CONNECTED) "Settings" else "Connect",
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                // Empty state for slot
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Bluetooth,
                        contentDescription = "Bluetooth",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No Speaker Selected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onSelectSpeakerClicked,
                        modifier = Modifier.testTag("select_speaker_btn_${slot.slotNumber}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (slot.slotNumber == 1) ElectricBlue else NeonViolet
                        )
                    ) {
                        Text("Select Speaker ${slot.slotNumber}")
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusPill(
    label: String,
    isActive: Boolean,
    activeColor: Color,
    inactiveColor: Color
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = if (isActive) activeColor.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(
            width = 1.dp,
            color = if (isActive) activeColor else inactiveColor.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(if (isActive) activeColor else inactiveColor.copy(alpha = 0.5f))
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isActive) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
