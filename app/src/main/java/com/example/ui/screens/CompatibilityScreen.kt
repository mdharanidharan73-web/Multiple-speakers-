package com.example.ui.screens

import android.os.Build
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Cable
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusCyan
import com.example.ui.theme.StatusEmerald

@Composable
fun CompatibilityScreen(
    modifier: Modifier = Modifier
) {
    val manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("compatibility_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Header
        item {
            Column {
                Text(
                    text = "Compatibility & Alternatives",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Proven ways to play across two Bluetooth speakers",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Truth Banner
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = ElectricBlue.copy(alpha = 0.12f),
                border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = ElectricBlue,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Standard Android allows third-party apps to stream to only 1 Bluetooth A2DP device. The three methods below represent genuine, tested solutions.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Mode A: Native Dual Audio (OEM / Auracast)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(ElectricBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhoneAndroid, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Mode A: Native Dual Audio", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Built into Samsung, Xiaomi & OnePlus", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "If your phone hardware and vendor OS support dual-streaming, Android's system audio router handles both Bluetooth speakers directly.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Step by step guides
                    StepItem(
                        number = "1",
                        title = "Samsung OneUI (Galaxy S / Z / A series)",
                        desc = "Connect both speakers in Bluetooth Settings > Swipe down notification shade > Tap 'Media Output' > Select both connected speakers."
                    )
                    StepItem(
                        number = "2",
                        title = "Xiaomi MIUI / HyperOS",
                        desc = "Open Settings > Sound & Vibration > Sound Assistant > Enable 'Adjust media sound in multiple apps' or multi-output."
                    )
                    StepItem(
                        number = "3",
                        title = "LE Audio Auracast (Android 13+)",
                        desc = "If both speakers support Bluetooth 5.2+ LC3 broadcast, use Android 13/14 Auracast broadcast in Bluetooth settings."
                    )
                }
            }
        }

        // Mode B: External Dual Bluetooth Transmitter
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(NeonViolet),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Cable, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(text = "Mode B: Dual Bluetooth Transmitter", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text(text = "Universal 100% hardware compatible", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "An inexpensive physical dual-link Bluetooth transmitter (e.g. 1Mii, TaoTronics, UGREEN) connects to your phone's 3.5mm jack or USB-C DAC.",
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    StepItem(
                        number = "1",
                        title = "Connect Transmitter to Phone",
                        desc = "Plug transmitter into your phone via 3.5mm headphone jack or USB-C to 3.5mm adapter."
                    )
                    StepItem(
                        number = "2",
                        title = "Pair Speaker 1 & Speaker 2 to Transmitter",
                        desc = "Put transmitter in TX pairing mode. Pair Speaker 1 (e.g. boAt). Double press pair button to link Speaker 2 (e.g. Sony)."
                    )
                    StepItem(
                        number = "3",
                        title = "Latency Caveat & aptX-LL",
                        desc = "Choose a transmitter supporting aptX-LL (Low Latency). Standard SBC transmitters introduce 150-200ms delay."
                    )
                }
            }
        }

        // Mode C: Wi-Fi Synchronized Playback (Architecture Only)
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(StatusCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, tint = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Mode C: Wi-Fi Sync Playback", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(shape = RoundedCornerShape(4.dp), color = StatusAmber.copy(alpha = 0.2f)) {
                                    Text(
                                        text = "Architecture Only",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StatusAmber,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(text = "Multi-device wireless clock sync", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "IMPORTANT: Wi-Fi synchronized playback requires TWO separate Android receiver phones, each physically paired to one speaker. It does NOT enable a single phone to connect to two Bluetooth speakers directly.",
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusAmber
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    StepItem(
                        number = "A",
                        title = "NTP-Style Clock Synchronization",
                        desc = "Sender device broadcasts clock synchronization packets. Receiver devices compute round-trip time (RTT) and phase-lock their local AudioTrack clock."
                    )
                    StepItem(
                        number = "B",
                        title = "Jitter Buffers & Resampling",
                        desc = "Each receiver maintains an adaptive 250ms circular ring buffer with fractional linear interpolation to eliminate drift."
                    )
                    StepItem(
                        number = "C",
                        title = "Transport Protocol",
                        desc = "RTP over UDP or WebSocket audio streaming with presentation timestamps (PTS)."
                    )
                }
            }
        }
    }
}

@Composable
private fun StepItem(number: String, title: String, desc: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
