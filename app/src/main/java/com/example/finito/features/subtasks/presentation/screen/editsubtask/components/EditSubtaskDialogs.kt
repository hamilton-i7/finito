package com.example.finito.features.subtasks.presentation.screen.editsubtask.components

import androidx.compose.runtime.Composable
import com.example.finito.core.presentation.components.dialogs.ErrorDialog
import com.example.finito.features.subtasks.presentation.screen.editsubtask.EditSubtaskEvent
import com.example.finito.features.subtasks.presentation.screen.editsubtask.EditSubtaskViewModel

@Composable
fun EditSubtaskDialogs(editSubtaskViewModel: EditSubtaskViewModel) {
    when (editSubtaskViewModel.dialogType) {
        is EditSubtaskEvent.DialogType.Error -> {
            val message = (editSubtaskViewModel.dialogType as EditSubtaskEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    editSubtaskViewModel.onEvent(EditSubtaskEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    editSubtaskViewModel.onEvent(EditSubtaskEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}