package com.example.finito.core.domain.util.menu

import androidx.annotation.StringRes

sealed class MenuOption(@StringRes val label: Int)

sealed class BoardScreenMenuOption(label: Int) : MenuOption(label)

sealed class EditBoardScreenMenuOption(label: Int) : MenuOption(label)

sealed class BoardCardMenuOption(label: Int) : MenuOption(label)