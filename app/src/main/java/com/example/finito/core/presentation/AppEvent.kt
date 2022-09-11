package com.example.finito.core.presentation

import androidx.compose.ui.text.input.TextFieldValue
import com.example.finito.core.presentation.util.DialogType

sealed class AppEvent {
    data class ShowSearchBar(val show: Boolean) : AppEvent()

    data class ShowTopBarMenu(val show: Boolean) : AppEvent()

    data class SearchBoards(val query: TextFieldValue) : AppEvent()

    data class ShowDialog(val type: DialogType? = null) : AppEvent()

    object ToggleLayout : AppEvent()
}
