package com.example.finito.core.presentation

import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks

sealed class AppEvent {
    data class UndoBoardChange(val board: DetailedBoard) : AppEvent()

    data class UndoTaskChange(val task: TaskWithSubtasks) : AppEvent()
}
