package com.example.finito.core.presentation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val arguments: List<NamedNavArgument> = emptyList(),
) {
    object Home : Screen(route = "home")

    object Today : Screen(route = "today")

    object Tomorrow : Screen(route = "tomorrow")

    object Urgent : Screen(route = "urgent")

    object Board : Screen(
        route = "$BOARD_ROUTE_PREFIX/{$BOARD_ROUTE_ARGUMENT}",
        arguments = listOf(navArgument(BOARD_ROUTE_ARGUMENT) { type = NavType.IntType })
    )

    object Archive : Screen(route = "archive")

    object Trash : Screen(route = "trash")

    companion object {
        const val BOARD_ROUTE_PREFIX = "board"
        const val BOARD_ROUTE_ARGUMENT = "boardId"
    }
}
