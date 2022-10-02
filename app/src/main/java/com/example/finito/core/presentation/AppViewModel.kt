package com.example.finito.core.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.usecase.SubtaskUseCases
import com.example.finito.features.tasks.domain.entity.CompletedTask
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val taskUseCases: TaskUseCases,
    private val subtaskUseCases: SubtaskUseCases,
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    var event by mutableStateOf<Event?>(null)
        private set

    fun onEvent(event: AppEvent) {
        when (event) {
            is AppEvent.UndoBoardChange -> onUndoBoardChange(event.board)
            is AppEvent.UndoTaskCompletedToggle -> onUndoTaskCompletedToggle(event.task)
            is AppEvent.UndoSubtaskCompletedToggle -> onUndoSubtaskCompletedToggle(event.subtask)
            is AppEvent.RecoverTask -> onRecoverTask(event.task)
            is AppEvent.RecoverSubtask -> onRecoverSubtask(event.subtask)
            AppEvent.RefreshBoard -> this.event = Event.RefreshBoard
        }
    }

    fun onClearEvent() {
        event = null
    }

    private fun onRecoverSubtask(subtask: Subtask) = viewModelScope.launch {
        when (subtaskUseCases.createSubtask(subtask)) {
            is Result.Error -> TODO(reason = "Implement error scenario")
            is Result.Success -> _eventFlow.emit(Event.RefreshBoard)
        }
    }

    private fun onRecoverTask(task: TaskWithSubtasks) = viewModelScope.launch {
        when (taskUseCases.createTask(task)) {
            is Result.Error -> TODO(reason = "Implement error scenario")
            is Result.Success -> _eventFlow.emit(Event.RefreshBoard)
        }
    }

    private fun onUndoTaskCompletedToggle(task: TaskWithSubtasks) = viewModelScope.launch {
        when (val result = taskUseCases.toggleTaskCompleted(task, undoingToggle = true)) {
            is Result.Error -> {
                if (result.message != ErrorMessages.NOT_FOUND) return@launch
                taskUseCases.createTask(task)
                _eventFlow.emitAll(
                    flow {
                        emit(Event.RefreshBoard)
                        emit(Event.RefreshTask)
                    }
                )
            }
            is Result.Success -> {
                _eventFlow.emitAll(
                    flow {
                        emit(Event.RefreshBoard)
                        emit(Event.RefreshTask)
                    }
                )
            }
        }
    }

    private fun onUndoSubtaskCompletedToggle(subtask: Subtask) = viewModelScope.launch {
        when (subtaskUseCases.updateSubtask(subtask)) {
            is Result.Error -> TODO(reason = "Implement error scenario")
            is Result.Success -> {
                _eventFlow.emitAll(
                    flow {
                        emit(Event.RefreshBoard)
                        emit(Event.RefreshTask)
                    }
                )
            }
        }
    }

    private fun onUndoBoardChange(originalBoard: DetailedBoard) = viewModelScope.launch {
        originalBoard.let {
            boardUseCases.updateBoard(
                BoardWithLabelsAndTasks(
                    board = it.board,
                    labels = it.labels,
                    tasks = it.tasks.map { task -> CompletedTask(task.task.completed) }
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