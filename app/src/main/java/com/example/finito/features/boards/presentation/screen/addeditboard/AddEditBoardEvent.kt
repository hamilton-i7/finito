package com.example.finito.features.boards.presentation.screen.addeditboard

import androidx.annotation.StringRes
import com.example.finito.features.labels.domain.entity.SimpleLabel

sealed class AddEditBoardEvent {
    data class ChangeName(val name: String) : AddEditBoardEvent()

    object ToggleLabelsVisibility : AddEditBoardEvent()

    data class SelectLabel(val label: SimpleLabel) : AddEditBoardEvent()

    object CreateBoard : AddEditBoardEvent()

    object EditBoard : AddEditBoardEvent()

    object MoveBoardToTrash : AddEditBoardEvent()

    object DeleteForever : AddEditBoardEvent()

    data class RestoreBoard(val showSnackbar: Boolean = false) : AddEditBoardEvent()

    object UndoRestore : AddEditBoardEvent()

    object AlertNotEditable : AddEditBoardEvent()

    data class ShowScreenMenu(val show: Boolean) : AddEditBoardEvent()

    data class ShowDialog(val type: DialogType? = null) : AddEditBoardEvent()

    object RefreshBoard : AddEditBoardEvent()

    sealed class DialogType {
        object DeleteForever : DialogType()

        data class Error(@StringRes val message: Int) : DialogType()
    }
}
