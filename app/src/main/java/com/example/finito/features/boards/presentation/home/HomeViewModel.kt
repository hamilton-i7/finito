package com.example.finito.features.boards.presentation.home

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.SEARCH_DELAY_MILLIS
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.boards.utils.DeactivateMode
import com.example.finito.features.labels.domain.entity.SimpleLabel
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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

    var searchQuery by mutableStateOf("")
        private set

    private var searchJob: Job? = null

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

    private var recentlyDeactivatedBoard: BoardWithLabelsAndTasks? = null

    var showCardMenu by mutableStateOf(false)
        private set

    var selectedBoardId by mutableStateOf(0)
        private set

    init {
        fetchLabels()
        fetchBoards()
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.AddFilter -> {
                val exists = labelFilters.contains(event.labelId)
                labelFilters = if (exists) {
                    labelFilters.filter { it != event.labelId }
                } else {
                    labelFilters + listOf(event.labelId)
                }
                fetchBoards()
            }
            HomeEvent.RemoveFilters -> {
                labelFilters = emptyList()
                fetchBoards()
            }
            is HomeEvent.SortBoards -> {
                if (event.sortingOption::class == boardsOrder::class) return

                boardsOrder = event.sortingOption
                with(preferences.edit()) {
                    putString(PreferencesModule.TAG.BOARDS_ORDER.name, event.sortingOption.name)
                    apply()
                }
                fetchBoards()
            }
            is HomeEvent.ArchiveBoard -> deactivateBoard(event.board, DeactivateMode.ARCHIVE)
            is HomeEvent.DeleteBoard -> deactivateBoard(event.board, DeactivateMode.DELETE)
            is HomeEvent.SearchBoards -> {
                searchQuery = event.query

                searchJob?.cancel()
                searchJob = viewModelScope.launch {
                    delay(SEARCH_DELAY_MILLIS)
                    fetchBoards()
                }
            }
            HomeEvent.ToggleLayout -> toggleLayout()
            HomeEvent.RestoreBoard -> restoreBoard()
            is HomeEvent.ShowSearchBar -> {
                showSearchBar(event.show)
            }
            is HomeEvent.ShowCardMenu -> onShowCardMenu(id = event.boardId, show = event.show)
        }
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
            searchQuery = searchQuery,
            labelIds = labelFilters.toIntArray()
        ).onEach { boards ->
            this@HomeViewModel.boards = boards
            boardsOrder = boardsOrder
        }.launchIn(viewModelScope)
    }

    private fun deactivateBoard(
        board: BoardWithLabelsAndTasks,
        mode: DeactivateMode,
    ) = viewModelScope.launch {
        try {
            with(board) {
                boardUseCases.updateBoard(
                    when (mode) {
                        DeactivateMode.ARCHIVE -> copy(board = this.board.copy(archived = true))
                        DeactivateMode.DELETE -> copy(board = this.board.copy(
                            deleted = true,
                            removedAt = LocalDateTime.now()
                        ))
                    }
                )
                recentlyDeactivatedBoard = this
            }
        } catch (e: ResourceException.NegativeIdException) {

        } catch (e: ResourceException.EmptyException) {

        } catch (e: ResourceException.InvalidStateException) {

        } catch (e: ResourceException.NotFoundException) {}
    }

    private fun toggleLayout() {
        gridLayout = !gridLayout
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.GRID_LAYOUT.name, gridLayout)
            apply()
        }
    }

    private fun restoreBoard() = viewModelScope.launch {
        recentlyDeactivatedBoard?.let {
            boardUseCases.updateBoard(
                it.copy(board = it.board.copy(archived = false, deleted = false, removedAt = null))
            )
            recentlyDeactivatedBoard = null
        }
    }

    private fun showSearchBar(show: Boolean) {
        if (show == showSearchBar) return
        if (!show) {
            searchQuery = ""
            fetchBoards()
        }
        showSearchBar = show
    }

    private fun onShowCardMenu(id: Int, show: Boolean) {
        selectedBoardId = if (!show) 0 else id
        showCardMenu = show
    }

}