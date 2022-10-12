package com.example.finito.features.tasks.presentation.screen.addedittask

import androidx.annotation.StringRes
import com.example.finito.core.presentation.util.SubtaskTextField
import com.example.finito.features.boards.domain.entity.SimpleBoard
import com.example.finito.features.tasks.domain.util.Priority
import org.burnoutcrew.reorderable.ItemPosition
import java.time.LocalDate
import java.time.LocalTime

sealed class AddEditTaskEvent {
    data class ChangeBoard(val board: SimpleBoard) : AddEditTaskEvent()

    data class ChangeName(val name: String) : AddEditTaskEvent()

    data class ChangeDescription(val description: String) : AddEditTaskEvent()

    data class ChangeDate(val date: LocalDate? = null) : AddEditTaskEvent()

    data class ChangeTime(val time: LocalTime? = null) : AddEditTaskEvent()

    data class ChangePriority(val priority: Priority? = null) : AddEditTaskEvent()

    data class ChangeSubtaskName(val id: Int, val name: String) : AddEditTaskEvent()

    object CreateSubtask : AddEditTaskEvent()

    data class RemoveSubtask(val state: SubtaskTextField) : AddEditTaskEvent()

    data class ReorderSubtasks(val from: ItemPosition, val to: ItemPosition) : AddEditTaskEvent()

    object CreateTask : AddEditTaskEvent()

    object EditTask : AddEditTaskEvent()

    object ToggleCompleted : AddEditTaskEvent()

    object RefreshTask : AddEditTaskEvent()

    object DeleteTask : AddEditTaskEvent()

    data class ShowDialog(val type: DialogType? = null) : AddEditTaskEvent()

    object AllowReminder : AddEditTaskEvent()

    sealed class DialogType {
        object Date : DialogType()

        object Time : DialogType()

        data class Error(@StringRes val message: Int) : DialogType()

        object NotificationsPermission : DialogType()
    }
}
