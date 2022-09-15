package com.example.finito.features.boards.presentation.board

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.Priority
import com.example.finito.core.presentation.Screen
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.boards.utils.DeactivateMode
import com.example.finito.features.tasks.domain.entity.CompletedTask
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.usecase.TaskUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class BoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val taskUseCases: TaskUseCases,
    private val preferences: SharedPreferences,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var boardState by mutableStateOf(BoardState.ACTIVE)
        private set

    var board by mutableStateOf<DetailedBoard?>(null)
        private set

    var showCompletedTasks by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.SHOW_COMPLETED_TASKS.name,
        true
    )); private set

    var showScreenMenu by mutableStateOf(false)
        private set

    var dialogType by mutableStateOf<BoardEvent.DialogType?>(null)
        private set

    var selectedPriority by mutableStateOf<Priority?>(null)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchBoard()
        fetchBoardState()
    }

    fun onEvent(event: BoardEvent) {
        when (event) {
            BoardEvent.ArchiveBoard -> onDeactivateBoard(DeactivateMode.ARCHIVE)
            BoardEvent.DeleteBoard -> onDeactivateBoard(DeactivateMode.DELETE)
            BoardEvent.RestoreBoard -> restoreBoard()
            is BoardEvent.ChangeTaskPriority -> selectedPriority = event.priority
            is BoardEvent.ChangeTaskPriorityConfirm -> onChangeTaskPriorityConfirm(event.task)
            is BoardEvent.ToggleTaskCompleted -> TODO()
            BoardEvent.DeleteCompletedTasks -> TODO()
            is BoardEvent.ShowScreenMenu -> showScreenMenu = event.show
            BoardEvent.ToggleCompletedTasksVisibility -> onShowCompletedTasksChange()
            is BoardEvent.ShowDialog -> onShowDialogChange(event.type)
            BoardEvent.EditBoard -> onEditBoard()
            is BoardEvent.EditTaskDateTime -> onEditTaskDateTime(event.taskId)
            BoardEvent.RefreshBoard -> {
                fetchBoard()
                fetchBoardState()
            }
        }
    }

    private fun onEditTaskDateTime(taskId: Int) = viewModelScope.launch {
        val route = "${Screen.TaskDateTime.prefix}/${taskId}"
        _eventFlow.emit(Event.Navigate(route = route))
    }

    private fun onEditBoard() = viewModelScope.launch {
        val route = "${Screen.EditBoard.prefix}/${board!!.board.boardId}?${Screen.BOARD_ROUTE_STATE_ARGUMENT}=${boardState.name}"
        _eventFlow.emit(Event.Navigate(route = route))
    }

    private fun onShowDialogChange(dialogType: BoardEvent.DialogType?) {
        this.dialogType = dialogType
        selectedPriority = if (dialogType is BoardEvent.DialogType.Priority) {
            dialogType.taskWithSubtasks.task.priority
        } else null
    }

    private fun onChangeTaskPriorityConfirm(task: TaskWithSubtasks) = viewModelScope.launch {
        if (task.task.priority == selectedPriority) return@launch
        taskUseCases.updateTask(
            TaskWithSubtasks(
                task = task.task.copy(priority = selectedPriority),
                subtasks = task.subtasks
            )
        ).also { fetchBoard() }
    }

    private fun onShowCompletedTasksChange() {
        showCompletedTasks = !showCompletedTasks
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.SHOW_COMPLETED_TASKS.name, showCompletedTasks)
            apply()
        }
    }

    private fun fetchBoard() {
        savedStateHandle.get<Int>(Screen.BOARD_ROUTE_ID_ARGUMENT)?.let { boardId ->
             viewModelScope.launch {
                boardUseCases.findOneBoard(boardId).also {
                    board = it
                }
            }
        }
    }

    private fun fetchBoardState() {
        savedStateHandle.get<String>(Screen.BOARD_ROUTE_STATE_ARGUMENT)?.let { state ->
            boardState = when (state) {
                BoardState.ARCHIVED.name -> BoardState.ARCHIVED
                BoardState.DELETED.name -> BoardState.DELETED
                else -> BoardState.ACTIVE
            }
        }
    }

    private fun restoreBoard() = viewModelScope.launch {
        board?.let {
            val restoredBoard = BoardWithLabelsAndTasks(
                board = it.board.copy(state = BoardState.ACTIVE, removedAt = null),
                labels = it.labels,
                tasks = it.tasks.map { task -> CompletedTask(completed = task.task.completed) }
            )
            boardUseCases.updateBoard(restoredBoard)
            _eventFlow.emit(Event.ShowSnackbar(
                message = R.string.board_was_restored,
                board = it
            ))
        }
    }

    private fun onDeactivateBoard(mode: DeactivateMode) = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            when (mode) {
                DeactivateMode.ARCHIVE -> {
                    BoardWithLabelsAndTasks(
                        board = board.copy(state = BoardState.ARCHIVED, removedAt = null),
                        labels = labels,
                        tasks = tasks.map { CompletedTask(completed = it.task.completed) }
                    ).let { boardUseCases.updateBoard(it) }
                    _eventFlow.emit(Event.Navigate(route = Screen.Home.route))
                    _eventFlow.emit(Event.ShowSnackbar(
                        message = R.string.board_archived,
                        board = this,
                    ))
                }
                DeactivateMode.DELETE -> {
                    BoardWithLabelsAndTasks(
                        board = board.copy(
                            state = BoardState.DELETED,
                            removedAt = LocalDateTime.now()
                        ),
                        labels = labels,
                        tasks = tasks.map { CompletedTask(completed = it.task.completed) }
                    ).let { boardUseCases.updateBoard(it) }
                    _eventFlow.emit(Event.ShowSnackbar(
                        message = R.string.board_moved_to_trash,
                        board = this@with
                    ))
                }
            }
        }
    }

    sealed class Event {
        data class ShowSnackbar(@StringRes val message: Int, val board: DetailedBoard) : Event()

        data class Navigate(val route: String) : Event()
    }
}