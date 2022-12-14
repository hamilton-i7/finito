package com.example.finito.core.presentation

import androidx.navigation.*
import com.example.finito.features.boards.domain.entity.BoardState

sealed class Screen(
    val route: String,
    val prefix: String? = null,
    val arguments: List<NamedNavArgument> = emptyList(),
    val deepLinks: List<NavDeepLink> = emptyList(),
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
        route = "board/{$BOARD_ID_ARGUMENT}?$BOARD_STATE_ARGUMENT={$BOARD_STATE_ARGUMENT}",
        prefix = "board",
        arguments = listOf(
            navArgument(BOARD_ID_ARGUMENT) { type = NavType.IntType },
            navArgument(BOARD_STATE_ARGUMENT) {
                type = NavType.StringType
                defaultValue = BoardState.ACTIVE.name
            }
        )
    )

    object CreateBoard : Screen(route = "create_board")

    object EditBoard : Screen(
        route = "edit_board/{$BOARD_ID_ARGUMENT}" +
                "?$BOARD_STATE_ARGUMENT={$BOARD_STATE_ARGUMENT}",
        prefix = "edit_board",
        arguments = listOf(
            navArgument(BOARD_ID_ARGUMENT) { type = NavType.IntType },
            navArgument(BOARD_STATE_ARGUMENT) {
                type = NavType.StringType
                nullable = true
            },
        )
    )

    object Label : Screen(
        route = "label/{$LABEL_ID_ARGUMENT}",
        prefix = "label",
        arguments = listOf(navArgument(LABEL_ID_ARGUMENT) { type = NavType.IntType })
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

    object CreateTask : Screen(
        route = "create_task?" +
                "$BOARD_ID_ARGUMENT={$BOARD_ID_ARGUMENT}" +
                "&$TASK_NAME_ARGUMENT={$TASK_NAME_ARGUMENT}" +
                "&$DATE_ARGUMENT={$DATE_ARGUMENT}" +
                "&$IS_URGENT_ARGUMENT={$IS_URGENT_ARGUMENT}",
        prefix = "create_task",
        arguments = listOf(
            navArgument(BOARD_ID_ARGUMENT) {
                type = NavType.IntType
                defaultValue = -1
            },
            navArgument(TASK_NAME_ARGUMENT) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(DATE_ARGUMENT) {
                type = NavType.StringType
                defaultValue = ""
            },
            navArgument(IS_URGENT_ARGUMENT) {
                type = NavType.BoolType
                defaultValue = false
            }
        )
    )

    object EditTask : Screen(
        route = "edit_task/{$TASK_ID_ARGUMENT}",
        prefix = "edit_task",
        arguments = listOf(
            navArgument(TASK_ID_ARGUMENT) { type = NavType.IntType },
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "$URI/$TASK_ID_ARGUMENT={$TASK_ID_ARGUMENT}" }
        )
    )

    object EditSubtask : Screen(
        route = "edit_subtask/{$SUBTASK_ID_ARGUMENT}?$BOARD_ID_ARGUMENT={$BOARD_ID_ARGUMENT}",
        prefix = "edit_subtask",
        arguments = listOf(
            navArgument(SUBTASK_ID_ARGUMENT) { type = NavType.IntType },
            navArgument(BOARD_ID_ARGUMENT) {
                type = NavType.IntType
                defaultValue = -1
            }
        )
    )

    companion object {
        const val BOARD_ID_ARGUMENT = "boardId"
        const val BOARD_STATE_ARGUMENT = "boardState"
        const val LABEL_ID_ARGUMENT = "labelId"
        const val TASK_ID_ARGUMENT = "taskId"
        const val TASK_NAME_ARGUMENT = "taskName"
        const val DATE_ARGUMENT = "includeDate"
        const val IS_URGENT_ARGUMENT = "isUrgent"
        const val SUBTASK_ID_ARGUMENT = "subtaskId"
        const val URI = "https://example.com"
    }
}
