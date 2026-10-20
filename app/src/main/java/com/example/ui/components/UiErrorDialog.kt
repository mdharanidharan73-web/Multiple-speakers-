package com.example.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.testTag
import com.example.model.UiError
import com.example.ui.theme.StatusAmber

@Composable
fun UiErrorDialog(
    error: UiError?,
    onDismiss: () -> Unit,
    onActionClicked: (UiError.RecoveryAction) -> Unit
) {
    if (error == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = "Alert",
                tint = StatusAmber
            )
        },
        title = {
            Text(text = error.title, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(text = error.message, style = MaterialTheme.typography.bodyMedium)
        },
        confirmButton = {
            Button(
                onClick = { onActionClicked(error.recoveryAction) },
                modifier = androidx.compose.ui.Modifier.testTag("error_dialog_action_btn")
            ) {
                Text(error.actionLabel)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = androidx.compose.ui.Modifier.testTag("error_dialog_dismiss_btn")
            ) {
                Text("Dismiss")
            }
        }
    )
}
