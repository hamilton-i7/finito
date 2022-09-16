package com.example.finito.features.boards.presentation.screen.board.components

import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.components.dialogs.DatePickerDialog
import com.example.finito.core.presentation.components.dialogs.SimpleDialog
import com.example.finito.core.presentation.components.dialogs.TimePickerDialog
import com.example.finito.features.boards.presentation.screen.board.BoardEvent
import com.example.finito.features.boards.presentation.screen.board.BoardViewModel
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
        BoardEvent.DialogType.DiscardChanges -> {
            SimpleDialog(
                onDismiss = {
                    boardViewModel.onEvent(BoardEvent.ShowDialog())
                },
                description = R.string.discard_changes_question,
                confirmButtonText = R.string.discard,
                onDismissClick = {
                    boardViewModel.onEvent(BoardEvent.ShowDialog())
                },
                onConfirmClick = {
                    boardViewModel.onEvent(BoardEvent.ShowTaskDateTimeFullDialog(task = null))
                }
            )
        }
        BoardEvent.DialogType.TaskDate -> {
            DatePickerDialog(
                initialDate = boardViewModel.selectedDate,
                onDismiss = {
                    boardViewModel.onEvent(BoardEvent.ShowDialog())
                },
                onConfirmClick = {
                    boardViewModel.onEvent(BoardEvent.ChangeTaskDate(date = it))
                }
            )
        }
        BoardEvent.DialogType.TaskTime -> {
            TimePickerDialog(
                initialTime = boardViewModel.selectedTime,
                onDismiss = {
                    boardViewModel.onEvent(BoardEvent.ShowDialog())
                },
                onConfirmClick = {
                    boardViewModel.onEvent(BoardEvent.ChangeTaskTime(time = it))
                }
            )
        }
        null -> Unit
    }
}