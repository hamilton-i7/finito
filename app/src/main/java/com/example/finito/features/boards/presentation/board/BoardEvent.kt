package com.example.finito.features.boards.presentation.board

import com.example.finito.core.domain.Priority
import com.example.finito.features.boards.domain.entity.Board
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import java.time.LocalDate
import java.time.LocalTime

sealed class BoardEvent {
    data class ShowScreenMenu(val show: Boolean) : BoardEvent()

    data class ArchiveBoard(val board: BoardWithLabelsAndTasks) : BoardEvent()

    data class DeleteBoard(val board: BoardWithLabelsAndTasks) : BoardEvent()

    object DeleteCompletedTas : BoardEvent()

    data class CheckTask(val task: TaskWithSubtasks) : BoardEvent()

    data class UncheckTask(val task: TaskWithSubtasks) : BoardEvent()

    data class ShowPriorityDialog(val show: Boolean) : BoardEvent()

    data class ChangeTaskPriority(val task: Task, val priority: Priority) : BoardEvent()

    data class ShowDateTimeDialog(val show: Boolean) : BoardEvent()

    data class ChangeTaskDateTime(
        val task: Task,
        val date: LocalDate?,
        val time: LocalTime? = null,
    ) : BoardEvent()

    object ToggleCompletedTasksVisibility : BoardEvent()

    sealed class DialogType {
        object DeleteCompletedTasks : DialogType()
    }
}
