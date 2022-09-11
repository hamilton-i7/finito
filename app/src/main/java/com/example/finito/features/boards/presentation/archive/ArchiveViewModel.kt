package com.example.finito.features.boards.presentation.archive

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val labelUseCases: LabelUseCases,
    private val preferences: SharedPreferences
) : ViewModel() {

    var labels by mutableStateOf<List<SimpleLabel>>(emptyList())
        private set

    var labelFilters by mutableStateOf<List<Int>>(emptyList())
        private set

    var boards by mutableStateOf<List<BoardWithLabelsAndTasks>>(emptyList())
        private set

    private var fetchBoardsJob: Job? = null

    var boardsOrder by mutableStateOf(
        preferences.getString(
            PreferencesModule.TAG.BOARDS_ORDER.name,
            SortingOption.Common.Default.name
        )?.let {
            when (it) {
                SortingOption.Common.NameZA.name -> SortingOption.Common.NameZA
                SortingOption.Common.Newest.name -> SortingOption.Common.Newest
                SortingOption.Common.Oldest.name -> SortingOption.Common.Oldest
                else -> SortingOption.Common.NameAZ
            }
        } ?: SortingOption.Common.Default
    ); private set

    var gridLayout by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.GRID_LAYOUT.name,
        true
    )); private set

    private var recentlyMovedBoard: BoardWithLabelsAndTasks? = null

    var showCardMenu by mutableStateOf(false)
        private set

    var selectedBoardId by mutableStateOf(0)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchLabels()
        fetchBoards()
    }

    fun onEvent(event: ArchiveEvent) {
        when (event) {
            is ArchiveEvent.AddFilter -> {
                val exists = labelFilters.contains(event.labelId)
                labelFilters = if (exists) {
                    labelFilters.filter { it != event.labelId }
                } else {
                    labelFilters + listOf(event.labelId)
                }
                fetchBoards()
            }
            ArchiveEvent.RemoveFilters -> {
                labelFilters = emptyList()
                fetchBoards()
            }
            is ArchiveEvent.SortBoards -> {
                if (event.sortingOption::class == boardsOrder::class) return

                boardsOrder = event.sortingOption
                with(preferences.edit()) {
                    putString(PreferencesModule.TAG.BOARDS_ORDER.name, event.sortingOption.name)
                    apply()
                }
                fetchBoards()
            }
            is ArchiveEvent.UnarchiveBoard -> moveBoard(event.board, EditMode.UNARCHIVE)
            is ArchiveEvent.DeleteBoard -> moveBoard(event.board, EditMode.DELETE)
            is ArchiveEvent.SearchBoards -> fetchBoards(event.query)
            ArchiveEvent.RestoreBoard -> restoreBoard()
            is ArchiveEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
        }
    }

    private fun fetchLabels() = viewModelScope.launch {
        labelUseCases.findSimpleLabels().onEach { labels ->
            this@ArchiveViewModel.labels = labels
        }.launchIn(viewModelScope)
    }

    private fun fetchBoards(searchQuery: String? = null) = viewModelScope.launch {
        fetchBoardsJob?.cancel()
        fetchBoardsJob = boardUseCases.findArchivedBoards(
            boardOrder = boardsOrder,
            searchQuery = searchQuery,
            labelIds = labelFilters.toIntArray()
        ).onEach { boards ->
            this@ArchiveViewModel.boards = boards
            boardsOrder = boardsOrder
        }.launchIn(viewModelScope)
    }

    private fun moveBoard(
        board: BoardWithLabelsAndTasks,
        mode: EditMode,
    ) = viewModelScope.launch {
        with(board) {
            when (mode) {
                EditMode.UNARCHIVE -> {
                    boardUseCases.updateBoard(copy(board = this.board.copy(archived = false))).also {
                        _eventFlow.emit(Event.ShowSnackbar(message = R.string.board_moved_out_of_archive))
                    }
                }
                EditMode.DELETE -> {
                    boardUseCases.updateBoard(copy(board = this.board.copy(
                        deleted = true,
                        archived = false,
                        removedAt = LocalDateTime.now()
                    ))).also {
                        _eventFlow.emit(Event.ShowSnackbar(message = R.string.board_moved_to_trash))
                    }
                }
            }
            recentlyMovedBoard = this
        }
    }

    private fun restoreBoard() = viewModelScope.launch {
        recentlyMovedBoard?.let {
            boardUseCases.updateBoard(
                it.copy(board = it.board.copy(archived = true, deleted = false, removedAt = null))
            )
            recentlyMovedBoard = null
        }
    }

    private fun onShowCardMenu(id: Int, show: Boolean) {
        selectedBoardId = if (!show) 0 else id
        showCardMenu = show
    }

    enum class EditMode {
        UNARCHIVE, DELETE
    }

    sealed class Event {
        data class ShowSnackbar(@StringRes val message: Int) : Event()
    }
}