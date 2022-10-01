package com.example.finito.features.labels.presentation.screen.label.components

import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.components.dialogs.DeleteDialog
import com.example.finito.core.presentation.components.dialogs.ErrorDialog
import com.example.finito.features.labels.presentation.components.EditLabelDialog
import com.example.finito.features.labels.presentation.screen.label.LabelEvent
import com.example.finito.features.labels.presentation.screen.label.LabelViewModel

@Composable
fun LabelDialogs(labelViewModel: LabelViewModel) {
    when (labelViewModel.dialogType) {
        LabelEvent.DialogType.Delete -> {
            DeleteDialog(
                onDismiss = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                title = R.string.delete_label_question,
                description = R.string.delete_label_confirmation,
                onDismissClick = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                onConfirmClick = {
                    labelViewModel.onEvent(LabelEvent.DeleteLabel)
                }
            )
        }
        LabelEvent.DialogType.Rename -> {
            EditLabelDialog(
                nameState = labelViewModel.labelNameState.copy(
                    onValueChange = {
                        labelViewModel.onEvent(LabelEvent.ChangeName(it))
                    }
                ),
                onDismiss = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                onDismissClick = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                onConfirmClick = {
                    labelViewModel.onEvent(LabelEvent.EditLabel)
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                }
            )
        }
        is LabelEvent.DialogType.Error -> {
            val message = (labelViewModel.dialogType as LabelEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    labelViewModel.onEvent(LabelEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}