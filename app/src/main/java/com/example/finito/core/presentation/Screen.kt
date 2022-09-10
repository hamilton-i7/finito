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

    object CreateBoard : Screen(route = "create_board")

    object EditBoard : Screen(
        route = "edit_board/{$BOARD_ROUTE_ARGUMENT}",
        prefix = "edit_board",
        arguments = listOf(navArgument(BOARD_ROUTE_ARGUMENT) { type = NavType.IntType })
    )

    object Label : Screen(
        route = "label/{$LABEL_ROUTE_ARGUMENT}",
        prefix = "label",
        arguments = listOf(navArgument(LABEL_ROUTE_ARGUMENT) { type = NavType.IntType })
    )

    object Archive : Screen(route = "archive")

    object Trash : Screen(route = "trash")

    object TaskDateTime : Screen(
        route = "task_date_time/{$TASK_ROUTE_ARGUMENT}",
        prefix = "task_date_time",
        arguments = listOf(navArgument(TASK_ROUTE_ARGUMENT) { type = NavType.IntType })
    )

    companion object {
        const val BOARD_ROUTE_ARGUMENT = "boardId"
        const val LABEL_ROUTE_ARGUMENT = "labelId"
        const val TASK_ROUTE_ARGUMENT = "taskId"
    }
}
