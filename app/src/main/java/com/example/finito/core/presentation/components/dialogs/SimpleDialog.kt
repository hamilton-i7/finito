package com.example.finito.core.presentation.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R

@Composable
fun SimpleDialog(
    onDismiss: () -> Unit,
    @StringRes description: Int,
    @StringRes confirmButtonText: Int,
    @StringRes dismissButtonText: Int? = R.string.cancel,
    onConfirmClick: () -> Unit = {},
    onDismissClick: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = { Text(text = stringResource(id = description)) },
        confirmButton = {
            TextButton(onClick = {
                onConfirmClick()
                onDismiss()
            }) {
                Text(text = stringResource(id = confirmButtonText))
            }
        },
        dismissButton = dismissButton@{
            if (dismissButtonText == null) return@dismissButton
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(id = dismissButtonText))
            }
        }
    )
}