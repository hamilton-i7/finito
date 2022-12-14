package com.example.finito.features.boards.presentation.screen.trash.components

import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.components.dialogs.DeleteDialog
import com.example.finito.core.presentation.components.dialogs.ErrorDialog
import com.example.finito.features.boards.presentation.screen.trash.TrashEvent
import com.example.finito.features.boards.presentation.screen.trash.TrashViewModel

@Composable
fun TrashDialogs(trashViewModel: TrashViewModel) {
    when (trashViewModel.dialogType) {
        is TrashEvent.DialogType.DeleteBoard -> {
            DeleteDialog(
                onDismiss = { trashViewModel.onEvent(TrashEvent.ShowDialog()) },
                description = R.string.delete_board_confirmation,
                onConfirmClick = {
                    trashViewModel.onEvent(TrashEvent.DeleteForever(
                        (trashViewModel.dialogType as TrashEvent.DialogType.DeleteBoard).board)
                    )
                },
                onDismissClick = {
                    trashViewModel.onEvent(TrashEvent.ShowDialog())
                }
            )
        }
        TrashEvent.DialogType.EmptyTrash -> {
            DeleteDialog(
                onDismiss = { trashViewModel.onEvent(TrashEvent.ShowDialog()) },
                title = R.string.empty_trash_confirmation_title,
                description = R.string.empty_trash_confirmation,
                onConfirmClick = { trashViewModel.onEvent(TrashEvent.EmptyTrash) },
                onDismissClick = {
                    trashViewModel.onEvent(TrashEvent.ShowDialog())
                }
            )
        }
        is TrashEvent.DialogType.Error -> {
            val message = (trashViewModel.dialogType as TrashEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    trashViewModel.onEvent(TrashEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    trashViewModel.onEvent(TrashEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}