package com.example.finito.features.boards.presentation.screen.archive

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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    var dialogType by mutableStateOf<ArchiveEvent.DialogType?>(null)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchBoards()
    }

    fun onEvent(event: ArchiveEvent) {
        when (event) {
            is ArchiveEvent.SortBoards -> onSortBoards(event.sortingOption)
            is ArchiveEvent.UnarchiveBoard -> onMoveBoard(event.board, EditMode.UNARCHIVE)
            is ArchiveEvent.MoveBoardToTrash -> onMoveBoard(event.board, EditMode.DELETE)
            ArchiveEvent.ToggleLayout -> onToggleLayout()
            is ArchiveEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
            is ArchiveEvent.ShowDialog -> onShowDialogChange(event.type)
            ArchiveEvent.EnableSearch -> onEnableSearch()
        }
    }

    private fun onEnableSearch() = viewModelScope.launch {
        fireEvents(Event.NavigateToSearchBoards)
    }

    private fun onShowDialogChange(dialogType: ArchiveEvent.DialogType?) {
        this.dialogType = dialogType
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
        fetchBoardsJob = boardUseCases.findArchivedBoards(
            boardOrder = boardsOrder
        ).onEach { boards ->
            this@ArchiveViewModel.boards = boards
            boardsOrder = boardsOrder
        }.launchIn(viewModelScope)
    }

    private fun onMoveBoard(
        board: BoardWithLabelsAndTasks,
        mode: EditMode,
    ) = viewModelScope.launch {
        with(board) {
            when (mode) {
                EditMode.UNARCHIVE -> {
                    val updatedBoard = copy(
                        board = this.board.copy(
                            state = BoardState.ACTIVE,
                            position = 0,
                            archivedAt = null,
                        )
                    )
                    when (boardUseCases.updateBoard(updatedBoard)) {
                        is Result.Error -> {
                            fireEvents(Event.ShowError(
                                error = R.string.restore_board_error
                            ))
                        }
                        is Result.Success -> {
                            fireEvents(
                                Event.Snackbar.BoardStateChanged(
                                    message = R.string.board_was_restored,
                                    board = this,
                                )
                            )
                        }
                    }
                }
                EditMode.DELETE -> {
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

    enum class EditMode {
        UNARCHIVE, DELETE
    }

    sealed class Event {
        data class ShowError(@StringRes val error: Int) : Event()

        sealed class Snackbar(
            @StringRes open val message: Int,
            @StringRes val actionLabel: Int = R.string.undo,
        ) : Event() {
            class BoardStateChanged(
                @StringRes message: Int,
                val board: BoardWithLabelsAndTasks,
            ) : Snackbar(message)
        }

        object NavigateToSearchBoards : Event()
    }
}