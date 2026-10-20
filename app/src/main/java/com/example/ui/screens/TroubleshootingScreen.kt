package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Router
import androidx.compose.material.icons.filled.Sync
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.NeonViolet

@Composable
fun TroubleshootingScreen(
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClicked() }
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("troubleshooting_screen"),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClicked, modifier = Modifier.testTag("troubleshooting_back_btn")) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Bluetooth Troubleshooting",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Step-by-step connection recovery wizard",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Step 1: Cycle Bluetooth
        item {
            TroubleshootStepCard(
                step = 1,
                title = "Cycle Bluetooth Radio",
                desc = "Toggle Bluetooth off for 5 seconds and back on to clear stale profile proxies and reset the Android A2DP stack.",
                icon = Icons.Default.Bluetooth,
                buttonLabel = "Open Bluetooth Settings",
                onAction = {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }

        // Step 2: Forget and Re-pair
        item {
            TroubleshootStepCard(
                step = 2,
                title = "Forget & Re-Pair Speakers",
                desc = "If a speaker refuses to connect or report battery levels, unpair/forget it in Android Bluetooth Settings, then re-pair from scratch.",
                icon = Icons.Default.Sync,
                buttonLabel = "Android Paired Devices",
                onAction = {
                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }

        // Step 3: Check Manufacturer Dual-Audio Setting
        item {
            TroubleshootStepCard(
                step = 3,
                title = "Check Manufacturer Dual-Audio Setting",
                desc = "Samsung: Quick Settings > Media Output > check both speakers.\nXiaomi: Settings > Sound Assistant > Multi-sound.\nOnePlus: Bluetooth > Advanced > Dual-mode audio.",
                icon = Icons.Default.GraphicEq,
                buttonLabel = "Check Sound Settings",
                onAction = {
                    val intent = Intent(Settings.ACTION_SOUND_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )
        }

        // Step 4: Distance & 2.4 GHz RF Interference
        item {
            TroubleshootStepCard(
                step = 4,
                title = "Check Distance & 2.4 GHz Wi-Fi Interference",
                desc = "Bluetooth operates on 2.4 GHz. Streaming to two separate speakers requires high packet throughput. Keep both speakers within 5 meters with unobstructed line of sight, and prefer 5 GHz Wi-Fi on your phone.",
                icon = Icons.Default.Router,
                buttonLabel = null,
                onAction = null
            )
        }
    }
}

@Composable
private fun TroubleshootStepCard(
    step: Int,
    title: String,
    desc: String,
    icon: ImageVector,
    buttonLabel: String?,
    onAction: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = ElectricBlue.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "STEP $step",
                        color = ElectricBlue,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = desc,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            if (buttonLabel != null && onAction != null) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = onAction,
                    modifier = Modifier.fillMaxWidth().testTag("troubleshoot_step_${step}_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.padding(end = 6.dp))
                    Text(buttonLabel)
                }
            }
        }
    }
}
