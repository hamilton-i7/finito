package com.example.finito.core.presentation.util.menu

import com.example.finito.R

sealed class TaskReminderOption(label: Int) : MenuOption(label) {
    object FiveMinutes : TaskReminderOption(R.string.five_minutes_before)
    object TenMinutes : TaskReminderOption(R.string.ten_minutes_before)
    object FifteenMinutes : TaskReminderOption(R.string.fifteen_minutes_before)
    object ThirtyMinutes : TaskReminderOption(R.string.thirty_minutes_before)
}
