package com.example.finito.features.boards.presentation.home

import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks

sealed class HomeEvent {
    data class SortBoards(val sortingOption: SortingOption.Common) : HomeEvent()

    data class SelectFilter(val labelId: Int) : HomeEvent()

    object RemoveFilters : HomeEvent()

    data class ArchiveBoard(val board: BoardWithLabelsAndTasks) : HomeEvent()

    data class DeleteBoard(val board: BoardWithLabelsAndTasks) : HomeEvent()

    data class SearchBoards(val query: String) : HomeEvent()

    object RestoreBoard : HomeEvent()

    data class RestoreCrossScreenBoard(val board: BoardWithLabelsAndTasks) : HomeEvent()

    data class ShowCardMenu(val boardId: Int = 0, val show: Boolean) : HomeEvent()
}
