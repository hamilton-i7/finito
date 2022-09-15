package com.example.finito.features.boards.presentation.screen.addeditboard.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.finito.R
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.dialogs.DeleteDialog
import com.example.finito.features.boards.presentation.screen.addeditboard.AddEditBoardEvent
import com.example.finito.features.boards.presentation.screen.addeditboard.AddEditBoardViewModel

@Composable
fun AddEditBoardDialogs(
    addEditBoardViewModel: AddEditBoardViewModel,
    navController: NavController
) {
    when (addEditBoardViewModel.dialogType) {
        AddEditBoardEvent.DialogType.DeleteForever -> {
            DeleteDialog(
                onDismiss = { addEditBoardViewModel.onEvent(AddEditBoardEvent.ShowDialog()) },
                description = R.string.delete_board_confirmation,
                onConfirmClick = {
                    addEditBoardViewModel.onEvent(AddEditBoardEvent.DeleteForever)
                    navController.navigate(route = Screen.Trash.route) {
                        popUpTo(Screen.Trash.route) { inclusive = true }
                    }
                },
                onDismissClick = {
                    addEditBoardViewModel.onEvent(AddEditBoardEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}