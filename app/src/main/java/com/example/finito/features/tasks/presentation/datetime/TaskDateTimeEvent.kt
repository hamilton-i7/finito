package com.example.finito.features.tasks.presentation.datetime

import java.time.LocalDate
import java.time.LocalTime

sealed class TaskDateTimeEvent {
    data class ShowDialog(val type: DialogType? = null) : TaskDateTimeEvent()

    data class ChangeDate(val date: LocalDate? = null) : TaskDateTimeEvent()

    data class ChangeTime(val time: LocalTime? = null) : TaskDateTimeEvent()

    object SaveChanges : TaskDateTimeEvent()

    object DiscardChanges : TaskDateTimeEvent()

    sealed class DialogType {
        object TaskDate : DialogType()

        object TaskTime : DialogType()

        object DiscardChanges : DialogType()
    }
}
