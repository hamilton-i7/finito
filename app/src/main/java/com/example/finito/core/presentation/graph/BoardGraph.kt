package com.example.finito.core.presentation.graph

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.navigation
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.*
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenExitTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenPopEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenPopExitTransition
import com.example.finito.core.presentation.util.NavigationTransitions.peerScreenEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.peerScreenExitTransition
import com.example.finito.features.boards.presentation.screen.addeditboard.AddEditBoardScreen
import com.example.finito.features.boards.presentation.screen.board.BoardScreen
import com.google.accompanist.navigation.animation.composable

const val BOARD_GRAPH_ROUTE = "board_flow/{${Screen.BOARD_ROUTE_ID_ARGUMENT}}" +
        "?${Screen.BOARD_ROUTE_STATE_ARGUMENT}" +
        "={${Screen.BOARD_ROUTE_STATE_ARGUMENT}}"

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
fun NavGraphBuilder.boardGraph(
    navController: NavController,
    drawerState: DrawerState,
    appViewModel: AppViewModel,
    onShowSnackbar: (message: Int, actionLabel: Int?, onActionClick: () -> Unit) -> Unit,
) {
    navigation(startDestination = Screen.Board.route, route = BOARD_GRAPH_ROUTE) {
        composable(
            route = Screen.Board.route,
            arguments = Screen.Board.arguments,
            enterTransition = {
                when (initialState.destination.route) {
                    Screen.Home.route, Screen.Archive.route,
                    Screen.Trash.route, Screen.Label.route -> childScreenEnterTransition()
                    Screen.EditBoard.route, Screen.CreateTask.route,
                    Screen.EditTask.route -> childScreenPopEnterTransition()
                    else -> peerScreenEnterTransition()
                }
            },
            popEnterTransition = {
                when (initialState.destination.route) {
                    Screen.EditBoard.route -> {
                        childScreenPopEnterTransition()
                    }
                    else -> peerScreenEnterTransition()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.EditBoard.route, Screen.CreateTask.route,
                    Screen.EditTask.route -> {
                        childScreenExitTransition()
                    }
                    else -> peerScreenExitTransition()
                }
            },
            popExitTransition = childScreenPopExitTransition
        ) {
            BoardScreen(
                drawerState = drawerState,
                appViewModel = appViewModel,
                showSnackbar = onShowSnackbar,
                previousRoute = navController.previousBackStackEntry?.destination?.route,
                onNavigateToHome = { navController.navigateToHome() },
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEditBoard = { boardId, boardState ->
                    navController.navigateToEditBoard(boardId, boardState)
                },
                onNavigateToCreateTask = { boardId, taskName ->
                    navController.navigateToCreateTask(boardId, taskName)
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigateToEditTask(taskId)
                }
            )
        }

        composable(
            route = Screen.EditBoard.route,
            arguments = Screen.EditBoard.arguments,
            enterTransition = childScreenEnterTransition,
            exitTransition = childScreenExitTransition,
            popExitTransition = childScreenPopExitTransition
        ) {
            AddEditBoardScreen(
                onShowSnackbar = onShowSnackbar,
                appViewModel = appViewModel,
                createMode = false,
                onNavigateBack = { navController.navigateUp() },
                onNavigateToHome = { navController.navigateToHome() },
                onNavigateToArchive = { navController.navigateToArchive() },
                onNavigateToTrash = { navController.navigateToTrash() },
                onNavigateToBoard = { boardId, boardState ->
                    navController.navigateToBoard(boardId, boardState)
                },
            )
        }
    }
}