package com.example.finito.core.presentation.util

import com.example.finito.features.boards.domain.entity.Board

sealed class DialogType {
    data class DeleteBoard(val board: Board) : DialogType()

    object EmptyTrash : DialogType()
}
