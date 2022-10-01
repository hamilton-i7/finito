package com.example.finito.features.labels.presentation.screen.createlabel

sealed class CreateLabelEvent {
    data class ChangeName(val name: String) : CreateLabelEvent()

    object CreateLabel : CreateLabelEvent()
}
