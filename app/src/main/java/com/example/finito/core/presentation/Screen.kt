package com.example.finito.core.presentation

import androidx.navigation.NamedNavArgument
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.finito.features.boards.domain.entity.BoardState

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
                defaultValue = BoardState.ACTIVE.name
            }
        )
    ) {
        val childRoutes = listOf(EditBoard.route)
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
    ) {
        val childRoutes = listOf(Board.route)
    }

    object CreateLabel : Screen(route = "create_label")

    object Archive : Screen(route = "archive") {
        val childRoutes = listOf(Board.route)
    }

    object Trash : Screen(route = "trash") {
        val childRoutes = listOf(Board.route)
    }

    companion object {
        const val BOARD_ROUTE_ID_ARGUMENT = "boardId"
        const val BOARD_ROUTE_STATE_ARGUMENT = "boardState"
        const val LABEL_ROUTE_ARGUMENT = "labelId"
        const val EDIT_TASK_ROUTE_ID_ARGUMENT = "taskId"
    }
}
