package com.example.finito.features.boards.presentation.screen.home

import androidx.annotation.StringRes
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import org.burnoutcrew.reorderable.ItemPosition

sealed class HomeEvent {
    data class SortBoards(val sortingOption: SortingOption.Common?) : HomeEvent()

    data class SelectFilter(val labelId: Int) : HomeEvent()

    object RemoveFilters : HomeEvent()

    data class ArchiveBoard(val board: BoardWithLabelsAndTasks) : HomeEvent()

    data class MoveBoardToTrash(val board: BoardWithLabelsAndTasks) : HomeEvent()

    data class SearchBoards(val query: String) : HomeEvent()

    data class ReorderTasks(val from: ItemPosition, val to: ItemPosition) : HomeEvent()

    data class SaveTasksOrder(val from: Int, val to: Int) : HomeEvent()

    object ToggleLayout : HomeEvent()

    data class ShowSearchBar(val show: Boolean) : HomeEvent()

    data class ShowCardMenu(val boardId: Int = 0, val show: Boolean) : HomeEvent()

    data class ShowDialog(val type: DialogType? = null) : HomeEvent()

    sealed class DialogType {
        data class Error(@StringRes val message: Int) : DialogType()
    }
}
