package com.example.finito.core.domain.util.menu

import com.example.finito.R

sealed class DeletedEditBoardScreenMenuOption(label: Int) : EditBoardScreenMenuOption(label) {
    object DeleteForever : DeletedEditBoardScreenMenuOption(label = R.string.delete_forever)
}