package com.example.finito.features.boards.presentation.addeditboard

import com.example.finito.features.labels.domain.entity.SimpleLabel

sealed class AddEditBoardEvent {
    data class ChangeName(val name: String) : AddEditBoardEvent()

    object ToggleLabelsVisibility : AddEditBoardEvent()

    data class AddLabel(val label: SimpleLabel) : AddEditBoardEvent()

    data class RemoveLabel(val label: SimpleLabel) : AddEditBoardEvent()

    object CreateBoard : AddEditBoardEvent()

    object EditBoard : AddEditBoardEvent()

    object DeleteBoard : AddEditBoardEvent()
}
