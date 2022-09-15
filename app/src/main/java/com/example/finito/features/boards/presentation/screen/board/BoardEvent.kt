package com.example.finito.features.boards.presentation.screen.board

import com.example.finito.core.domain.Priority
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks

sealed class BoardEvent {
    data class ShowScreenMenu(val show: Boolean) : BoardEvent()

    object EditBoard : BoardEvent()

    object ArchiveBoard : BoardEvent()

    object DeleteBoard : BoardEvent()

    object RestoreBoard : BoardEvent()

    object RefreshBoard : BoardEvent()

    object DeleteCompletedTasks : BoardEvent()

    data class EditTaskDateTime(val taskId: Int) : BoardEvent()

    data class ToggleTaskCompleted(val task: TaskWithSubtasks) : BoardEvent()

    data class ShowDialog(val type: DialogType? = null) : BoardEvent()

    data class ChangeTaskPriorityConfirm(val task: TaskWithSubtasks) : BoardEvent()

    data class ChangeTaskPriority(val priority: Priority?) : BoardEvent()

    object ToggleCompletedTasksVisibility : BoardEvent()

    sealed class DialogType {
        object DeleteCompletedTasks : DialogType()

        data class Priority(val taskWithSubtasks: TaskWithSubtasks) : DialogType()
    }
}
