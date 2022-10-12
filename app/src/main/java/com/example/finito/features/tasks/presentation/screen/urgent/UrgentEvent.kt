package com.example.finito.features.tasks.presentation.screen.urgent

import androidx.annotation.StringRes
import com.example.finito.features.tasks.domain.util.Priority
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import java.time.LocalDate
import java.time.LocalTime

sealed class UrgentEvent {
    data class ShowScreenMenu(val show: Boolean) : UrgentEvent()

    object DeleteCompleted : UrgentEvent()

    data class ToggleTaskCompleted(val task: TaskWithSubtasks) : UrgentEvent()

    data class ToggleSubtaskCompleted(val subtask: Subtask) : UrgentEvent()

    data class ShowDialog(val type: DialogType? = null) : UrgentEvent()

    data class ShowTaskDateTimeFullDialog(val task: Task?) : UrgentEvent()

    data class ChangeDate(val date: LocalDate? = null) : UrgentEvent()

    data class ChangeTime(val time: LocalTime? = null) : UrgentEvent()

    object SaveTaskDateTimeChanges : UrgentEvent()

    data class ChangeTaskPriority(val priority: Priority?) : UrgentEvent()

    data class ChangeTaskPriorityConfirm(val task: Task) : UrgentEvent()

    data class ChangeBoard(val board: SimpleBoard, val task: Task? = null) : UrgentEvent()

    object ResetBottomSheetContent : UrgentEvent()

    data class ChangeBottomSheetContent(val content: BottomSheetContent) : UrgentEvent()

    object ToggleCompletedTasksVisibility : UrgentEvent()

    data class ChangeNewTaskName(val name: String) : UrgentEvent()

    object SaveNewTask : UrgentEvent()

    object DismissBottomSheet : UrgentEvent()

    sealed class DialogType {
        object DeleteCompleted : DialogType()

        data class Priority(val task: Task) : DialogType()

        object TaskDate : DialogType()

        object TaskTime : DialogType()

        object DiscardChanges : DialogType()

        data class Error(@StringRes val message: Int) : DialogType()
    }

    sealed class BottomSheetContent {
        data class BoardsList(val task: Task? = null) : BottomSheetContent()

        object NewTask : BottomSheetContent()
    }
}
