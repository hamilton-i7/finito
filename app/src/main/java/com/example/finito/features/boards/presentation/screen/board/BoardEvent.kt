package com.example.finito.features.boards.presentation.screen.board

import androidx.annotation.StringRes
import com.example.finito.core.domain.Priority
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import org.burnoutcrew.reorderable.ItemPosition
import java.time.LocalDate
import java.time.LocalTime

sealed class BoardEvent {
    data class ShowScreenMenu(val show: Boolean) : BoardEvent()

    object ArchiveBoard : BoardEvent()

    object DeleteBoard : BoardEvent()

    object RestoreBoard : BoardEvent()

    object RefreshBoard : BoardEvent()

    object DeleteCompletedTasks : BoardEvent()

    data class ReorderTasks(val from: ItemPosition, val to: ItemPosition) : BoardEvent()

    data class ReorderSubtasks(val from: ItemPosition, val to: ItemPosition) : BoardEvent()

    data class SaveTasksOrder(val from: Int, val to: Int) : BoardEvent()

    data class SaveSubtasksOrder(val from: Int, val to: Int) : BoardEvent()

    data class ToggleTaskCompleted(val task: TaskWithSubtasks) : BoardEvent()

    data class ShowDialog(val type: DialogType? = null) : BoardEvent()

    data class ShowTaskDateTimeFullDialog(val task: Task?) : BoardEvent()

    data class ChangeTaskPriorityConfirm(val task: Task) : BoardEvent()

    data class ChangeNewTaskName(val name: String) : BoardEvent()

    object SaveTask : BoardEvent()

    data class ChangeTaskPriority(val priority: Priority?) : BoardEvent()

    data class ChangeTaskDate(val date: LocalDate? = null) : BoardEvent()

    data class ChangeTaskTime(val time: LocalTime? = null) : BoardEvent()

    object SaveTaskDateTimeChanges : BoardEvent()

    object ToggleCompletedTasksVisibility : BoardEvent()

    data class DragContent(val content: DraggingContent? = null) : BoardEvent()

    sealed class DialogType {
        object DeleteCompletedTasks : DialogType()

        data class Priority(val taskWithSubtasks: Task) : DialogType()

        data class Error(@StringRes val message: Int) : DialogType()

        object TaskDate : DialogType()

        object TaskTime : DialogType()

        object DiscardChanges : DialogType()
    }

    enum class DraggingContent {
        TASK, SUBTASK
    }
}
