package com.example.finito.features.boards.presentation.trash

import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks

sealed class TrashEvent {
    data class RestoreBoard(val board: BoardWithLabelsAndTasks) : TrashEvent()

    object UndoRestore : TrashEvent()

    data class DeleteForever(val board: Board) : TrashEvent()

    object EmptyTrash : TrashEvent()

    data class ShowMenu(val show: Boolean) : TrashEvent()

    data class ShowCardMenu(val boardId: Int = 0, val show: Boolean) : TrashEvent()

    data class ShowDialog(val type: DialogType? = null) : TrashEvent()

    sealed class DialogType {
        data class DeleteBoard(val board: Board) : DialogType()

        object EmptyTrash : DialogType()
    }
}
