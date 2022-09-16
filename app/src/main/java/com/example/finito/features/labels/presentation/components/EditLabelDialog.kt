package com.example.finito.features.labels.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.example.finito.R
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.ui.theme.FinitoTheme
import com.example.finito.ui.theme.finitoColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLabelDialog(
    nameState: TextFieldState,
    onDismiss: () -> Unit,
    onDismissClick: () -> Unit,
    onConfirmClick: () -> Unit,
) {
    val (name, onNameChange) = nameState

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(imageVector = Icons.Outlined.Edit, contentDescription = null)
        },
        title = { Text(text = stringResource(id = R.string.rename_label)) },
        text = {
            TextField(
                value = name,
                onValueChange = onNameChange,
                singleLine = true,
                label = { Text(text = stringResource(id = R.string.name)) },
                colors = TextFieldDefaults.textFieldColors(
                    containerColor = Color.Transparent,
                    unfocusedLabelColor = finitoColors.onSurfaceVariant.copy(alpha = 0.60f)
                )
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            FilledTonalButton(onClick = onConfirmClick, enabled = name.isNotBlank()) {
                Text(text = stringResource(id = R.string.rename))
            }
        }
    )
}

@Preview
@Composable
private fun EditLabelDialogPreview() {
    FinitoTheme {
        Surface {
            EditLabelDialog(
                nameState = TextFieldState(),
                onDismiss = {},
                onDismissClick = {},
                onConfirmClick = {}
            )
        }
    }
}