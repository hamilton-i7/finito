package com.example.finito.core.presentation

import androidx.compose.ui.text.input.TextFieldValue

sealed class AppEvent {
    data class ShowSearchBar(val show: Boolean) : AppEvent()

    data class SearchBoards(val query: TextFieldValue) : AppEvent()

    object ToggleLayout : AppEvent()
}
