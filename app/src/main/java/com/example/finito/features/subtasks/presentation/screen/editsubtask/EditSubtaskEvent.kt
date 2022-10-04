package com.example.finito.features.subtasks.presentation.screen.editsubtask

import androidx.annotation.StringRes
import com.example.finito.features.boards.domain.entity.SimpleBoard

sealed class EditSubtaskEvent {
    data class ChangeBoard(val board: SimpleBoard) : EditSubtaskEvent()

    data class ChangeName(val name: String) : EditSubtaskEvent()

    data class ChangeDescription(val description: String) : EditSubtaskEvent()

    object EditSubtask : EditSubtaskEvent()

    object ToggleCompleted : EditSubtaskEvent()

    object RefreshSubtask : EditSubtaskEvent()

    object DeleteSubtask : EditSubtaskEvent()

    data class ShowDialog(val type: DialogType? = null) : EditSubtaskEvent()

    sealed class DialogType {
        data class Error(@StringRes val message: Int) : DialogType()
    }
}
