package com.example.finito.core.presentation.util.menu

import com.example.finito.R

sealed class UrgentScreenMenuOption(label: Int) : MenuOption(label) {
    object DeleteCompleted: UrgentScreenMenuOption(R.string.delete_completed_tasks)
}
