package com.example.finito.core.presentation.components

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R

@Composable
fun DeleteDialog(
    onDismiss: () -> Unit,
    @StringRes title: Int? = null,
    @StringRes description: Int,
    @StringRes confirmButtonText: Int = R.string.delete,
    onConfirmClick: () -> Unit = {},
    onDismissClick: () -> Unit = {}
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(imageVector = Icons.Outlined.ErrorOutline, contentDescription = null) },
        title = if (title != null) {
            { Text(text = stringResource(id = title)) }
        } else null,
        text = { Text(text = stringResource(id = description)) },
        confirmButton = {
            FilledTonalButton(onClick = {
                onConfirmClick()
                onDismiss()
            }) { Text(text = stringResource(id = confirmButtonText)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}