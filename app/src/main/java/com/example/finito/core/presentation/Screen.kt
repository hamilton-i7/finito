package com.example.finito.core.presentation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument

sealed class Screen(
    val route: String,
    val prefix: String? = null,
    val arguments: List<NamedNavArgument> = emptyList(),
) {
    object Home : Screen(route = "home") {
        val childRoutes = listOf(
            Board.route,
            CreateBoard.route
        )
    }

    object Today : Screen(route = "today")

    object Tomorrow : Screen(route = "tomorrow")

    object Urgent : Screen(route = "urgent")

    object Board : Screen(
        route = "board/{$BOARD_ROUTE_ID_ARGUMENT}?$BOARD_ROUTE_STATE_ARGUMENT={$BOARD_ROUTE_STATE_ARGUMENT}",
        prefix = "board",
        arguments = listOf(
            navArgument(BOARD_ROUTE_ID_ARGUMENT) { type = NavType.IntType },
            navArgument(BOARD_ROUTE_STATE_ARGUMENT) {
                type = NavType.StringType
                nullable = true
            }
        )
    ) {
        val childRoutes = listOf(
            TaskDateTime.route,
            EditBoard.route
        )
        val parentRoutes = listOf(
            Home.route,
            Archive.route,
            Trash.route
        )
    }

    object CreateBoard : Screen(route = "create_board")

    object EditBoard : Screen(
        route = "edit_board/{$BOARD_ROUTE_ID_ARGUMENT}?$BOARD_ROUTE_STATE_ARGUMENT={$BOARD_ROUTE_STATE_ARGUMENT}",
        prefix = "edit_board",
        arguments = listOf(
            navArgument(BOARD_ROUTE_ID_ARGUMENT) { type = NavType.IntType },
            navArgument(BOARD_ROUTE_STATE_ARGUMENT) {
                type = NavType.StringType
                nullable = true
            }
        )
    )

    object Label : Screen(
        route = "label/{$LABEL_ROUTE_ARGUMENT}",
        prefix = "label",
        arguments = listOf(navArgument(LABEL_ROUTE_ARGUMENT) { type = NavType.IntType })
    )

    object Archive : Screen(route = "archive") {
        val childRoutes = listOf(Board.route)
    }

    object Trash : Screen(route = "trash") {
        val childRoutes = listOf(Board.route)
    }

    object TaskDateTime : Screen(
        route = "task_date_time/{$TASK_ROUTE_ARGUMENT}",
        prefix = "task_date_time",
        arguments = listOf(navArgument(TASK_ROUTE_ARGUMENT) { type = NavType.IntType })
    )

    companion object {
        const val BOARD_ROUTE_ID_ARGUMENT = "boardId"
        const val BOARD_ROUTE_STATE_ARGUMENT = "boardState"
        const val LABEL_ROUTE_ARGUMENT = "labelId"
        const val TASK_ROUTE_ARGUMENT = "taskId"
    }
}
