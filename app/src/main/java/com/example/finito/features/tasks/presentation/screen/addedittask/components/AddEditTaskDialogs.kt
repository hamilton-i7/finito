package com.example.finito.features.tasks.presentation.screen.addedittask.components

import androidx.compose.runtime.Composable
import com.example.finito.core.presentation.components.dialogs.DatePickerDialog
import com.example.finito.core.presentation.components.dialogs.ErrorDialog
import com.example.finito.core.presentation.components.dialogs.TimePickerDialog
import com.example.finito.features.tasks.presentation.screen.addedittask.AddEditTaskEvent
import com.example.finito.features.tasks.presentation.screen.addedittask.AddEditTaskViewModel

@Composable
fun AddEditTaskDialogs(addEditTaskViewModel: AddEditTaskViewModel) {
    when (addEditTaskViewModel.dialogType) {
        AddEditTaskEvent.DialogType.Date -> {
            DatePickerDialog(
                initialDate = addEditTaskViewModel.selectedDate,
                onDismiss = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog())
                },
                onConfirmClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeDate(date = it))
                }
            )
        }
        AddEditTaskEvent.DialogType.Time -> {
            TimePickerDialog(
                initialTime = addEditTaskViewModel.selectedTime,
                onDismiss = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog())
                },
                onConfirmClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ChangeTime(time = it))
                }
            )
        }
        is AddEditTaskEvent.DialogType.Error -> {
            val message = (addEditTaskViewModel.dialogType as AddEditTaskEvent.DialogType.Error).message
            ErrorDialog(
                message = message,
                onDismiss = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog())
                },
                onConfirmButtonClick = {
                    addEditTaskViewModel.onEvent(AddEditTaskEvent.ShowDialog())
                }
            )
        }
        null -> Unit
    }
}