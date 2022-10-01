package com.example.finito.core.presentation.util.menu

import com.example.finito.R

sealed class TomorrowScreenMenuOption(label: Int) : MenuOption(label) {
    object DeleteCompleted: TomorrowScreenMenuOption(R.string.delete_completed_tasks)
}
