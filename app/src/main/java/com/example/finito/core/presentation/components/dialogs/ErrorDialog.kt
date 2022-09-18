package com.example.finito.core.presentation.components.dialogs

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ReportProblem
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.util.preview.ThemePreviews
import com.example.finito.ui.theme.FinitoTheme

@Composable
fun ErrorDialog(
    @StringRes message: Int,
    onDismiss: () -> Unit = {},
    onConfirmButtonClick: () -> Unit = {},
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Outlined.ReportProblem, contentDescription = null)
        },
        text = { Text(text = stringResource(id = message)) },
        confirmButton = {
            TextButton(onClick = onConfirmButtonClick) {
                Text(text = stringResource(id = R.string.ok))
            }
        }
    )
}

@ThemePreviews
@Composable
private fun ErrorDialogPreview() {
    FinitoTheme {
        Surface {
            ErrorDialog(message = R.string.delete_label_error)
        }
    }
}