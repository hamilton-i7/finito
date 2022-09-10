package com.example.finito.features.boards.presentation.board

import com.example.finito.core.domain.Priority
import com.example.finito.features.boards.domain.entity.BoardWithLabelsAndTasks
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import java.time.LocalDate
import java.time.LocalTime

sealed class BoardEvent {
    data class ShowScreenMenu(val show: Boolean) : BoardEvent()

    data class ArchiveBoard(val board: BoardWithLabelsAndTasks) : BoardEvent()

    data class DeleteBoard(val board: BoardWithLabelsAndTasks) : BoardEvent()

    object DeleteCompletedTasks : BoardEvent()

    data class CheckTask(val task: TaskWithSubtasks) : BoardEvent()

    data class UncheckTask(val task: TaskWithSubtasks) : BoardEvent()

    data class ShowDialog(val type: DialogType? = null) : BoardEvent()

    data class ChangeTaskPriorityConfirm(val task: TaskWithSubtasks) : BoardEvent()

    data class ChangeTaskPriority(val priority: Priority?) : BoardEvent()
    
    data class ChangeTaskDate(val date: LocalDate, val task: TaskWithSubtasks) : BoardEvent()

    data class ChangeTaskTime(val time: LocalTime, val task: TaskWithSubtasks) : BoardEvent()

    object ToggleCompletedTasksVisibility : BoardEvent()

    sealed class DialogType {
        object DeleteCompletedTasks : DialogType()

        data class Priority(val taskWithSubtasks: TaskWithSubtasks) : DialogType()
    }
}
