package com.example.finito.features.boards.presentation.screen.home

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.util.PreferencesKeys
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.boards.utils.DeactivateMode
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ItemPosition
import java.time.LocalDateTime
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val preferences: SharedPreferences
) : ViewModel() {

    var boards by mutableStateOf<List<BoardWithLabelsAndTasks>>(emptyList())
        private set

    private var fetchBoardsJob: Job? = null

    var boardsOrder by mutableStateOf(
        preferences.getString(
            PreferencesKeys.BOARDS_ORDER,
            SortingOption.Common.Default.name
        )?.let {
            when (it) {
                SortingOption.Common.Newest.name -> SortingOption.Common.Newest
                SortingOption.Common.Oldest.name -> SortingOption.Common.Oldest
                SortingOption.Common.NameAZ.name -> SortingOption.Common.NameAZ
                SortingOption.Common.NameZA.name -> SortingOption.Common.NameZA
                else -> SortingOption.Common.Custom
            }
        } ?: SortingOption.Common.Default
    ); private set
    
    var gridLayout by mutableStateOf(preferences.getBoolean(
        PreferencesKeys.GRID_LAYOUT,
        true
    )); private set

    var showCardMenu by mutableStateOf(false)
        private set

    var selectedBoardId by mutableStateOf(0)
        private set

    var dialogType by mutableStateOf<HomeEvent.DialogType?>(null)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchBoards()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SortBoards -> onSortBoards(event.sortingOption)
            is HomeEvent.ArchiveBoard -> onDeactivateBoard(event.board, DeactivateMode.ARCHIVE)
            is HomeEvent.MoveBoardToTrash -> onDeactivateBoard(event.board, DeactivateMode.DELETE)
            HomeEvent.ToggleLayout -> onToggleLayout()
            is HomeEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
            is HomeEvent.ReorderTasks -> onReorder(event.from, event.to)
            is HomeEvent.SaveTasksOrder -> onSaveTasksOrder(event.from, event.to)
            is HomeEvent.ShowDialog -> onShowDialogChange(event.type)
            HomeEvent.EnableSearch -> onEnableSearch()
        }
    }

    private fun onEnableSearch() = viewModelScope.launch {
        fireEvents(Event.NavigateToSearchBoards)
    }

    private fun onShowDialogChange(dialogType: HomeEvent.DialogType?) {
        this.dialogType = dialogType
    }

    fun canDrag(position: ItemPosition): Boolean = boards.any { it.board.boardId == position.key }

    private fun onSaveTasksOrder(from: Int, to: Int) = viewModelScope.launch {
        if (from == to) return@launch
        boardUseCases.arrangeBoards(boards)
    }

    private fun onReorder(from: ItemPosition, to: ItemPosition) {
        boards = boards.toMutableList().apply {
            add(
                index = indexOfFirst { it.board.boardId == to.key },
                element = removeAt(indexOfFirst { it.board.boardId == from.key })
            )
        }
    }

    private fun onSortBoards(sortingOption: SortingOption.Common) {
        boardsOrder = sortingOption
        with(preferences.edit()) {
            putString(PreferencesKeys.BOARDS_ORDER, sortingOption.name)
            apply()
        }
        fetchBoards()
    }

    private fun fetchBoards() = viewModelScope.launch {
        fetchBoardsJob?.cancel()
        fetchBoardsJob = boardUseCases.findActiveBoards(
            boardOrder = boardsOrder
        ).data.onEach { boards ->
            this@HomeViewModel.boards = boards
            boardsOrder = boardsOrder
        }.launchIn(viewModelScope)
    }

    private fun onDeactivateBoard(
        board: BoardWithLabelsAndTasks,
        mode: DeactivateMode,
    ) = viewModelScope.launch {
        with(board) {
            when (mode) {
                DeactivateMode.ARCHIVE -> {
                    val updatedBoard = copy(
                        board = this.board.copy(
                            state = BoardState.ARCHIVED,
                            archivedAt = LocalDateTime.now(),
                            position = null,
                        )
                    )
                    when (boardUseCases.updateBoard(updatedBoard)) {
                        is Result.Error -> {
                            fireEvents(Event.ShowError(
                                error = R.string.archive_board_error
                            ))
                        }
                        is Result.Success -> {
                            fireEvents(
                                Event.Snackbar.BoardStateChanged(
                                    message = R.string.board_archived,
                                    board = this,
                                )
                            )
                        }
                    }
                }
                DeactivateMode.DELETE -> {
                    val updatedBoard = copy(
                        board = this.board.copy(
                            state = BoardState.DELETED,
                            position = null,
                            removedAt = LocalDateTime.now(),
                        )
                    )
                    when (boardUseCases.updateBoard(updatedBoard)) {
                        is Result.Error -> {
                            fireEvents(Event.ShowError(
                                error = R.string.move_to_trash_error
                            ))
                        }
                        is Result.Success -> {
                            fireEvents(
                                Event.Snackbar.BoardStateChanged(
                                    message = R.string.board_moved_to_trash,
                                    board = this,
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    private fun onToggleLayout() {
        gridLayout = !gridLayout
        with(preferences.edit()) {
            putBoolean(PreferencesKeys.GRID_LAYOUT, gridLayout)
            apply()
        }
    }

    private fun onShowCardMenu(id: Int, show: Boolean) {
        selectedBoardId = if (!show) 0 else id
        showCardMenu = show
    }

    private suspend fun fireEvents(vararg events: Event) {
        events.forEachIndexed { index, event ->
            _eventFlow.emit(event)
            if (index != events.lastIndex) { delay(100) }
        }
    }

    sealed class Event {
        data class ShowError(@StringRes val error: Int) : Event()

        object NavigateToSearchBoards : Event()

        sealed class Snackbar(
            @StringRes open val message: Int,
            @StringRes val actionLabel: Int = R.string.undo,
        ) : Event() {
            class BoardStateChanged(
                @StringRes message: Int,
                val board: BoardWithLabelsAndTasks,
            ) : Snackbar(message)
        }
    }
}