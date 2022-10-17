package com.example.finito.features.tasks.presentation.screen.urgent.components

import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.components.dialogs.*
import com.example.finito.features.tasks.presentation.components.PriorityDialog
import com.example.finito.features.tasks.presentation.screen.urgent.UrgentEvent
import com.example.finito.features.tasks.presentation.screen.urgent.UrgentViewModel

@Composable
fun UrgentDialogs(urgentViewModel: UrgentViewModel) {
    when (urgentViewModel.dialogType) {
        UrgentEvent.DialogType.DeleteCompleted -> {
            DeleteDialog(
                onDismiss = {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog())
                },
                description = R.string.delete_completed_urgent_tasks_confirmation,
                onDismissClick = {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog())
                },
                onConfirmClick = {
                    urgentViewModel.onEvent(UrgentEvent.DeleteCompleted)
                }
            )
        }
        is UrgentEvent.DialogType.Priority -> {
            PriorityDialog(
                onDismiss = { urgentViewModel.onEvent(UrgentEvent.ShowDialog()) },
                selectedPriority = urgentViewModel.selectedPriority,
                onPrioritySelect = {
                    urgentViewModel.onEvent(UrgentEvent.ChangeTaskPriority(priority = it))
                },
                onDismissClick = { urgentViewModel.onEvent(UrgentEvent.ShowDialog()) },
                onConfirmClick = {
                    urgentViewModel.onEvent(UrgentEvent.ChangeTaskPriorityConfirm(
                        (urgentViewModel.dialogType as UrgentEvent.DialogType.Priority).task)
                    )
                }
            )
        }
        UrgentEvent.DialogType.TaskDate -> {
            DatePickerDialog(
                initialDate = urgentViewModel.selectedDate,
                onDismiss = {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog())
                },
                onConfirmClick = {
                    urgentViewModel.onEvent(UrgentEvent.ChangeDate(date = it))
                }
            )
        }
        UrgentEvent.DialogType.TaskTime -> {
            TimePickerDialog(
                initialTime = urgentViewModel.selectedTime,
                onDismiss = {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog())
                },
                onConfirmClick = {
                    urgentViewModel.onEvent(UrgentEvent.ChangeTime(time = it))
                }
            )
        }
        UrgentEvent.DialogType.DiscardChanges -> {
            SimpleDialog(
                onDismiss = {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog())
                },
                description = R.string.discard_changes_question,
                confirmButtonText = R.string.discard,
                onDismissClick = {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog())
                },
                onConfirmClick = {
                    urgentViewModel.onEvent(UrgentEvent.ShowTaskDateTimeFullDialog(task = null))
                }
            )
        }
        is UrgentEvent.DialogType.Error -> {
            val message = (urgentViewModel.dialogType as UrgentEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    urgentViewModel.onEvent(UrgentEvent.ShowDialog())
                }
            )
        }
        UrgentEvent.DialogType.CreateBoard -> {
            SimpleDialog(
                onDismiss = { urgentViewModel.onEvent(UrgentEvent.ShowDialog()) },
                description = R.string.create_board_first,
                dismissButtonText = R.string.cancel,
                onDismissClick = { urgentViewModel.onEvent(UrgentEvent.ShowDialog()) },
                confirmButtonText = R.string.create,
                onConfirmClick = { urgentViewModel.onEvent(UrgentEvent.CreateBoard) },
            )
        }
        null -> Unit
    }
}