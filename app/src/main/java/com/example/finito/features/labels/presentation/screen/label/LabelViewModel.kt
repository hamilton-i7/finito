package com.example.finito.features.labels.presentation.screen.label

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.domain.Result
import com.example.finito.core.domain.util.SEARCH_DELAY_MILLIS
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.PreferencesKeys
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.boards.utils.DeactivateMode
import com.example.finito.features.labels.domain.entity.Label
import com.example.finito.features.labels.domain.usecase.LabelUseCases
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
class LabelViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val labelUseCases: LabelUseCases,
    private val preferences: SharedPreferences,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    var label by mutableStateOf<Label?>(null)
        private set

    var boards by mutableStateOf<List<BoardWithLabelsAndTasks>>(emptyList())
        private set

    var dialogType by mutableStateOf<LabelEvent.DialogType?>(null)
        private set

    var labelNameState by mutableStateOf(TextFieldState.Default)
        private set

    private var fetchBoardsJob: Job? = null

    var showSearchBar by mutableStateOf(false)
        private set

    var showScreenMenu by mutableStateOf(false)
        private set

    var searchQueryState by mutableStateOf(TextFieldState.Default)
        private set

    private var searchJob: Job? = null

    var boardsOrder by mutableStateOf(
        preferences.getString(
            PreferencesKeys.BOARDS_ORDER,
            null
        )?.let {
            when (it) {
                SortingOption.Common.NameZA.name -> SortingOption.Common.NameZA
                SortingOption.Common.Newest.name -> SortingOption.Common.Newest
                SortingOption.Common.Oldest.name -> SortingOption.Common.Oldest
                SortingOption.Common.NameAZ.name -> SortingOption.Common.NameAZ
                else -> SortingOption.Common.Custom
            }
        } ?: SortingOption.Common.Default
    ); private set

    var gridLayout by mutableStateOf(preferences.getBoolean(
        PreferencesKeys.GRID_LAYOUT,
        true
    )); private set

    var showCardMenu by mutableStateOf(false)

    var selectedBoardId by mutableStateOf(0)
        private set

    private val _eventFlow = MutableSharedFlow<Event>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        fetchLabel()
    }

    fun onEvent(event: LabelEvent) {
        when (event) {
            is LabelEvent.ArchiveBoard -> onDeactivateBoard(event.board, DeactivateMode.ARCHIVE)
            is LabelEvent.MoveBoardToTrash -> onDeactivateBoard(event.board, DeactivateMode.DELETE)
            is LabelEvent.SearchBoards -> onSearchBoards(event.query)
            is LabelEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
            is LabelEvent.ShowSearchBar -> onShowSearchBar(event.show)
            is LabelEvent.SortBoards -> onSortBoards(event.sortingOption)
            LabelEvent.ToggleLayout -> onToggleLayout()
            is LabelEvent.ShowScreenMenu -> showScreenMenu = event.show
            is LabelEvent.ChangeName -> labelNameState = labelNameState.copy(value = event.name)
            is LabelEvent.ShowDialog -> onShowDialog(event.type)
            LabelEvent.DeleteLabel -> onDeleteLabel()
            LabelEvent.EditLabel -> onEditLabel()
        }
    }

    private fun onShowDialog(type: LabelEvent.DialogType?) {
        dialogType = type
        if (dialogType == null) {
            labelNameState = labelNameState.copy(value = "")
        }
    }

    private fun onDeleteLabel() = viewModelScope.launch {
        if (label == null) return@launch
        with(label!!) {
            when (labelUseCases.deleteLabel(this)) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.delete_label_error)
                    )
                }
                is Result.Success -> _eventFlow.emit(Event.NavigateHome)
            }
        }
    }

    private fun onEditLabel() = viewModelScope.launch {
        if (label == null) return@launch
        with(label!!) {
            when (labelUseCases.updateLabel(copy(name = labelNameState.value))) {
                is Result.Error -> {
                    _eventFlow.emit(Event.ShowError(
                        error = R.string.update_label_error)
                    )
                }
                is Result.Success -> fetchLabel()
            }
        }
    }

    private fun onSortBoards(sortingOption: SortingOption.Common) {
        boardsOrder = sortingOption
        with(preferences.edit()) {
            putString(PreferencesKeys.BOARDS_ORDER, sortingOption.name)
            apply()
        }
        label?.let { fetchBoards(it.labelId) }
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

    private fun onShowCardMenu(id: Int, show: Boolean) {
        selectedBoardId = if (!show) 0 else id
        showCardMenu = show
    }

    private fun onSearchBoards(query: String) {
        searchQueryState = searchQueryState.copy(value = query)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            label?.let { fetchBoards(it.labelId) }
        }
    }

    private fun onShowSearchBar(show: Boolean) {
        if (!show) {
            searchQueryState = searchQueryState.copy(value = "")
            label?.let { fetchBoards(it.labelId) }
        }
        showSearchBar = show
    }

    private fun onToggleLayout() {
        gridLayout = !gridLayout
        with(preferences.edit()) {
            putBoolean(PreferencesKeys.GRID_LAYOUT, gridLayout)
            apply()
        }
    }

    private fun fetchLabel() {
        savedStateHandle.get<Int>(Screen.LABEL_ID_ARGUMENT)?.let { labelId ->
            viewModelScope.launch {
                when (val result = labelUseCases.findLabel(labelId)) {
                    is Result.Error -> {
                        _eventFlow.emit(Event.ShowError(
                            error = R.string.find_label_error
                        ))
                    }
                    is Result.Success -> {
                        label = result.data
                        labelNameState = TextFieldState(value = result.data.name)
                        fetchBoards(result.data.labelId)
                    }
                }
            }
        }
    }

    private fun fetchBoards(labelId: Int) = viewModelScope.launch {
        fetchBoardsJob?.cancel()
        fetchBoardsJob = boardUseCases.findActiveBoards(
            boardOrder = boardsOrder,
            searchQuery = searchQueryState.value,
            labelId
        ).data.onEach { boards ->
            this@LabelViewModel.boards = boards
            boardsOrder = boardsOrder
        }.launchIn(viewModelScope)
    }

    private suspend fun fireEvents(vararg events: Event) {
        events.forEachIndexed { index, event ->
            _eventFlow.emit(event)
            if (index != events.lastIndex) { delay(100) }
        }
    }

    sealed class Event {
        data class ShowError(@StringRes val error: Int) : Event()

        object NavigateHome : Event()

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