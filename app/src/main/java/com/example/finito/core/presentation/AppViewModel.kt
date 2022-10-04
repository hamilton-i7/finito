package com.example.finito.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.usecase.SubtaskUseCases
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val taskUseCases: TaskUseCases,
    private val subtaskUseCases: SubtaskUseCases,
) : ViewModel() {

    private val _event = MutableStateFlow<Event?>(null)
    val event = _event.asStateFlow()

    fun onEvent(event: AppEvent) {
        when (event) {
            is AppEvent.UndoBoardChange -> onUndoBoardChange(event.board)
            is AppEvent.UndoTaskCompletedToggle -> onUndoTaskCompletedToggle(event.task)
            is AppEvent.UndoSubtaskCompletedToggle -> onUndoSubtaskCompletedToggle(event.subtask, event.task)
            is AppEvent.RecoverTask -> onRecoverTask(event.task)
            is AppEvent.RecoverSubtask -> onRecoverSubtask(event.subtask)
            AppEvent.RefreshBoard -> onRefreshBoard()
        }
    }

    private fun onRefreshBoard() = viewModelScope.launch {
        _event.value = Event.RefreshBoard
        delay(100)
        _event.value = null
    }

    private fun onRecoverSubtask(subtask: Subtask) = viewModelScope.launch {
        when (subtaskUseCases.createSubtask(subtask)) {
            is Result.Error -> TODO(reason = "Implement error scenario")
            is Result.Success -> _event.emit(Event.RefreshBoard)
        }
    }

    private fun onRecoverTask(task: TaskWithSubtasks) = viewModelScope.launch {
        when (taskUseCases.createTask(task)) {
            is Result.Error -> TODO(reason = "Implement error scenario")
            is Result.Success -> _event.emit(Event.RefreshBoard)
        }
    }

    private fun onUndoTaskCompletedToggle(task: TaskWithSubtasks) = viewModelScope.launch {
        val result = taskUseCases.toggleTaskCompleted(task, undoingToggle = true)
        if (result is Result.Error) {
            if (result.message != ErrorMessages.NOT_FOUND) return@launch
            taskUseCases.createTask(task)
        }
        _event.value = Event.RefreshBoard
        delay(100)
        _event.value = Event.RefreshTask
    }

    private fun onUndoSubtaskCompletedToggle(
        subtask: Subtask,
        task: Task,
    ) = viewModelScope.launch {
        val result = subtaskUseCases.updateSubtask(subtask).also {
            // Change related task completed state to its previous state
            if (!subtask.completed) return@also
            if (!task.completed) return@also
            taskUseCases.updateTask(task)
        }
        if (result is Result.Error) {
            if (result.message != ErrorMessages.NOT_FOUND) return@launch
            subtaskUseCases.createSubtask(subtask)
        }
        _event.value = Event.RefreshBoard
        delay(100)
        _event.value = Event.RefreshSubtask
        delay(100)
        _event.value = Event.RefreshTask
    }

    private fun onUndoBoardChange(originalBoard: DetailedBoard) = viewModelScope.launch {
        originalBoard.let {
            boardUseCases.updateBoard(
                BoardWithLabelsAndTasks(
                    board = it.board,
                    labels = it.labels,
                    tasks = it.tasks.map { task -> task.toCompletedTask() }
                )
            )
        }
    }

    sealed class Event {
        object RefreshBoard : Event()

        object RefreshTask : Event()

        object RefreshSubtask : Event()
    }
}