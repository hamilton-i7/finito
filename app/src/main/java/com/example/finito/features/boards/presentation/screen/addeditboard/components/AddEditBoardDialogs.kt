package com.example.finito.features.boards.presentation.screen.addeditboard.components

import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.components.dialogs.DeleteDialog
import com.example.finito.features.boards.presentation.screen.addeditboard.AddEditBoardEvent
import com.example.finito.features.boards.presentation.screen.addeditboard.AddEditBoardViewModel

@Composable
fun AddEditBoardDialogs(
    addEditBoardViewModel: AddEditBoardViewModel,
    onNavigateToTrash: () -> Unit = {},
) {
    when (addEditBoardViewModel.dialogType) {
        AddEditBoardEvent.DialogType.DeleteForever -> {
            DeleteDialog(
                onDismiss = { addEditBoardViewModel.onEvent(AddEditBoardEvent.ShowDialog()) },
                description = R.string.delete_board_confirmation,
                onConfirmClick = {
                    addEditBoardViewModel.onEvent(AddEditBoardEvent.DeleteForever)
                    onNavigateToTrash()
                },
                onDismissClick = {
                    addEditBoardViewModel.onEvent(AddEditBoardEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}