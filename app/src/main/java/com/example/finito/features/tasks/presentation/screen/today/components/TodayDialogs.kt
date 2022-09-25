package com.example.finito.features.tasks.presentation.screen.today.components

import androidx.compose.runtime.Composable
import com.example.finito.R
import com.example.finito.core.presentation.components.dialogs.*
import com.example.finito.features.tasks.presentation.components.PriorityDialog
import com.example.finito.features.tasks.presentation.screen.today.TodayEvent
import com.example.finito.features.tasks.presentation.screen.today.TodayViewModel

@Composable
fun TodayDialogs(todayViewModel: TodayViewModel) {
    when (todayViewModel.dialogType) {
        TodayEvent.DialogType.DeleteCompleted -> {
            DeleteDialog(
                onDismiss = {
                    todayViewModel.onEvent(TodayEvent.ShowDialog())
                },
                description = R.string.delete_completed_today_tasks_confirmation,
                onDismissClick = {
                    todayViewModel.onEvent(TodayEvent.ShowDialog())
                },
                onConfirmClick = {
                    todayViewModel.onEvent(TodayEvent.DeleteCompleted)
                }
            )
        }
        is TodayEvent.DialogType.Priority -> {
            PriorityDialog(
                onDismiss = { todayViewModel.onEvent(TodayEvent.ShowDialog()) },
                selectedPriority = todayViewModel.selectedPriority,
                onPrioritySelect = {
                    todayViewModel.onEvent(TodayEvent.ChangeTaskPriority(priority = it))
                },
                onDismissClick = { todayViewModel.onEvent(TodayEvent.ShowDialog()) },
                onConfirmClick = {
                    todayViewModel.onEvent(TodayEvent.ChangeTaskPriorityConfirm(
                        (todayViewModel.dialogType as TodayEvent.DialogType.Priority).task)
                    )
                }
            )
        }
        TodayEvent.DialogType.TaskDate -> {
            DatePickerDialog(
                initialDate = todayViewModel.selectedDate,
                onDismiss = {
                    todayViewModel.onEvent(TodayEvent.ShowDialog())
                },
                onConfirmClick = {
                    todayViewModel.onEvent(TodayEvent.ChangeDate(date = it))
                }
            )
        }
        TodayEvent.DialogType.TaskTime -> {
            TimePickerDialog(
                initialTime = todayViewModel.selectedTime,
                onDismiss = {
                    todayViewModel.onEvent(TodayEvent.ShowDialog())
                },
                onConfirmClick = {
                    todayViewModel.onEvent(TodayEvent.ChangeTime(time = it))
                }
            )
        }
        TodayEvent.DialogType.DiscardChanges -> {
            SimpleDialog(
                onDismiss = {
                    todayViewModel.onEvent(TodayEvent.ShowDialog())
                },
                description = R.string.discard_changes_question,
                confirmButtonText = R.string.discard,
                onDismissClick = {
                    todayViewModel.onEvent(TodayEvent.ShowDialog())
                },
                onConfirmClick = {
                    todayViewModel.onEvent(TodayEvent.ShowTaskDateTimeFullDialog(task = null))
                }
            )
        }
        is TodayEvent.DialogType.Error -> {
            val message = (todayViewModel.dialogType as TodayEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    todayViewModel.onEvent(TodayEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    todayViewModel.onEvent(TodayEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}