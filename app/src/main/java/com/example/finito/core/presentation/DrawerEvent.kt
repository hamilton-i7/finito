package com.example.finito.core.presentation

sealed class DrawerEvent {
    data class ChangeRoute(val route: String) : DrawerEvent()

    object ToggleBoardsExpanded : DrawerEvent()

    object ToggleLabelsExpanded : DrawerEvent()
}
