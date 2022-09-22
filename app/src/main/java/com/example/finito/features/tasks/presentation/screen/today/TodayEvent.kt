package com.example.finito.features.tasks.presentation.screen.today

import com.example.finito.core.domain.Priority
import com.example.finito.core.domain.util.SortingOption
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.tasks.domain.entity.TaskWithSubtasks

sealed class TodayEvent {
    data class ShowScreenMenu(val show: Boolean) : TodayEvent()

    object DeleteCompleted : TodayEvent()

    data class ToggleTaskCompleted(val task: TaskWithSubtasks) : TodayEvent()

    data class SortByPriority(val option: SortingOption.Priority?) : TodayEvent()

    data class ShowDialog(val type: DialogType? = null) : TodayEvent()

    data class ShowTaskDateTimeFullDialog(val task: TaskWithSubtasks?) : TodayEvent()

    object SaveTaskDateTimeChanges : TodayEvent()

    data class ChangeTaskPriority(val priority: Priority?) : TodayEvent()

    data class ChangeTaskPriorityConfirm(val task: TaskWithSubtasks) : TodayEvent()

    data class ChangeBoard(val board: SimpleBoard, val task: TaskWithSubtasks? = null) : TodayEvent()

    object ResetBottomSheetContent : TodayEvent()

    data class ChangeBottomSheetContent(val content: BottomSheetContent) : TodayEvent()

    object ToggleCompletedTasksVisibility : TodayEvent()

    data class ChangeNewTaskName(val name: String) : TodayEvent()

    object SaveNewTask : TodayEvent()

    sealed class DialogType {
        object DeleteCompleted : DialogType()

        data class Priority(val task: TaskWithSubtasks) : DialogType()

        object TaskDate : DialogType()

        object TaskTime : DialogType()
    }

    sealed class BottomSheetContent {
        data class BoardsList(val task: TaskWithSubtasks? = null) : BottomSheetContent()

        object NewTask : BottomSheetContent()
    }
}
