package com.example.finito.features.boards.presentation.screen.home

import android.content.SharedPreferences
import androidx.annotation.StringRes
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.R
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.util.SEARCH_DELAY_MILLIS
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.core.presentation.util.TextFieldState
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.boards.utils.DeactivateMode
import com.example.finito.features.labels.domain.entity.SimpleLabel
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
class HomeViewModel @Inject constructor(
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

    var showSearchBar by mutableStateOf(false)
        private set

    var searchQueryState by mutableStateOf(TextFieldState())
        private set

    private var searchJob: Job? = null

    var boardsOrder by mutableStateOf(
        preferences.getString(
            PreferencesModule.TAG.BOARDS_ORDER.name,
            null
        )?.let {
            when (it) {
                SortingOption.Common.NameZA.name -> SortingOption.Common.NameZA
                SortingOption.Common.Newest.name -> SortingOption.Common.Newest
                SortingOption.Common.Oldest.name -> SortingOption.Common.Oldest
                SortingOption.Common.NameAZ.name -> SortingOption.Common.NameAZ
                else -> null
            }
        }
    ); private set
    
    var gridLayout by mutableStateOf(preferences.getBoolean(
        PreferencesModule.TAG.GRID_LAYOUT.name,
        true
    )); private set

    private var recentlyDeactivatedBoard: BoardWithLabelsAndTasks? = null

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

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.SelectFilter -> onSelectFilter(event.labelId)
            HomeEvent.RemoveFilters -> onRemoveFilters()
            is HomeEvent.SortBoards -> onSortBoards(event.sortingOption)
            is HomeEvent.ArchiveBoard -> onDeactivateBoard(event.board, DeactivateMode.ARCHIVE)
            is HomeEvent.MoveBoardToTrash -> onDeactivateBoard(event.board, DeactivateMode.DELETE)
            is HomeEvent.SearchBoards -> onSearchBoards(event.query)
            HomeEvent.ToggleLayout -> onToggleLayout()
            HomeEvent.RestoreBoard -> onRestoreBoard()
            is HomeEvent.ShowSearchBar -> onShowSearchBar(event.show)
            is HomeEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
        }
    }

    private fun onSearchBoards(query: String) {
        searchQueryState = searchQueryState.copy(value = query)

        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(SEARCH_DELAY_MILLIS)
            fetchBoards()
        }
    }

    private fun onSortBoards(sortingOption: SortingOption.Common?) {
        boardsOrder = sortingOption
        with(preferences.edit()) {
            putString(PreferencesModule.TAG.BOARDS_ORDER.name, sortingOption?.name)
            apply()
        }
        fetchBoards()
    }

    private fun onRemoveFilters() {
        labelFilters = emptyList()
        fetchBoards()
    }

    private fun onSelectFilter(labelId: Int) {
        val exists = labelFilters.contains(labelId)
        labelFilters = if (exists) {
            labelFilters.filter { it != labelId }
        } else {
            labelFilters + listOf(labelId)
        }
        fetchBoards()
    }

    private fun fetchLabels() = viewModelScope.launch {
        labelUseCases.findSimpleLabels().onEach { labels ->
            this@HomeViewModel.labels = labels
        }.launchIn(viewModelScope)
    }

    private fun fetchBoards() = viewModelScope.launch {
        fetchBoardsJob?.cancel()
        fetchBoardsJob = boardUseCases.findActiveBoards(
            boardOrder = boardsOrder,
            searchQuery = searchQueryState.value,
            labelIds = labelFilters.toIntArray()
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
                    boardUseCases.updateBoard(copy(board = this.board.copy(
                        state = BoardState.ARCHIVED,
                        archivedAt = LocalDateTime.now(),
                        position = null
                    )))
                    _eventFlow.emit(Event.ShowSnackbar(message = R.string.board_archived))
                }
                DeactivateMode.DELETE -> {
                    boardUseCases.updateBoard(
                        copy(
                            board = this.board.copy(
                                state = BoardState.DELETED,
                                removedAt = LocalDateTime.now(),
                                position = null
                            )
                        )
                    )
                    _eventFlow.emit(Event.ShowSnackbar(message = R.string.board_moved_to_trash))
                }
            }
            recentlyDeactivatedBoard = this
        }
    }

    private fun onToggleLayout() {
        gridLayout = !gridLayout
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.GRID_LAYOUT.name, gridLayout)
            apply()
        }
    }

    private fun onRestoreBoard() = viewModelScope.launch {
        recentlyDeactivatedBoard?.let {
            boardUseCases.updateBoard(
                it.copy(board = it.board.copy(
                    state = BoardState.ACTIVE,
                    removedAt = null,
                    archivedAt = null,
                    position = recentlyDeactivatedBoard!!.board.position
                ))
            )
            recentlyDeactivatedBoard = null
        }
    }

    private fun onShowSearchBar(show: Boolean) {
        if (!show) {
            searchQueryState = searchQueryState.copy(value = "")
            fetchBoards()
        }
        showSearchBar = show
    }

    private fun onShowCardMenu(id: Int, show: Boolean) {
        selectedBoardId = if (!show) 0 else id
        showCardMenu = show
    }

    sealed class Event {
        data class ShowSnackbar(@StringRes val message: Int) : Event()
    }
}