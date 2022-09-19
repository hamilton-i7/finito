package com.example.finito.features.boards.presentation.screen.board

import androidx.annotation.StringRes
import com.example.finito.core.domain.Priority
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import java.time.LocalDate
import java.time.LocalTime

sealed class BoardEvent {
    data class ShowScreenMenu(val show: Boolean) : BoardEvent()

    object ArchiveBoard : BoardEvent()

    object DeleteBoard : BoardEvent()

    object RestoreBoard : BoardEvent()

    object RefreshBoard : BoardEvent()

    object DeleteCompletedTasks : BoardEvent()

    data class ToggleTaskCompleted(val task: TaskWithSubtasks) : BoardEvent()

    data class ShowDialog(val type: DialogType? = null) : BoardEvent()

    data class ShowTaskDateTimeFullDialog(val task: TaskWithSubtasks?) : BoardEvent()

    data class ChangeTaskPriorityConfirm(val task: TaskWithSubtasks) : BoardEvent()

    data class ChangeNewTaskName(val name: String) : BoardEvent()

    object SaveTask : BoardEvent()

    data class ChangeTaskPriority(val priority: Priority?) : BoardEvent()

    data class ChangeTaskDate(val date: LocalDate? = null) : BoardEvent()

    data class ChangeTaskTime(val time: LocalTime? = null) : BoardEvent()

    object SaveTaskDateTimeChanges : BoardEvent()

    object ToggleCompletedTasksVisibility : BoardEvent()

    sealed class DialogType {
        object DeleteCompletedTasks : DialogType()

        data class Priority(val taskWithSubtasks: TaskWithSubtasks) : DialogType()

        data class Error(@StringRes val message: Int) : DialogType()

        object TaskDate : DialogType()

        object TaskTime : DialogType()

        object DiscardChanges : DialogType()
    }
}
