package com.example.finito.features.boards.presentation.board

import com.example.finito.core.domain.Priority
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.tasks.domain.entity.DetailedTask
import java.time.LocalDate
import java.time.LocalTime

sealed class BoardEvent {
    data class ShowScreenMenu(val show: Boolean) : BoardEvent()

    data class ArchiveBoard(val board: BoardWithLabelsAndTasks) : BoardEvent()

    data class DeleteBoard(val board: BoardWithLabelsAndTasks) : BoardEvent()

    object DeleteCompletedTasks : BoardEvent()

    data class CheckTask(val task: DetailedTask) : BoardEvent()

    data class UncheckTask(val task: DetailedTask) : BoardEvent()

    data class ShowDialog(val type: DialogType? = null) : BoardEvent()

    data class ChangeTaskPriorityConfirm(val task: DetailedTask) : BoardEvent()

    data class ChangeTaskPriority(val priority: Priority?) : BoardEvent()
    
    data class ChangeTaskDate(val date: LocalDate?) : BoardEvent()

    data class ChangeTaskDateTime(
        val task: DetailedTask,
        val date: LocalDate?,
        val time: LocalTime? = null,
    ) : BoardEvent()

    object ToggleCompletedTasksVisibility : BoardEvent()

    sealed class DialogType {
        object DeleteCompletedTasks : DialogType()

        data class Priority(val detailedTask: DetailedTask) : DialogType()

        data class DateTime(val detailedTask: DetailedTask) : DialogType()
    }
}
