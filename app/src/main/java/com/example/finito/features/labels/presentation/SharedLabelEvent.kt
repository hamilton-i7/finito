package com.example.finito.features.labels.presentation

import com.example.finito.features.labels.domain.entity.Label

sealed class SharedLabelEvent {
    data class ShowDialog(val type: DialogType? = null) : SharedLabelEvent()

    data class ChangeName(val name: String) : SharedLabelEvent()

    object CreateLabel : SharedLabelEvent()

    data class DeleteLabel(val label: Label) : SharedLabelEvent()

    sealed class DialogType {
        object CreateLabel : DialogType()

        object EditLabel : DialogType()

        object DeleteLabel : DialogType()
    }
}
