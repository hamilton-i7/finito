package com.example.finito.features.tasks.presentation.screen.today

import androidx.annotation.StringRes
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.subtasks.domain.entity.Subtask
import com.example.finito.features.tasks.domain.entity.Task
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks
import com.example.finito.features.tasks.domain.util.Priority
import java.time.LocalDate
import java.time.LocalTime

sealed class TodayEvent {
    data class ShowScreenMenu(val show: Boolean) : TodayEvent()

    object DeleteCompleted : TodayEvent()

    data class ToggleTaskCompleted(val task: TaskWithSubtasks) : TodayEvent()

    data class ToggleSubtaskCompleted(val subtask: Subtask) : TodayEvent()

    data class SortByPriority(val option: SortingOption.Priority?) : TodayEvent()

    data class ShowDialog(val type: DialogType? = null) : TodayEvent()

    data class ShowTaskDateTimeFullDialog(val task: Task?) : TodayEvent()

    data class ChangeDate(val date: LocalDate? = null) : TodayEvent()

    data class ChangeTime(val time: LocalTime? = null) : TodayEvent()

    object SaveTaskDateTimeChanges : TodayEvent()

    data class ChangeTaskPriority(val priority: Priority?) : TodayEvent()

    data class ChangeTaskPriorityConfirm(val task: Task) : TodayEvent()

    data class ChangeBoard(val board: SimpleBoard, val task: Task? = null) : TodayEvent()

    object ResetBottomSheetContent : TodayEvent()

    data class ChangeBottomSheetContent(val content: BottomSheetContent) : TodayEvent()

    object ToggleCompletedTasksVisibility : TodayEvent()

    data class ChangeNewTaskName(val name: String) : TodayEvent()

    object SaveNewTask : TodayEvent()

    object DismissBottomSheet : TodayEvent()

    object CreateBoard : TodayEvent()

    sealed class DialogType {
        object DeleteCompleted : DialogType()

        data class Priority(val task: Task) : DialogType()

        object TaskDate : DialogType()

        object TaskTime : DialogType()

        object DiscardChanges : DialogType()

        data class Error(@StringRes val message: Int) : DialogType()

        object CreateBoard : DialogType()
    }

    sealed class BottomSheetContent {
        data class BoardsList(val task: Task? = null) : BottomSheetContent()

        object NewTask : BottomSheetContent()
    }
}
