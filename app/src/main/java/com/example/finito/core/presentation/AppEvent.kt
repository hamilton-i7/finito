package com.example.finito.core.presentation

import com.example.finito.features.boards.domain.entity.DetailedBoard
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks

sealed class AppEvent {
    data class UndoBoardChange(val board: DetailedBoard) : AppEvent()

    data class UndoTaskCompletedToggle(val task: TaskWithSubtasks) : AppEvent()

    data class UndoSubtaskCompletedToggle(val subtask: Subtask) : AppEvent()

    data class RecoverTask(val task: TaskWithSubtasks) : AppEvent()

    data class RecoverSubtask(val subtask: Subtask) : AppEvent()

    object RefreshBoard : AppEvent()
}
