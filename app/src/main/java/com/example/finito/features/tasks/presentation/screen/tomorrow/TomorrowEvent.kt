package com.example.finito.features.tasks.presentation.screen.tomorrow

import androidx.annotation.StringRes
import com.example.finito.core.domain.Priority
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import java.time.LocalDate
import java.time.LocalTime

sealed class TomorrowEvent {
    data class ShowScreenMenu(val show: Boolean) : TomorrowEvent()

    object DeleteCompleted : TomorrowEvent()

    data class ToggleTaskCompleted(val task: TaskWithSubtasks) : TomorrowEvent()

    data class ToggleSubtaskCompleted(val subtask: Subtask) : TomorrowEvent()

    data class SortByPriority(val option: SortingOption.Priority?) : TomorrowEvent()

    data class ShowDialog(val type: DialogType? = null) : TomorrowEvent()

    data class ShowTaskDateTimeFullDialog(val task: Task?) : TomorrowEvent()

    data class ChangeDate(val date: LocalDate? = null) : TomorrowEvent()

    data class ChangeTime(val time: LocalTime? = null) : TomorrowEvent()

    object SaveTaskDateTimeChanges : TomorrowEvent()

    data class ChangeTaskPriority(val priority: Priority?) : TomorrowEvent()

    data class ChangeTaskPriorityConfirm(val task: Task) : TomorrowEvent()

    data class ChangeBoard(val board: SimpleBoard, val task: Task? = null) : TomorrowEvent()

    object ResetBottomSheetContent : TomorrowEvent()

    data class ChangeBottomSheetContent(val content: BottomSheetContent) : TomorrowEvent()

    object ToggleCompletedTasksVisibility : TomorrowEvent()

    data class ChangeNewTaskName(val name: String) : TomorrowEvent()

    object SaveNewTask : TomorrowEvent()

    object DismissBottomSheet : TomorrowEvent()

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
