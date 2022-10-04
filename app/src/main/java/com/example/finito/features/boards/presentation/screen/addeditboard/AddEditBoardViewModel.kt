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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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
            AddEditBoardEvent.RefreshBoard -> {
                fetchBoard()
                fetchBoardState()
            }
        }
    }

    private fun onAlertNotEditable() = viewModelScope.launch {
        with(board!!) {
            val restoredBoard = BoardWithLabelsAndTasks(
                board = board.copy(state = BoardState.ACTIVE, removedAt = null, archivedAt = null),
                labels = labels,
                tasks = tasks.map { task -> task.toCompletedTask() }
            )
            fireEvents(Event.Snackbar.UneditableBoard(board = restoredBoard))
        }
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
                tasks = tasks.map { it.toCompletedTask() }
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
            val updatedBoard = BoardWithLabelsAndTasks(
                board = board.copy(
                    state = BoardState.ACTIVE,
                    removedAt = null,
                    archivedAt = null,
                    position = 0
                ),
                labels = labels,
                tasks = tasks.map { it.toCompletedTask() }
            )
            when (boardUseCases.updateBoard(updatedBoard)) {
                is Result.Error -> TODO()
                is Result.Success -> {
                    val originalBoard = BoardWithLabelsAndTasks(
                        board = board,
                        labels = labels,
                        tasks = tasks.map { it.toCompletedTask() }
                    )

                    fetchBoard()
                    boardState = BoardState.ACTIVE

                    if (!showSnackbar) return@launch
                    fireEvents(Event.Snackbar.BoardStateChanged(
                        message = R.string.board_was_restored,
                        board = originalBoard
                    ))
                }
            }
        }
    }

    private fun onDeleteForever() = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            when (boardUseCases.deleteBoard(board)) {
                is Result.Error -> {
                    fireEvents(Event.ShowError(
                        error = R.string.delete_board_error
                    ))
                }
                is Result.Success -> fireEvents(Event.NavigateToTrash)
            }
        }
    }

    private fun onMoveToTrash() = viewModelScope.launch {
        if (board == null) return@launch
        with(board!!) {
            val updatedBoard = BoardWithLabelsAndTasks(
                board = board.copy(
                    state = BoardState.DELETED,
                    removedAt = LocalDateTime.now(),
                    position = null
                ),
                labels = labels,
                tasks = tasks.map { it.toCompletedTask() }
            )
            when (boardUseCases.updateBoard(updatedBoard)) {
                is Result.Error -> TODO()
                is Result.Success -> {
                    val originalBoard = BoardWithLabelsAndTasks(
                        board = board,
                        labels = labels,
                        tasks = tasks.map { it.toCompletedTask() }
                    )
                    val events = mutableListOf<Event>(
                        Event.Snackbar.BoardStateChanged(
                            message = R.string.board_was_restored,
                            board = originalBoard
                        )
                    ).apply {
                        if (boardState == BoardState.ACTIVE)
                            add(Event.NavigateToHome)
                        else if (boardState == BoardState.ARCHIVED)
                            add(Event.NavigateToArchive)
                    }
                    fireEvents(*events.toTypedArray())
                }
            }
        }
    }

    private fun onCreateBoard() = viewModelScope.launch {
        val board = BoardWithLabelsAndTasks(
            board = Board(name = nameState.value),
            labels = selectedLabels
        )
        boardUseCases.createBoard(board).also {
            fireEvents(Event.NavigateToCreatedBoard(it))
        }
    }

    private fun onEditBoard() = viewModelScope.launch {
        if (board == null) return@launch
        if (!boardChanged()) {
            fireEvents(Event.NavigateToUpdatedBoard(board!!.board.boardId))
            return@launch
        }

        with(board!!) {
            BoardWithLabelsAndTasks(
                board = board.copy(name = nameState.value),
                labels = selectedLabels,
                tasks = tasks.map { it.toCompletedTask() }
            ).let {
                when (boardUseCases.updateBoard(it)) {
                    is Result.Error -> {
                        fireEvents(Event.ShowError(
                            error = R.string.update_board_error
                        ))
                    }
                    is Result.Success -> fireEvents(Event.NavigateToUpdatedBoard(it.board.boardId))
                }
            }
        }
    }

    private fun fetchBoard() {
        savedStateHandle.get<Int>(Screen.BOARD_ID_ARGUMENT)?.let { boardId ->
            viewModelScope.launch {
                when (val result = boardUseCases.findOneBoard(boardId)) {
                    is Result.Error -> {
                        fireEvents(Event.ShowError(
                            error = R.string.find_board_error
                        ))
                    }
                    is Result.Success -> {
                        println(result.data.board)
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

    private suspend fun fireEvents(vararg events: Event) {
        events.forEachIndexed { index, event ->
            _eventFlow.emit(event)
            if (index != events.lastIndex) { delay(100) }

        }
    }

    sealed class Event {
        data class NavigateToCreatedBoard(val id: Int) : Event()

        data class NavigateToUpdatedBoard(val id: Int) : Event()

        data class ShowError(@StringRes val error: Int) : Event()

        object NavigateToTrash : Event()

        object NavigateToHome : Event()

        object NavigateToArchive : Event()

        sealed class Snackbar(
            @StringRes val message: Int,
            @StringRes val actionLabel: Int = R.string.undo,
        ) : Event() {
            class UneditableBoard(
                val board: BoardWithLabelsAndTasks
            ) : Snackbar(
                message = R.string.board_not_editable,
                actionLabel = R.string.restore
            )

            class BoardStateChanged(
                @StringRes message: Int,
                val board: BoardWithLabelsAndTasks,
            ) : Snackbar(message)
        }
    }
}