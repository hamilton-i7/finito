package com.example.finito.features.subtasks.presentation.screen.editsubtask

import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.domain.Result
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.subtasks.domain.usecase.SubtaskUseCases
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class EditSubtaskViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val taskUseCases: TaskUseCases,
    private val subtaskUseCases: SubtaskUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var subtask by mutableStateOf<Subtask?>(null)
        private set

    var boards by mutableStateOf<List<SimpleBoard>>(emptyList())
        private set

    var selectedBoard by mutableStateOf<SimpleBoard?>(null)
        private set

    var originalRelatedBoard: Board? = null
        private set

    var nameState by mutableStateOf(TextFieldState())
        private set

    var descriptionState by mutableStateOf(TextFieldState())
        private set

    var dialogType by mutableStateOf<EditSubtaskEvent.DialogType?>(null)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchSubtask()
        fetchBoards()
    }

    fun onEvent(event: EditSubtaskEvent) {
        when (event) {
            is EditSubtaskEvent.ChangeBoard -> selectedBoard = event.board
            is EditSubtaskEvent.ChangeName -> nameState = nameState.copy(value = event.name)
            is EditSubtaskEvent.ChangeDescription -> {
                descriptionState = descriptionState.copy(value = event.description)
            }
            EditSubtaskEvent.EditSubtask -> onEditSubtask()
            EditSubtaskEvent.DeleteSubtask -> onDeleteSubtask()
            EditSubtaskEvent.RefreshSubtask -> fetchSubtask()
            is EditSubtaskEvent.ShowDialog -> dialogType = event.type
            EditSubtaskEvent.ToggleCompleted -> onToggleCompleted()
        }
    }

    private fun onToggleCompleted() = viewModelScope.launch {
        if (subtask == null) return@launch
        with(subtask!!) {
            taskUseCases.findOneTask(taskId).let {
                if (it is Result.Error) return@launch
                val task = (it as Result.Success).data
                val completed = !this.completed
                val uncompletedSubtasksAmount = task.subtasks.count { subtask -> !subtask.completed }
                val updatedSubtask = copy(
                    completed = completed,
                    completedAt = if (completed) LocalDateTime.now() else null,
                    position = if (completed) null else uncompletedSubtasksAmount
                )
                when (subtaskUseCases.updateSubtask(updatedSubtask)) {
                    is Result.Error -> {
                        _eventFlow.emit(Event.ShowError(
                            error = R.string.update_subtask_error
                        ))
                    }
                    is Result.Success -> {
                        _eventFlow.emitAll(
                            flow {
                                emit(Event.Snackbar.UndoSubtaskChange(
                                    message = if (completed)
                                        R.string.subtask_marked_as_completed
                                    else
                                        R.string.subtask_marked_as_uncompleted,
                                    subtask = this@with
                                ))
                                emit(Event.NavigateBack)
                            }
                        )
                    }
                }
            }
        }
    }

    private fun onDeleteSubtask() = viewModelScope.launch {
        if (subtask == null) return@launch
        with(subtask!!) {
            when (subtaskUseCases.deleteSubtask(subtask = this)) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.delete_subtask_error
                    ))
                }
                is Result.Success -> {
                    _eventFlow.emitAll(
                        flow {
                            emit(Event.Snackbar.RecoverSubtask(
                                message = R.string.subtask_deleted,
                                subtask = this@with
                            ))
                            emit(Event.NavigateBack)
                        }
                    )
                }
            }
        }
    }

    private fun onEditSubtask() = viewModelScope.launch {
        if (subtask == null) return@launch
        with(subtask!!) {
            if (originalRelatedBoard!!.boardId != selectedBoard!!.boardId) {
                subtaskUseCases.deleteSubtask(subtask = this)
                boardUseCases.findOneBoard(selectedBoard!!.boardId).let {
                    if (it is Result.Error) return@launch
                    val board = (it as Result.Success).data
                    val uncompletedTasksAmount = board.tasks.count { task -> !task.task.completed }
                    val taskFromSubtask = TaskWithSubtasks(
                        task = Task(
                            boardId = selectedBoard!!.boardId,
                            name = nameState.value,
                            description = descriptionState.value.ifBlank { null },
                            completed = completed,
                            completedAt = completedAt,
                            createdAt = createdAt,
                            boardPosition = if (completed) null else uncompletedTasksAmount
                        )
                    )

                    when (taskUseCases.createTask(taskFromSubtask)) {
                        is Result.Error -> {
                            _eventFlow.emit(Event.ShowError(
                                error = R.string.update_subtask_error
                            ))
                        }
                        is Result.Success -> _eventFlow.emit(Event.NavigateBack)
                    }
                }
                return@launch
            }
            copy(
                name = nameState.value,
                description = descriptionState.value.ifBlank { null }
            ).let {
                when (subtaskUseCases.updateSubtask(it)) {
                    is Result.Error -> _eventFlow.emit(Event.ShowError(
                        error = R.string.update_subtask_error
                    ))
                    is Result.Success -> _eventFlow.emit(Event.NavigateBack)
                }
            }
        }
    }

    private fun fetchSubtask() {
        savedStateHandle.get<Int>(Screen.SUBTASK_ID_ARGUMENT)?.let { subtaskId ->
            viewModelScope.launch {
                when (val result = subtaskUseCases.findOneSubtask(subtaskId)) {
                    is Result.Error -> {
                        _eventFlow.emit(Event.ShowError(
                            error = R.string.find_subtask_error
                        ))
                    }
                    is Result.Success -> setupData(result.data)
                }
            }
        }
    }

    private suspend fun setupData(subtask: Subtask) {
        this.subtask = subtask
        fetchRelatedBoard()
        nameState = nameState.copy(value = subtask.name)
        if (subtask.description != null) {
            descriptionState = descriptionState.copy(value = subtask.description)
        }
    }

    private suspend fun fetchRelatedBoard() {
        savedStateHandle.get<Int>(Screen.BOARD_ID_ARGUMENT)?.let { boardId ->
            when (val result = boardUseCases.findOneBoard(boardId)) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.find_board_error
                    ))
                }
                is Result.Success -> {
                    val board = result.data.board
                    selectedBoard = SimpleBoard(boardId = board.boardId, name = board.name)
                    originalRelatedBoard = result.data.board
                }
            }
        }
    }

    private fun fetchBoards() = viewModelScope.launch {
        boardUseCases.findSimpleBoards().data.onEach { boards ->
            this@EditSubtaskViewModel.boards = boards
            val boardId = savedStateHandle.get<Int>(Screen.BOARD_ID_ARGUMENT) ?: return@onEach
            if (boardId == -1) return@onEach

            boards.first { it.boardId == boardId }.let { selectedBoard = it }
        }.launchIn(viewModelScope)
    }

    sealed class Event {
        data class ShowError(@StringRes val error: Int) : Event()

        object NavigateBack : Event()

        sealed class Snackbar : Event() {
            data class UndoSubtaskChange(
                @StringRes val message: Int,
                val subtask: Subtask,
            ) : Snackbar()

            data class RecoverSubtask(
                @StringRes val message: Int,
                val subtask: Subtask,
            ) : Snackbar()
        }
    }
}