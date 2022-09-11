package com.example.finito.features.boards.presentation.trash

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

    var showCardMenu by mutableStateOf(false)
        private set

    var selectedBoardId by mutableStateOf(0)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchBoards()
    }

    // TODO: Delete boards after 7 days of being in the trash
    fun onEvent(event: TrashEvent) {
        when (event) {
            is TrashEvent.DeleteForever -> deleteBoard(event.board)
            TrashEvent.EmptyTrash -> emptyTrash()
            is TrashEvent.RestoreBoard -> restoreBoard(event.board)
            TrashEvent.UndoRestore -> undoRestore()
            is TrashEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
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
                copy(board = this.board.copy(deleted = false, removedAt = null))
            ).also {
                _eventFlow.emit(Event.ShowSnackbar(message = R.string.board_was_restored))
            }
            recentlyRestoredBoard = this
        }
    }

    private fun undoRestore() = viewModelScope.launch {
        recentlyRestoredBoard?.let {
            boardUseCases.updateBoard(
                it.copy(board = it.board.copy(deleted = true, removedAt = it.board.removedAt))
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