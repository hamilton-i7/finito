package com.example.finito.features.tasks.presentation.screen.datetime.components

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.example.finito.R
import com.example.finito.core.presentation.components.dialogs.DatePickerDialog
import com.example.finito.core.presentation.components.dialogs.SimpleDialog
import com.example.finito.core.presentation.components.dialogs.TimePickerDialog
import com.example.finito.features.tasks.presentation.screen.datetime.TaskDateTimeEvent
import com.example.finito.features.tasks.presentation.screen.datetime.TaskDateTimeViewModel

@Composable
fun TaskDateTimeDialogs(
    taskDateTimeViewModel: TaskDateTimeViewModel,
    navController: NavController,
) {
    when (taskDateTimeViewModel.dialogType) {
        TaskDateTimeEvent.DialogType.TaskDate -> {
            DatePickerDialog(
                initialDate = taskDateTimeViewModel.date ?: taskDateTimeViewModel.task!!.task.date,
                onDismiss = {
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ShowDialog())
                },
                onConfirmClick = {
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ChangeDate(date = it))
                }
            )
        }
        TaskDateTimeEvent.DialogType.TaskTime -> {
            TimePickerDialog(
                initialTime = taskDateTimeViewModel.time ?: taskDateTimeViewModel.task!!.task.time,
                onDismiss = {
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ShowDialog())
                },
                onConfirmClick = {
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ChangeTime(time = it))
                }
            )
        }
        TaskDateTimeEvent.DialogType.DiscardChanges -> {
            SimpleDialog(
                onDismiss = {
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ShowDialog())
                },
                description = R.string.discard_changes_question,
                confirmButtonText = R.string.discard,
                onDismissClick = {
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ShowDialog())
                },
                onConfirmClick = {
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.DiscardChanges)
                    navController.navigateUp()
                }
            )
        }
        null -> Unit
    }
}