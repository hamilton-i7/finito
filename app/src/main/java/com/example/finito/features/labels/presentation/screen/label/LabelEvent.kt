package com.example.finito.features.labels.presentation.screen.label

import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks

sealed class LabelEvent {
    object RenameLabel : LabelEvent()

    object DeleteLabel : LabelEvent()

    data class SortBoards(val sortingOption: SortingOption.Common) : LabelEvent()

    data class ArchiveBoard(val board: BoardWithLabelsAndTasks) :LabelEvent()

    data class MoveBoardToTrash(val board: BoardWithLabelsAndTasks) : LabelEvent()

    data class SearchBoards(val query: String) : LabelEvent()

    object ToggleLayout : LabelEvent()

    object RestoreBoard : LabelEvent()

    data class ShowSearchBar(val show: Boolean) : LabelEvent()

    data class ShowCardMenu(val boardId: Int = 0, val show: Boolean) : LabelEvent()
}
