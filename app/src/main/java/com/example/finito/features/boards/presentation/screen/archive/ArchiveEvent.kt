package com.example.finito.features.boards.presentation.screen.archive

import androidx.annotation.StringRes
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks

sealed class ArchiveEvent {
    data class SortBoards(val sortingOption: SortingOption.Common?) : ArchiveEvent()

    data class AddFilter(val labelId: Int) : ArchiveEvent()

    object RemoveFilters : ArchiveEvent()

    data class UnarchiveBoard(val board: BoardWithLabelsAndTasks) : ArchiveEvent()

    data class MoveBoardToTrash(val board: BoardWithLabelsAndTasks) : ArchiveEvent()

    data class SearchBoards(val query: String) : ArchiveEvent()

    object ToggleLayout : ArchiveEvent()

    data class ShowSearchBar(val show: Boolean) : ArchiveEvent()

    data class ShowCardMenu(val boardId: Int = 0, val show: Boolean) : ArchiveEvent()

    data class ShowDialog(val type: DialogType? = null) : ArchiveEvent()

    sealed class DialogType {
        data class Error(@StringRes val message: Int) : DialogType()
    }
}