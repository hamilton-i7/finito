package com.example.finito.features.boards.presentation.home

import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.finito.core.di.PreferencesModule
import com.example.finito.core.domain.util.ResourceException
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.boards.domain.usecase.BoardUseCases
import com.example.finito.features.labels.domain.usecase.LabelUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val boardUseCases: BoardUseCases,
    private val labelUseCases: LabelUseCases,
    private val preferences: SharedPreferences
) : ViewModel() {

    var state by mutableStateOf(
        HomeState(
            gridLayout = preferences.getBoolean(PreferencesModule.TAG.GRID_LAYOUT.name, true),
            boardsOrder = preferences.getString(
                PreferencesModule.TAG.BOARDS_ORDER.name,
                SortingOption.Common.NameAZ.name
            )?.let {
                when (it) {
                    SortingOption.Common.NameZA.name -> SortingOption.Common.NameZA
                    SortingOption.Common.Newest.name -> SortingOption.Common.Newest
                    SortingOption.Common.Oldest.name -> SortingOption.Common.Oldest
                    else -> SortingOption.Common.NameAZ
                }
            } ?: SortingOption.Common.NameAZ
        )
    ); private set

    private var recentlyDeactivatedBoard: BoardWithLabelsAndTasks? = null
    private var fetchBoardsJob: Job? = null

    init {
        fetchLabels()
        fetchBoards(state.boardsOrder)
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.AddFilter -> {
                val exists = state.labelFilters.contains(event.labelId)
                state = if (exists) {
                    state.copy(
                        labelFilters = state.labelFilters.filter { it != event.labelId }
                    )
                } else {
                    state.copy(
                        labelFilters = state.labelFilters + listOf(event.labelId)
                    )
                }
                fetchBoards(sortingOption = state.boardsOrder, filters = state.labelFilters)
            }
            HomeEvent.RemoveFilters -> {
                state = state.copy(labelFilters = emptyList())
                fetchBoards(sortingOption = state.boardsOrder, filters = emptyList())
            }
            is HomeEvent.SortBoards -> {
                if (event.sortingOption::class == state.boardsOrder::class) return

                with(preferences.edit()) {
                    putString(PreferencesModule.TAG.BOARDS_ORDER.name, event.sortingOption.name)
                }
                fetchBoards(event.sortingOption, filters = state.labelFilters)
            }
            is HomeEvent.ArchiveBoard -> deactivateBoard(event.board, DeactivateMode.ARCHIVE)
            is HomeEvent.DeleteBoard -> deactivateBoard(event.board, DeactivateMode.DELETE)
            is HomeEvent.SearchBoards -> TODO()
            HomeEvent.ChangeLayout -> changeLayout()
            HomeEvent.RestoreBoard -> restoreBoard()
        }
    }

    private fun fetchLabels() = viewModelScope.launch {
        labelUseCases.findSimpleLabels().onEach { labels ->
            state = state.copy(labels = labels)
        }.launchIn(viewModelScope)
    }

    private fun fetchBoards(
        sortingOption: SortingOption.Common = state.boardsOrder,
        filters: List<Int> = emptyList(),
    ) = viewModelScope.launch {

        fetchBoardsJob?.cancel()
        fetchBoardsJob = boardUseCases.findAllBoards(
            boardOrder = sortingOption,
            labelIds = filters.toIntArray()
        ).onEach { boards ->
            state = state.copy(
                boards = boards,
                boardsOrder = sortingOption
            )
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
                        DeactivateMode.DELETE -> copy(board = this.board.copy(deleted = true))
                    }
                )
                recentlyDeactivatedBoard = this
            }
        } catch (e: ResourceException.NegativeIdException) {

        } catch (e: ResourceException.EmptyException) {

        } catch (e: ResourceException.InvalidStateException) {

        } catch (e: ResourceException.NotFoundException) {}
    }

    private fun changeLayout() {
        state = state.copy(
            gridLayout = !state.gridLayout
        )
        with(preferences.edit()) {
            putBoolean(PreferencesModule.TAG.GRID_LAYOUT.name, state.gridLayout)
        }
    }

    private fun restoreBoard() = viewModelScope.launch {
        recentlyDeactivatedBoard?.let {
            boardUseCases.updateBoard(it)
            recentlyDeactivatedBoard = null
        }
    }

    enum class DeactivateMode {
        ARCHIVE, DELETE
    }
}