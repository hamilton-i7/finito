package com.example.finito.core.presentation.util.menu

import com.example.finito.R

sealed class LabelMenuOption(label: Int) : LabelScreenMenuOption(label) {
    object RenameLabel : LabelMenuOption(R.string.rename_label)

    object DeleteLabel : LabelMenuOption(R.string.delete_label)
}
