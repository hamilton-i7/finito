package com.example.finito.core.presentation

sealed class AppEvent {
    data class ShowSearchBar(val show: Boolean) : AppEvent()

    data class SearchBoards(val query: String) : AppEvent()

    object ToggleLayout : AppEvent()
}
