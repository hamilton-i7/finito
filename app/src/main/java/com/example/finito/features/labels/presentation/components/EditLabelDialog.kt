package com.example.finito.features.labels.presentation.components

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.example.finito.R
import com.example.finito.core.presentation.util.TestTags
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.core.presentation.util.preview.ThemePreviews
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
            Icon(painter = painterResource(id = R.drawable.edit), contentDescription = null)
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
                ),
                modifier = Modifier.testTag(TestTags.RENAME_TEXT_FIELD)
            )
        },
        dismissButton = {
            TextButton(onClick = onDismissClick) {
                Text(text = stringResource(id = R.string.cancel))
            }
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirmClick,
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = finitoColors.primaryContainer,
                    contentColor = finitoColors.onPrimaryContainer
                ),
                modifier = Modifier.testTag(TestTags.DIALOG_CONFIRM_BUTTON)
            ) {
                Text(text = stringResource(id = R.string.rename))
            }
        },
        modifier = Modifier.testTag(TestTags.RENAME_LABEL_DIALOG)
    )
}

@ThemePreviews
@Composable
private fun EditLabelDialogPreview() {
    FinitoTheme {
        Surface {
            EditLabelDialog(
                nameState = TextFieldState.Default,
                onDismiss = {},
                onDismissClick = {},
                onConfirmClick = {}
            )
        }
    }
}