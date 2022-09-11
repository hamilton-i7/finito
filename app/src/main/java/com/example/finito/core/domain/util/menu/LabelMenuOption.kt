package com.example.finito.core.domain.util.menu

import com.example.finito.R

sealed class LabelMenuOption(label: Int) : MenuOption(label) {
    object RenameLabel : LabelMenuOption(R.string.rename_label)

    object DeleteLabel : LabelMenuOption(R.string.delete_label)
}
