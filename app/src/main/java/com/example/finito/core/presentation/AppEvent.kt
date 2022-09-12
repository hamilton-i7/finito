package com.example.finito.core.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.example.finito.core.presentation.util.DialogType

sealed class AppEvent<S: Screen> {
    sealed class Generic : AppEvent<Screen>() {
        data class ShowBottomBar(val show: Boolean) : Generic()

        data class ChangeDynamicTopBarTitle(val title: String) : Generic()
    }

    sealed class Home : AppEvent<Screen.Home>() {
        data class ShowSearchBar(val show: Boolean) : Home()

        data class SearchBoards(val query: TextFieldValue) : Home()

        object ToggleLayout : Home()
    }

    sealed class Archive : AppEvent<Screen.Archive>() {
        data class ShowSearchBar(val show: Boolean) : Archive()

        data class SearchBoards(val query: TextFieldValue) : Archive()

        object ToggleLayout : Archive()
    }

    sealed class Trash : AppEvent<Screen.Trash>() {
        data class ShowTopBarMenu(val show: Boolean) : Trash()

        data class ShowDialog(val type: DialogType? = null) : Trash()
    }

    sealed class Board : AppEvent<Screen.Board>() {
        data class ShowTopBarMenu(val show: Boolean) : Board()

        data class ShowDialog(val type: DialogType? = null) : Board()

        object ArchiveBoard : Board()

        object DeleteBoard : Board()

        object DeleteCompletedTasks : Board()
    }
}
