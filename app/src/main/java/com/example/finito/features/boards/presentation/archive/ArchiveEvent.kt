package com.example.finito.features.boards.presentation.archive

import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks

sealed class ArchiveEvent {
    data class SortBoards(val sortingOption: SortingOption.Common) : ArchiveEvent()

    data class AddFilter(val labelId: Int) : ArchiveEvent()

    object RemoveFilters : ArchiveEvent()

    data class UnarchiveBoard(val board: BoardWithLabelsAndTasks) : ArchiveEvent()

    data class DeleteBoard(val board: BoardWithLabelsAndTasks) : ArchiveEvent()

    data class SearchBoards(val query: String) : ArchiveEvent()

    object ToggleLayout : ArchiveEvent()

    object RestoreBoard : ArchiveEvent()

    data class ShowSearchBar(val show: Boolean) : ArchiveEvent()

    data class ShowCardMenu(val boardId: Int = 0, val show: Boolean) : ArchiveEvent()
}
