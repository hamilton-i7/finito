package com.example.finito.features.boards.presentation.trash.components

import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.dialogs.DeleteDialog
import com.example.finito.core.presentation.util.DialogType
import com.example.finito.features.boards.presentation.trash.TrashEvent
import com.example.finito.features.boards.presentation.trash.TrashViewModel

@Composable
fun TrashDialogs(
    appViewModel: AppViewModel,
    trashViewModel: TrashViewModel,
) {
    when (appViewModel.dialogType) {
        is DialogType.DeleteBoard -> {
            DeleteDialog(
                onDismiss = {
                    appViewModel.onEvent(
                        screen = Screen.Trash,
                        event = AppEvent.Trash.ShowDialog()
                    )
                },
                description = R.string.delete_board_confirmation,
                onConfirmClick = {
                    trashViewModel.onEvent(TrashEvent.DeleteForever(
                        (appViewModel.dialogType as DialogType.DeleteBoard).board)
                    )
                },
                onDismissClick = {
                    appViewModel.onEvent(
                        screen = Screen.Trash,
                        event = AppEvent.Trash.ShowDialog()
                    )
                }
            )
        }
        DialogType.EmptyTrash -> {
            DeleteDialog(
                onDismiss = {
                    appViewModel.onEvent(
                        screen = Screen.Trash,
                        event = AppEvent.Trash.ShowDialog()
                    )
                },
                title = R.string.empty_trash_confirmation_title,
                description = R.string.empty_trash_confirmation,
                onConfirmClick = { trashViewModel.onEvent(TrashEvent.EmptyTrash) },
                onDismissClick = {
                    appViewModel.onEvent(
                        screen = Screen.Trash,
                        event = AppEvent.Trash.ShowDialog()
                    )
                }
            )
        }
        null -> Unit
    }
}