package com.example.finito.core.presentation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val prefix: String? = null,
    val arguments: List<NamedNavArgument> = emptyList(),
) {
    object Home : Screen(route = "home")

    object Today : Screen(route = "today")

    object Tomorrow : Screen(route = "tomorrow")

    object Urgent : Screen(route = "urgent")

    object Board : Screen(
        route = "board/{$BOARD_ROUTE_ARGUMENT}",
        prefix = "board",
        arguments = listOf(navArgument(BOARD_ROUTE_ARGUMENT) { type = NavType.IntType })
    )

    object Label : Screen(
        route = "label/{$LABEL_ROUTE_ARGUMENT}",
        prefix = "label",
        arguments = listOf(navArgument(LABEL_ROUTE_ARGUMENT) { type = NavType.IntType })
    )

    object Archive : Screen(route = "archive")

    object Trash : Screen(route = "trash")

    companion object {
        const val BOARD_ROUTE_ARGUMENT = "boardId"
        const val LABEL_ROUTE_ARGUMENT = "labelId"
    }
}
