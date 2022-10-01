package com.example.finito.features.subtasks.presentation.screen.addeditsubtask

import androidx.annotation.StringRes
import com.example.finito.features.subtasks.domain.entity.Subtask

sealed class AddEditSubtaskEvent {
    data class ChangeBoard(val subtask: Subtask) : AddEditSubtaskEvent()

    data class ChangeName(val name: String) : AddEditSubtaskEvent()

    data class ChangeDescription(val description: String) : AddEditSubtaskEvent()

    object CreateSubtask : AddEditSubtaskEvent()

    object EditSubtask : AddEditSubtaskEvent()

    object ToggleCompleted : AddEditSubtaskEvent()

    object RefreshSubtask : AddEditSubtaskEvent()

    object DeleteSubtask : AddEditSubtaskEvent()

    data class ShowDialog(val type: DialogType? = null) : AddEditSubtaskEvent()

    sealed class DialogType {
        data class Error(@StringRes val message: Int) : DialogType()
    }
}
