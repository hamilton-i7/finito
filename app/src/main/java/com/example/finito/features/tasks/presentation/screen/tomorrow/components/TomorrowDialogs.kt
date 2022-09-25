package com.example.finito.features.tasks.presentation.screen.tomorrow.components

import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.components.dialogs.*
import com.example.finito.features.tasks.presentation.components.PriorityDialog
import com.example.finito.features.tasks.presentation.screen.tomorrow.TomorrowEvent
import com.example.finito.features.tasks.presentation.screen.tomorrow.TomorrowViewModel

@Composable
fun TomorrowDialogs(tomorrowViewModel: TomorrowViewModel) {
    when (tomorrowViewModel.dialogType) {
        TomorrowEvent.DialogType.DeleteCompleted -> {
            DeleteDialog(
                onDismiss = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog())
                },
                description = R.string.delete_completed_tomorrow_tasks_confirmation,
                onDismissClick = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog())
                },
                onConfirmClick = {
                    tomorrowViewModel.onEvent(TomorrowEvent.DeleteCompleted)
                }
            )
        }
        is TomorrowEvent.DialogType.Priority -> {
            PriorityDialog(
                onDismiss = { tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog()) },
                selectedPriority = tomorrowViewModel.selectedPriority,
                onPrioritySelect = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ChangeTaskPriority(priority = it))
                },
                onDismissClick = { tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog()) },
                onConfirmClick = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ChangeTaskPriorityConfirm(
                        (tomorrowViewModel.dialogType as TomorrowEvent.DialogType.Priority).task)
                    )
                }
            )
        }
        TomorrowEvent.DialogType.TaskDate -> {
            DatePickerDialog(
                initialDate = tomorrowViewModel.selectedDate,
                onDismiss = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog())
                },
                onConfirmClick = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ChangeDate(date = it))
                }
            )
        }
        TomorrowEvent.DialogType.TaskTime -> {
            TimePickerDialog(
                initialTime = tomorrowViewModel.selectedTime,
                onDismiss = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog())
                },
                onConfirmClick = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ChangeTime(time = it))
                }
            )
        }
        TomorrowEvent.DialogType.DiscardChanges -> {
            SimpleDialog(
                onDismiss = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog())
                },
                description = R.string.discard_changes_question,
                confirmButtonText = R.string.discard,
                onDismissClick = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog())
                },
                onConfirmClick = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowTaskDateTimeFullDialog(task = null))
                }
            )
        }
        is TomorrowEvent.DialogType.Error -> {
            val message = (tomorrowViewModel.dialogType as TomorrowEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    tomorrowViewModel.onEvent(TomorrowEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}