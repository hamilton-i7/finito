package com.example.finito.features.boards.presentation.screen.trash

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.di.PreferencesModule
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TrashViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    preferences: SharedPreferences
) : ViewModel() {

    var boards by mutableStateOf<List<BoardWithLabelsAndTasks>>(emptyList())
        private set

    private var recentlyRestoredBoard: BoardWithLabelsAndTasks? = null

    val gridLayout = preferences.getBoolean(
        PreferencesModule.TAG.GRID_LAYOUT.name,
        true
    )

    var showMenu by mutableStateOf(false)
        private set

    var showCardMenu by mutableStateOf(false)
        private set

    var selectedBoardId by mutableStateOf(0)
        private set

    var dialogType by mutableStateOf<TrashEvent.DialogType?>(null)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchBoards()
    }

    fun onEvent(event: TrashEvent) {
        // TODO 01/10/2022: Automatically delete boards that have a week on trash
        when (event) {
            is TrashEvent.DeleteForever -> deleteBoard(event.board)
            TrashEvent.EmptyTrash -> emptyTrash()
            is TrashEvent.RestoreBoard -> restoreBoard(event.board)
            TrashEvent.UndoRestore -> undoRestore()
            is TrashEvent.ShowMenu -> showMenu = event.show
            is TrashEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
            is TrashEvent.ShowDialog -> dialogType = event.type
        }
    }

    private fun fetchBoards() = viewModelScope.launch {
        boardUseCases.findDeletedBoards().onEach { boards ->
            this@TrashViewModel.boards = boards
        }.launchIn(viewModelScope)
    }

    private fun deleteBoard(board: Board) = viewModelScope.launch {
        boardUseCases.deleteBoard(board)
    }

    private fun emptyTrash() = viewModelScope.launch {
        with(boards.map { it.board }) {
            boardUseCases.deleteBoard(*toTypedArray())
        }
    }

    private fun restoreBoard(board: BoardWithLabelsAndTasks) = viewModelScope.launch {
        with(board) {
            boardUseCases.updateBoard(
                copy(board = this.board.copy(
                    state = BoardState.ACTIVE,
                    removedAt = null,
                    position = 0
                ))
            )
            _eventFlow.emit(Event.ShowSnackbar(message = R.string.board_was_restored))
            recentlyRestoredBoard = this
        }
    }

    private fun undoRestore() = viewModelScope.launch {
        recentlyRestoredBoard?.let {
            boardUseCases.updateBoard(
                it.copy(board = it.board.copy(
                    state = BoardState.DELETED,
                    removedAt = it.board.removedAt,
                    position = null
                ))
            )
            recentlyRestoredBoard = null
        }
    }

    private fun onShowCardMenu(id: Int, show: Boolean) {
        selectedBoardId = if (!show) 0 else id
        showCardMenu = show
    }

    sealed class Event {
        data class ShowSnackbar(@StringRes val message: Int) : Event()
    }
}