package com.example.finito.features.tasks.presentation.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.finito.R
import com.example.finito.core.presentation.components.bars.DialogTopBar
import com.example.finito.core.presentation.components.textfields.DateTextField
import com.example.finito.core.presentation.components.textfields.TimeTextField
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.util.formatted
import com.example.finito.features.tasks.domain.util.isCurrentYear
import com.example.finito.features.tasks.domain.util.toCurrentYearFormat
import com.example.finito.features.tasks.domain.util.toFullFormat
import com.example.finito.ui.theme.FinitoTheme
import java.time.LocalDate
import java.time.LocalTime

private fun dataChanged(task: TaskWithSubtasks?, date: LocalDate?, time: LocalTime?): Boolean {
    return task?.task?.date?.isEqual(date) == false || task?.task?.time != time
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDateTimeFullDialog(
    task: TaskWithSubtasks,
    date: LocalDate?,
    onDateFieldClick: () -> Unit = {},
    onDateRemove: () -> Unit = {},
    time: LocalTime?,
    onTimeFieldClick: () -> Unit = {},
    onTimeRemove: () -> Unit = {},
    onCloseClick: () -> Unit = {},
    onSaveClick: () -> Unit = {},
    onAlertChangesMade: () -> Unit = {},
) {
    val locale = LocalConfiguration.current.locales[0]

    val formattedDate = date?.let {
        val today = LocalDate.now()
        if (it.isCurrentYear(today)) {
            it.toCurrentYearFormat(locale, complete = true)
        } else {
            it.toFullFormat(locale, complete = true)
        }
    } ?: ""
    val formattedTime = time?.formatted() ?: ""

    BackHandler {
        if (!dataChanged(task, date, time)) {
            onCloseClick()
            return@BackHandler
        }
        onAlertChangesMade()
    }

    Scaffold(
        topBar = {
            DialogTopBar(
                title = R.string.edit_date_time,
                onCloseClick = onCloseClick@{
                    if (!dataChanged(task, date, time)) {
                        onCloseClick()
                        return@onCloseClick
                    }
                    onAlertChangesMade()
                },
                saveButtonEnabled = date != null,
                onSave = {
                    onSaveClick()
                    onCloseClick()
                }
            )
        },
    ) { innerPadding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 16.dp)
            ) {
                DateTextField(
                    date = formattedDate,
                    onDateRemove = onDateRemove,
                    onClick = onDateFieldClick,
                    enabled = formattedDate.isNotEmpty()
                )
                TimeTextField(
                    time = formattedTime,
                    onTimeRemove = onTimeRemove,
                    onClick = onTimeFieldClick,
                    enabled = formattedDate.isNotEmpty()
                )
            }
        }
    }
}

@Preview
@Composable
private fun TaskDateTimeScreenPreview() {
    FinitoTheme {
        TaskDateTimeFullDialog(
            date = LocalDate.now(),
            time = LocalTime.now(),
            task = TaskWithSubtasks.dummyTasks.random()
        )
    }
}