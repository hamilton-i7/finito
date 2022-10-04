package com.example.finito.features.boards.presentation.screen.addeditboard

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
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import com.example.finito.features.tasks.domain.entity.CompletedTask
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class AddEditBoardViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val labelUseCases: LabelUseCases,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var boardState by mutableStateOf(BoardState.ACTIVE)
        private set

    var board by mutableStateOf<DetailedBoard?>(null)
        private set

    var labels by mutableStateOf<List<SimpleLabel>>(emptyList())
        private set

    var selectedLabels by mutableStateOf<List<SimpleLabel>>(emptyList())
        private set

    var nameState by mutableStateOf(TextFieldState())
        private set

    var showLabels by mutableStateOf(false)
        private set

    var showScreenMenu by mutableStateOf(false)
        private set

    var dialogType by mutableStateOf<AddEditBoardEvent.DialogType?>(null)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchBoard()
        fetchLabels()
        fetchBoardState()
    }

    fun onEvent(event: AddEditBoardEvent) {
        when (event) {
            is AddEditBoardEvent.SelectLabel -> onSelectLabel(event.label)
            is AddEditBoardEvent.ChangeName -> nameState = nameState.copy(
                value = event.name
            )
            AddEditBoardEvent.CreateBoard -> onCreateBoard()
            AddEditBoardEvent.MoveBoardToTrash -> onMoveToTrash()
            AddEditBoardEvent.EditBoard -> onEditBoard()
            AddEditBoardEvent.ToggleLabelsVisibility -> showLabels = !showLabels
            AddEditBoardEvent.DeleteForever -> onDeleteForever()
            is AddEditBoardEvent.RestoreBoard -> onRestoreBoard(event.showSnackbar)
            is AddEditBoardEvent.ShowScreenMenu -> showScreenMenu = event.show
            is AddEditBoardEvent.ShowDialog -> dialogType = event.type
            AddEditBoardEvent.AlertNotEditable -> onAlertNotEditable()
            AddEditBoardEvent.UndoRestore -> onUndoRestore()
        }
    }

    private fun onAlertNotEditable() = viewModelScope.launch {
        _eventFlow.emit(Event.Snackbar.UneditableBoard)
    }

    private fun onSelectLabel(label: SimpleLabel) {
        val exists = selectedLabels.contains(label)
        selectedLabels = if (exists) {
            selectedLabels.filter { it != label }
        } else {
            selectedLabels + listOf(label)
        }
    }

    private fun onUndoRestore() = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            BoardWithLabelsAndTasks(
                board = this.board.copy(state = BoardState.DELETED, removedAt = LocalDateTime.now()),
                labels = labels,
                tasks = tasks.map { CompletedTask(completed = it.task.completed) }
            ).let {
                boardUseCases.updateBoard(it)
            }.also {
                fetchBoard()
                boardState = BoardState.DELETED
            }
        }
    }

    private fun onRestoreBoard(showSnackbar: Boolean) = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            BoardWithLabelsAndTasks(
                board = this.board.copy(state = BoardState.ACTIVE, removedAt = null),
                labels = labels,
                tasks = tasks.map { CompletedTask(completed = it.task.completed) }
            ).let {
                boardUseCases.updateBoard(it)
            }.also {
                fetchBoard()
                boardState = BoardState.ACTIVE

                if (!showSnackbar) return@also
                _eventFlow.emit(Event.Snackbar.RestoredBoard)
            }
        }
    }

    private fun onDeleteForever() = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            boardUseCases.deleteBoard(board)
        }
    }

    private fun onMoveToTrash() = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            BoardWithLabelsAndTasks(
                board = board.copy(state = BoardState.DELETED, removedAt = LocalDateTime.now()),
                labels = labels,
                tasks = tasks.map { CompletedTask(completed = it.task.completed) }
            ).let {
                boardUseCases.updateBoard(it)
            }.also { _eventFlow.emit(Event.Snackbar.DeletedBoard) }
        }
    }

    private fun onCreateBoard() = viewModelScope.launch {
        val board = BoardWithLabelsAndTasks(
            board = Board(name = nameState.value),
            labels = selectedLabels
        )
        boardUseCases.createBoard(board).also {
            _eventFlow.emit(Event.NavigateToCreatedBoard(it))
        }
    }

    private fun onEditBoard() = viewModelScope.launch {
        if (board == null) return@launch
        if (!boardChanged()) {
            _eventFlow.emit(Event.NavigateToUpdatedBoard(board!!.board.boardId))
            return@launch
        }

        with(board!!) {
            BoardWithLabelsAndTasks(
                board = board.copy(name = nameState.value),
                labels = selectedLabels,
                tasks = tasks.map { CompletedTask(completed = it.task.completed) }
            ).let {
                when (boardUseCases.updateBoard(it)) {
                    is Result.Error -> {
                        _eventFlow.emit(Event.ShowError(
                            error = R.string.update_board_error
                        ))
                    }
                    is Result.Success -> {
                        _eventFlow.emit(Event.NavigateToUpdatedBoard(it.board.boardId))
                    }
                }
            }
        }
    }

    private fun fetchBoard() {
        savedStateHandle.get<Int>(Screen.BOARD_ID_ARGUMENT)?.let { boardId ->
            viewModelScope.launch {
                when (val result = boardUseCases.findOneBoard(boardId)) {
                    is Result.Error -> {
                        _eventFlow.emit(Event.ShowError(
                            error = R.string.find_board_error
                        ))
                    }
                    is Result.Success -> {
                        board = result.data
                        nameState = TextFieldState(value = result.data.board.name)
                        selectedLabels = result.data.labels
                    }
                }
            }
        }
    }

    private fun fetchLabels() = viewModelScope.launch {
        labelUseCases.findSimpleLabels().data.onEach {
            labels = it
        }.launchIn(viewModelScope)
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

    private fun boardChanged(): Boolean {
        return if (nameState.value != board?.board?.name) {
            true
        } else selectedLabels != board?.labels
    }

    sealed class Event {
        data class NavigateToCreatedBoard(val id: Int) : Event()

        data class NavigateToUpdatedBoard(val id: Int) : Event()

        data class ShowError(@StringRes val error: Int) : Event()

        sealed class Snackbar(
            @StringRes val message: Int,
            @StringRes val actionLabel: Int,
        ) : Event() {
            object UneditableBoard : Snackbar(
                message = R.string.board_not_editable,
                actionLabel = R.string.restore
            )

            object RestoredBoard : Snackbar(
                message = R.string.board_was_restored,
                actionLabel = R.string.undo
            )

            object DeletedBoard : Snackbar(
                message = R.string.board_moved_to_trash,
                actionLabel = R.string.undo
            )
        }
    }
}