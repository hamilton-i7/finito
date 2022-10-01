package com.example.finito.core.presentation.util.menu

import com.example.finito.R

sealed class TodayScreenMenuOption(label: Int) : MenuOption(label) {
    object DeleteCompleted: TodayScreenMenuOption(R.string.delete_completed_tasks)
}
