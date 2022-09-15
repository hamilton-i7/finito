package com.example.finito.features.tasks.presentation.screen.datetime

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.presentation.Screen
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class TaskDateTimeViewModel @Inject constructor(
    private val taskUseCases: TaskUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var task by mutableStateOf<TaskWithSubtasks?>(null)
        private set

    var showDialog by mutableStateOf(false)
        private set

    var dialogType by mutableStateOf<TaskDateTimeEvent.DialogType?>(null)
        private set

    var date by mutableStateOf<LocalDate?>(null)
        private set

    var time by mutableStateOf<LocalTime?>(null)
        private set

    init {
        fetchTask()
    }

    fun onEvent(event: TaskDateTimeEvent) {
        when (event) {
            is TaskDateTimeEvent.ChangeDate -> changeTaskDate(event.date)
            is TaskDateTimeEvent.ChangeTime -> changeTaskTime(event.time)
            is TaskDateTimeEvent.ShowDialog -> onShowDialog(event.type)
            TaskDateTimeEvent.SaveChanges -> saveChanges()
            TaskDateTimeEvent.DiscardChanges -> resetTextFields()
        }
    }

    private fun saveChanges() = viewModelScope.launch {
        if (task!!.task.date?.isEqual(date) == true
            && task!!.task.time == time) return@launch

        with(task!!) {
            taskUseCases.updateTask(
                copy(task = task.copy(time = time, date = date))
            )
            resetTextFields()
        }
    }

    private fun changeTaskTime(time: LocalTime?) = viewModelScope.launch {
        if (task!!.task.time == time) return@launch
        this@TaskDateTimeViewModel.time = time
    }

    private fun changeTaskDate(date: LocalDate?) = viewModelScope.launch {
        if (task!!.task.date?.isEqual(date) == true) return@launch
        this@TaskDateTimeViewModel.date = date
    }

    private fun onShowDialog(dialogType: TaskDateTimeEvent.DialogType?) {
        dialogType?.let {
            showDialog = true
            this.dialogType = it
        } ?: run { showDialog = false }
    }

    private fun fetchTask() {
        savedStateHandle.get<Int>(Screen.TASK_ROUTE_ARGUMENT)?.let { taskId ->
            viewModelScope.launch {
                task = taskUseCases.findOneTask(taskId)
                date = task?.task?.date
                time = task?.task?.time
            }
        }
    }

    private fun resetTextFields() {
        date = null
        time = null
    }
}