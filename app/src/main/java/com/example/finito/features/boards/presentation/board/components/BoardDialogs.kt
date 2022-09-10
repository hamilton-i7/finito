package com.example.finito.features.boards.presentation.board.components

import androidx.compose.runtime.Composable
import com.example.finito.features.boards.presentation.board.BoardEvent
import com.example.finito.features.boards.presentation.board.BoardViewModel
import com.example.finito.features.tasks.presentation.components.PriorityDialog

@Composable
fun BoardDialogs(
    boardViewModel: BoardViewModel,
) {
    when (boardViewModel.dialogType) {
        BoardEvent.DialogType.DeleteCompletedTasks -> TODO()
        is BoardEvent.DialogType.Priority -> {
            PriorityDialog(
                onDismiss = { boardViewModel.onEvent(BoardEvent.ShowDialog()) },
                selectedPriority = boardViewModel.selectedPriority,
                onPrioritySelect = {
                    boardViewModel.onEvent(BoardEvent.ChangeTaskPriority(priority = it))
                },
                onDismissClick = { boardViewModel.onEvent(BoardEvent.ShowDialog()) },
                onConfirmClick = {
                    boardViewModel.onEvent(BoardEvent.ChangeTaskPriorityConfirm(
                        (boardViewModel.dialogType as BoardEvent.DialogType.Priority).taskWithSubtasks)
                    )
                }
            )
        }
        null -> Unit
    }
}