package com.example.finito.features.boards.presentation.trash

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.di.PreferencesModule
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
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

    var showDialog by mutableStateOf(false)
        private set

    var dialogType by mutableStateOf<TrashEvent.DialogType>(TrashEvent.DialogType.EmptyTrash)
        private set

    init {
        fetchBoards()
    }

    fun onEvent(event: TrashEvent) {
        when (event) {
            is TrashEvent.DeleteForever -> deleteBoard(event.board)
            TrashEvent.EmptyTrash -> emptyTrash()
            is TrashEvent.RestoreBoard -> restoreBoard(event.board)
            TrashEvent.UndoRestore -> undoRestore()
            is TrashEvent.ShowMenu -> onShowDialogChange(event.show)
            is TrashEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
            is TrashEvent.ShowDeleteDialog -> {
                if (!event.show) {
                    showDialog = false
                    return
                }
                showDialog = true

                when (event.type) {
                    is TrashEvent.DialogType.DeleteBoard -> {
                        onShowDeleteSingleDialog(board = event.type.board)
                    }
                    TrashEvent.DialogType.EmptyTrash -> onShowEmptyTrashDialog()
                }
            }
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
            )
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

    private fun onShowDialogChange(show: Boolean) {
        if (show == showMenu) return
        showMenu = show
    }

    private fun onShowCardMenu(id: Int, show: Boolean) {
        selectedBoardId = if (!show) 0 else id
        showCardMenu = show
    }

    private fun onShowEmptyTrashDialog() {
        dialogType = TrashEvent.DialogType.EmptyTrash
    }

    private fun onShowDeleteSingleDialog(board: Board) {
        dialogType = TrashEvent.DialogType.DeleteBoard(board)
    }
}