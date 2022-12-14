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
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.presentation.screen.addeditboard.AddEditBoardScreen
import com.example.finito.features.boards.presentation.screen.board.BoardScreen
import com.google.accompanist.navigation.animation.composable

const val BOARD_GRAPH_ROUTE = "board_flow/{${Screen.BOARD_ID_ARGUMENT}}" +
        "?${Screen.BOARD_STATE_ARGUMENT}" +
        "={${Screen.BOARD_STATE_ARGUMENT}}"

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
                    Screen.EditBoard.route, Screen.EditSubtask.route -> {
                        childScreenPopEnterTransition()
                    }
                    else -> peerScreenEnterTransition()
                }
            },
            exitTransition = {
                when (targetState.destination.route) {
                    Screen.EditBoard.route, Screen.CreateTask.route,
                    Screen.EditTask.route, Screen.EditSubtask.route -> {
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
                onShowSnackbar = onShowSnackbar,
                previousRoute = navController.previousBackStackEntry?.destination?.route,
                onNavigateToHome = { navController.navigateToHome() },
                onNavigateBack = { navController.navigateUp() },
                onNavigateToEditBoard = { boardId, boardState ->
                    navController.navigateToEditBoard(boardId, boardState)
                },
                onNavigateToCreateTask = { boardId, taskName ->
                    taskName?.let {
                        navController.navigateToCreateTask(boardId, taskName = it)
                    } ?: navController.navigateToCreateTask(boardId)
                },
                onNavigateToEditTask = { taskId ->
                    navController.navigateToEditTask(taskId)
                },
                onNavigateToEditSubtask = { boardId, subtaskId ->
                    navController.navigateToEditSubtask(boardId, subtaskId)
                }
            )
        }

        composable(
            route = Screen.EditBoard.route,
            arguments = Screen.EditBoard.arguments,
            enterTransition = childScreenEnterTransition,
            exitTransition = childScreenExitTransition,
            popExitTransition = childScreenPopExitTransition
        ) { backStackEntry ->
            AddEditBoardScreen(
                onShowSnackbar = onShowSnackbar,
                appViewModel = appViewModel,
                createMode = false,
                onNavigateBack = { navController.navigateUp() },
                onNavigateBackTwice = onNavigateBackTwice@{
                    backStackEntry.arguments?.getString(Screen.BOARD_STATE_ARGUMENT)?.let { state ->
                        if (state != BoardState.ACTIVE.name) return@let
                        navController.navigateBackTwice()
                        val currentRoute = navController.currentBackStackEntry?.destination?.route
                        if (currentRoute == Screen.Label.route) return@onNavigateBackTwice

                        navController.navigateToHome()
                        return@onNavigateBackTwice
                    }
                    navController.navigateBackTwice()
                },
                onNavigateToBoard = { boardId, boardState ->
                    navController.navigateToBoard(boardId, boardState)
                },
            )
        }
    }
}