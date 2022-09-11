package com.example.finito.core.domain.util.menu

import com.example.finito.R

sealed class TrashScreenMenuOption(label: Int) : MenuOption(label) {
    object EmptyTrash : TrashScreenMenuOption(label = R.string.empty_trash)
}
