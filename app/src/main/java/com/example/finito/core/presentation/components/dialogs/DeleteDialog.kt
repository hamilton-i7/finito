package com.example.finito.core.presentation.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.ui.theme.finitoColors

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
        title = title@{
            if (title == null) return@title
            Text(text = stringResource(id = title))
        },
        text = { Text(text = stringResource(id = description)) },
        confirmButton = {
            FilledTonalButton(
                onClick = {
                    onConfirmClick()
                    onDismiss()
                },
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = finitoColors.primaryContainer,
                    contentColor = finitoColors.onPrimaryContainer
                )
            ) { Text(text = stringResource(id = confirmButtonText)) }
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(id = R.string.cancel))
            }
        }
    )
}