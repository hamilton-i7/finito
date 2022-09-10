package com.example.finito.features.tasks.presentation.datetime

import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.finito.R
import com.example.finito.core.presentation.HandleBackPress
import com.example.finito.core.presentation.components.bars.DialogTopBar
import com.example.finito.core.presentation.components.textfields.DateTextField
import com.example.finito.core.presentation.components.textfields.TimeTextField
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.util.formatted
import com.example.finito.features.tasks.domain.util.isCurrentYear
import com.example.finito.features.tasks.domain.util.toCurrentYearFormat
import com.example.finito.features.tasks.domain.util.toFullFormat
import com.example.finito.features.tasks.presentation.datetime.components.TaskDateTimeDialogs
import com.example.finito.ui.theme.FinitoTheme
import java.time.LocalDate
import java.time.LocalTime

private fun dataChanged(task: TaskWithSubtasks?, date: LocalDate?, time: LocalTime?): Boolean {
    return task?.task?.date?.isEqual(date) == false || task?.task?.time != time
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateTimeScreen(
    navController: NavController,
    taskDateTimeViewModel: TaskDateTimeViewModel = hiltViewModel(),
) {
    val task = taskDateTimeViewModel.task
    val locale = LocalConfiguration.current.locales[0]

    val date = taskDateTimeViewModel.date?.let {
        val today = LocalDate.now()
        if (it.isCurrentYear(today)) {
            it.toCurrentYearFormat(locale, complete = true)
        } else {
            it.toFullFormat(locale, complete = true)
        }
    } ?: ""
    val time = taskDateTimeViewModel.time?.formatted() ?: ""

    HandleBackPress {
        if (!dataChanged(task, taskDateTimeViewModel.date, taskDateTimeViewModel.time)) {
            navController.popBackStack()
            return@HandleBackPress
        }
        taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ShowDialog(
            type = TaskDateTimeEvent.DialogType.DiscardChanges
        ))
    }

    Scaffold(
        topBar = {
            DialogTopBar(
                title = R.string.edit_date_time,
                onCloseClick = onCloseClick@{
                    if (!dataChanged(task, taskDateTimeViewModel.date, taskDateTimeViewModel.time)) {
                        navController.popBackStack()
                        return@onCloseClick
                    }
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ShowDialog(
                        type = TaskDateTimeEvent.DialogType.DiscardChanges
                    ))
                },
                saveButtonEnabled = task != null,
                onSave = {
                    taskDateTimeViewModel.onEvent(TaskDateTimeEvent.SaveChanges)
                    navController.popBackStack()
                }
            )
        },
    ) { innerPadding ->
        if (taskDateTimeViewModel.showDialog) {
            TaskDateTimeDialogs(taskDateTimeViewModel, navController)
        }

        TaskDateTimeScreen(
            paddingValues = innerPadding,
            date = date,
            onDateFieldClick = {
                taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ShowDialog(
                    type = TaskDateTimeEvent.DialogType.TaskDate
                ))
            },
            time = time,
            onTimeFieldClick = {
                taskDateTimeViewModel.onEvent(TaskDateTimeEvent.ShowDialog(
                    type = TaskDateTimeEvent.DialogType.TaskTime
                ))
            },
        )
    }
}

@Composable
private fun TaskDateTimeScreen(
    paddingValues: PaddingValues = PaddingValues(),
    date: String = "",
    onDateFieldClick: () -> Unit = {},
    onDateRemove: () -> Unit = {},
    time: String = "",
    onTimeFieldClick: () -> Unit = {},
    onTimeRemove: () -> Unit = {},
) {
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp, horizontal = 16.dp)
        ) {
            DateTextField(
                date = date,
                onDateRemove = onDateRemove,
                onClick = onDateFieldClick,
                enabled = date.isNotEmpty()
            )
            TimeTextField(
                time = time,
                onTimeRemove = onTimeRemove,
                onClick = onTimeFieldClick,
                enabled = time.isNotEmpty()
            )
        }
    }
}

@Preview
@Composable
private fun TaskDateTimeScreenPreview() {
    FinitoTheme {
        TaskDateTimeScreen(
            date = "Sat, Sep 10",
            time = "10:45"
        )
    }
}