package com.example.finito.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.domain.ErrorMessages
import com.example.finito.core.domain.Result
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
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
) : ViewModel() {

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEvent(event: AppEvent) {
        when (event) {
            is AppEvent.UndoBoardChange -> onUndoBoardChange(event.board)
            is AppEvent.UndoTaskChange -> onUndoTaskChange(event.task)
            is AppEvent.RecoverTask -> onRecoverTask(event.task)
        }
    }

    private fun onRecoverTask(task: TaskWithSubtasks) = viewModelScope.launch {
        when (taskUseCases.createTask(task)) {
            is Result.Error -> TODO()
            is Result.Success -> _eventFlow.emit(Event.RefreshBoard)
        }
    }

    private fun onUndoTaskChange(task: TaskWithSubtasks) = viewModelScope.launch {
        when (val result = taskUseCases.updateTask(task)) {
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
    }
}