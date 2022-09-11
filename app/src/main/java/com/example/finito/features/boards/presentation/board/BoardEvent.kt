package com.example.finito.features.boards.presentation.board

import com.example.finito.core.domain.Priority
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks

sealed class BoardEvent {
    data class ShowScreenMenu(val show: Boolean) : BoardEvent()

    object ArchiveBoard : BoardEvent()

    object DeleteBoard : BoardEvent()

    object DeleteCompletedTasks : BoardEvent()

    data class CheckTask(val task: TaskWithSubtasks) : BoardEvent()

    data class UncheckTask(val task: TaskWithSubtasks) : BoardEvent()

    data class ShowDialog(val type: DialogType? = null) : BoardEvent()

    data class ChangeTaskPriorityConfirm(val task: TaskWithSubtasks) : BoardEvent()

    data class ChangeTaskPriority(val priority: Priority?) : BoardEvent()

    object ToggleCompletedTasksVisibility : BoardEvent()

    sealed class DialogType {
        object DeleteCompletedTasks : DialogType()

        data class Priority(val taskWithSubtasks: TaskWithSubtasks) : DialogType()
    }
}
