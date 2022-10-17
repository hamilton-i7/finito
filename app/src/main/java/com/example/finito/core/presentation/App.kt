package com.example.finito.core.presentation

import android.annotation.SuppressLint
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.dialog
import com.example.finito.core.presentation.components.Drawer
import com.example.finito.core.presentation.graph.boardGraph
import com.example.finito.core.presentation.util.*
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenExitTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenPopEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.childScreenPopExitTransition
import com.example.finito.core.presentation.util.NavigationTransitions.peerScreenEnterTransition
import com.example.finito.core.presentation.util.NavigationTransitions.peerScreenExitTransition
import com.example.finito.features.boards.domain.entity.BoardState
import com.example.finito.features.boards.presentation.screen.addeditboard.AddEditBoardScreen
import com.example.finito.features.boards.presentation.screen.archive.ArchiveScreen
import com.example.finito.features.boards.presentation.screen.home.HomeScreen
import com.example.finito.features.boards.presentation.screen.searchboard.SearchBoardScreen
import com.example.finito.features.boards.presentation.screen.trash.TrashScreen
import com.example.finito.features.labels.presentation.screen.createlabel.CreateLabelContent
import com.example.finito.features.labels.presentation.screen.label.LabelScreen
import com.example.finito.features.subtasks.presentation.screen.editsubtask.EditSubtaskScreen
import com.example.finito.features.tasks.presentation.screen.addedittask.AddEditTaskScreen
import com.example.finito.features.tasks.presentation.screen.today.TodayScreen
import com.example.finito.features.tasks.presentation.screen.tomorrow.TomorrowScreen
import com.example.finito.features.tasks.presentation.screen.urgent.UrgentScreen
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import kotlinx.coroutines.launch
import java.time.LocalDate

private val staticDrawerRoutes = listOf(
    Screen.Home.route,
    Screen.Today.route,
    Screen.Tomorrow.route,
    Screen.Urgent.route,
    Screen.Archive.route,
    Screen.Trash.route,
)
private val dynamicDrawerRoutes = listOf(
    Screen.Board.route,
    Screen.Label.route
)

private val drawerRoutes = staticDrawerRoutes + dynamicDrawerRoutes

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun App(
    drawerState: DrawerState = rememberDrawerState(initialValue = DrawerValue.Closed),
    appViewModel: AppViewModel = hiltViewModel(),
    drawerViewModel: DrawerViewModel  = hiltViewModel(),
    snackbarState: SnackbarState = rememberSnackbarState(),
    navController: NavHostController = rememberAnimatedNavController(),
    finishActivity: () -> Unit = {},
) {
    val context = LocalContext.current
    val (snackbarHostState, scope) = snackbarState
    val onShowSnackbar: (Int, Int?, () -> Unit) -> Unit = { message, actionLabel, onActionClick ->
        snackbarState.showSnackbar(
            context = context,
            message = message,
            actionLabel = actionLabel,
            onActionClick = onActionClick
        )
    }

    // Dynamically change Snackbar bottom padding
    var currentRoute by remember { mutableStateOf(navController.currentDestination?.route) }
    val snackbarModifier = when(currentRoute) {
        Screen.Today.route, Screen.Tomorrow.route,
        Screen.Urgent.route, Screen.Home.route,
        Screen.Archive.route, Screen.Label.route -> Modifier
            .navigationBarsPadding()
            .padding(bottom = FabPadding)
        Screen.Board.route -> {
            if (navController.previousBackStackEntry?.destination?.route == Screen.Trash.route) {
                Modifier.navigationBarsPadding()
            } else {
                Modifier
                    .navigationBarsPadding()
                    .padding(bottom = FabPadding)
            }
        }
        else -> Modifier.navigationBarsPadding()
    }

    navController.addOnDestinationChangedListener(
        listener = { _, destination, arguments ->
            currentRoute = destination.route

            if (!drawerRoutes.contains(destination.route)) {
                return@addOnDestinationChangedListener
            }

            val route: String = if (staticDrawerRoutes.contains(destination.route)) {
                destination.route!!
            } else if (destination.route == Screen.Board.route) {
                "${Screen.Board.prefix}/${arguments?.getInt(Screen.BOARD_ID_ARGUMENT)}"
            } else {
                "${Screen.Label.prefix}/${arguments?.getInt(Screen.LABEL_ID_ARGUMENT)}"
            }
            drawerViewModel.onEvent(DrawerEvent.ChangeRoute(route))
        }
    )

    Drawer(
        drawerState = drawerState,
        isSelectedScreen = {
            drawerViewModel.currentRoute == it
        },
        boards = drawerViewModel.boards,
        expandBoards = drawerViewModel.boardsExpanded,
        onExpandBoardsChange = {
            drawerViewModel.onEvent(DrawerEvent.ToggleBoardsExpanded)
        },
        labels = drawerViewModel.labels,
        expandLabels = drawerViewModel.labelsExpanded,
        onExpandLabelsChange = {
            drawerViewModel.onEvent(DrawerEvent.ToggleLabelsExpanded)
        },
        onBoardSelected = onBoardSelected@{ boardId ->
            val route = "${Screen.Board.prefix}/$boardId"
            if (drawerViewModel.currentRoute == route) {
                scope.launch { drawerState.close() }
                return@onBoardSelected
            }
            scope.launch {
                drawerState.close()
                navController.navigateToBoard(boardId)
            }
        },
        onLabelSelected = onLabelSelected@{ labelId ->
            val route = "${Screen.Label.prefix}/$labelId"
            if (drawerViewModel.currentRoute == route) {
                scope.launch { drawerState.close() }
                return@onLabelSelected
            }
            scope.launch {
                drawerState.close()
                navController.navigateToLabel(labelId)
            }
        },
        onStaticItemSelected = onStaticItemSelected@{ route ->
            if (drawerViewModel.currentRoute == route) {
                scope.launch { drawerState.close() }
                return@onStaticItemSelected
            }
            scope.launch {
                drawerState.close()
                navController.navigate(route)
            }
        }
    ) {
        // TODO: Add semantic descriptions to screen elements (accessibility)
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = snackbarModifier.testTag(TestTags.SNACKBAR)
                )
            },
        ) {
            AnimatedNavHost(
                navController = navController,
                startDestination = Screen.Home.route
            ) {
                composable(
                    route = Screen.Home.route,
                    enterTransition = {
                        when {
                            Screen.Home.childRoutes.contains(initialState.destination.route) -> {
                                childScreenPopEnterTransition()
                            }
                            else -> peerScreenEnterTransition()
                        }
                    },
                    exitTransition = {
                        when {
                            Screen.Home.childRoutes.contains(targetState.destination.route) -> {
                                childScreenExitTransition()
                            }
                            else -> peerScreenExitTransition()
                        }
                    },
                ) {
                    HomeScreen(
                        appViewModel = appViewModel,
                        drawerState = drawerState,
                        finishActivity = finishActivity,
                        onShowSnackbar = onShowSnackbar,
                        onNavigateToCreateBoard = { navController.navigateToCreateBoard() },
                        onNavigateToBoard = { navController.navigateToBoardFlow(it) },
                        onNavigateToSearchBoards = { navController.navigateToSearchBoards() }
                    )
                }

                composable(
                    route = Screen.Today.route,
                    enterTransition = {
                        when(initialState.destination.route) {
                            Screen.CreateTask.route, Screen.EditTask.route,
                            Screen.EditSubtask.route, Screen.CreateBoard.route -> {
                                childScreenPopEnterTransition()
                            }
                            else -> peerScreenEnterTransition()
                        }
                    },
                    exitTransition = {
                        when(targetState.destination.route) {
                            Screen.CreateTask.route, Screen.EditTask.route,
                            Screen.EditSubtask.route, Screen.CreateBoard.route -> {
                                childScreenExitTransition()
                            }
                            else -> peerScreenExitTransition()
                        }
                    },
                ) {
                    TodayScreen(
                        drawerState = drawerState,
                        appViewModel = appViewModel,
                        onNavigateToCreateTask = { boardId, taskName ->
                            val today = LocalDate.now()
                            taskName?.let {
                                navController.navigateToCreateTask(boardId, taskName = it, date = today)
                            } ?: navController.navigateToCreateTask(boardId, date = today)
                        },
                        onNavigateToEditTask = { taskId ->
                            navController.navigateToEditTask(taskId)
                        },
                        onNavigateToEditSubtask = { boardId, subtaskId ->
                            navController.navigateToEditSubtask(boardId, subtaskId)
                        },
                        onNavigateToCreateBoard = {
                            navController.navigateToCreateBoard()
                        },
                        finishActivity = finishActivity,
                        onShowSnackbar = onShowSnackbar
                    )
                }

                composable(
                    route = Screen.Tomorrow.route,
                    enterTransition = {
                        when(initialState.destination.route) {
                            Screen.CreateTask.route, Screen.EditTask.route,
                            Screen.EditSubtask.route -> {
                                childScreenPopEnterTransition()
                            }
                            else -> peerScreenEnterTransition()
                        }
                    },
                    exitTransition = {
                        when(targetState.destination.route) {
                            Screen.CreateTask.route, Screen.EditTask.route,
                            Screen.EditSubtask.route -> {
                                childScreenExitTransition()
                            }
                            else -> peerScreenExitTransition()
                        }
                    },
                ) {
                    TomorrowScreen(
                        drawerState = drawerState,
                        appViewModel = appViewModel,
                        onNavigateToCreateTask = { boardId, taskName ->
                            val tomorrow = LocalDate.now().plusDays(1)
                            taskName?.let {
                                navController.navigateToCreateTask(boardId, taskName = it, date = tomorrow)
                            } ?: navController.navigateToCreateTask(boardId, date = tomorrow)
                        },
                        onNavigateToEditTask = { taskId ->
                            navController.navigateToEditTask(taskId)
                        },
                        onNavigateToEditSubtask = { boardId, subtaskId ->
                            navController.navigateToEditSubtask(boardId, subtaskId)
                        },
                        onNavigateToCreateBoard = {
                            navController.navigateToCreateBoard()
                        },
                        finishActivity = finishActivity,
                        onShowSnackbar = onShowSnackbar
                    )
                }

                composable(
                    route = Screen.Urgent.route,
                    enterTransition = {
                        when(initialState.destination.route) {
                            Screen.CreateTask.route, Screen.EditTask.route,
                            Screen.EditSubtask.route -> {
                                childScreenPopEnterTransition()
                            }
                            else -> peerScreenEnterTransition()
                        }
                    },
                    exitTransition = {
                        when(targetState.destination.route) {
                            Screen.CreateTask.route, Screen.EditTask.route,
                            Screen.EditSubtask.route -> {
                                childScreenExitTransition()
                            }
                            else -> peerScreenExitTransition()
                        }
                    },
                ) {
                    UrgentScreen(
                        drawerState = drawerState,
                        appViewModel = appViewModel,
                        onNavigateToCreateTask = { boardId, taskName ->
                            taskName?.let {
                                navController.navigateToCreateTask(boardId, taskName = it, isUrgent = true)
                            } ?: navController.navigateToCreateTask(boardId, isUrgent = true)
                        },
                        onNavigateToEditTask = { taskId ->
                            navController.navigateToEditTask(taskId)
                        },
                        onNavigateToEditSubtask = { boardId, subtaskId ->
                            navController.navigateToEditSubtask(boardId, subtaskId)
                        },
                        onNavigateToCreateBoard = {
                            navController.navigateToCreateBoard()
                        },
                        finishActivity = finishActivity,
                        onShowSnackbar = onShowSnackbar
                    )
                }

                composable(
                    route = Screen.Archive.route,
                    enterTransition = peerScreenEnterTransition,
                    exitTransition = {
                        when {
                            Screen.Archive.childRoutes.contains(targetState.destination.route) -> {
                                childScreenExitTransition()
                            }
                            else -> peerScreenExitTransition()
                        }
                    },
                    popEnterTransition = {
                        when {
                            Screen.Archive.childRoutes.contains(initialState.destination.route) -> {
                                childScreenPopEnterTransition()
                            }
                            else -> peerScreenEnterTransition()
                        }
                    }
                ) {
                    ArchiveScreen(
                        appViewModel = appViewModel,
                        drawerState = drawerState,
                        finishActivity = finishActivity,
                        onShowSnackbar = onShowSnackbar,
                        onNavigateToBoardFlow = {
                            navController.navigateToBoardFlow(it, BoardState.ARCHIVED)
                        }
                    )
                }

                composable(
                    route = Screen.Trash.route,
                    enterTransition = peerScreenEnterTransition,
                    exitTransition = {
                        when {
                            Screen.Trash.childRoutes.contains(targetState.destination.route) -> {
                                childScreenExitTransition()
                            }
                            else -> peerScreenExitTransition()
                        }
                    },
                    popEnterTransition = {
                        when {
                            Screen.Trash.childRoutes.contains(initialState.destination.route) -> {
                                childScreenPopEnterTransition()
                            }
                            else -> peerScreenEnterTransition()
                        }
                    }
                ) {
                    TrashScreen(
                        appViewModel = appViewModel,
                        drawerState = drawerState,
                        finishActivity = finishActivity,
                        onShowSnackbar = onShowSnackbar,
                        onNavigateToBoardFlow = {
                            navController.navigateToBoardFlow(it, BoardState.DELETED)
                        }
                    )
                }

                boardGraph(
                    navController = navController,
                    drawerState = drawerState,
                    appViewModel = appViewModel,
                    onShowSnackbar = onShowSnackbar
                )

                composable(
                    route = Screen.SearchBoards.route,
                    enterTransition = childScreenEnterTransition,
                    exitTransition = childScreenExitTransition,
                    popExitTransition = childScreenPopExitTransition
                ) {
                    SearchBoardScreen(
                        onNavigateBack = { navController.navigateUp() }
                    )
                }

                composable(
                    route = Screen.CreateBoard.route,
                    enterTransition = childScreenEnterTransition,
                    exitTransition = childScreenExitTransition,
                    popExitTransition = childScreenPopExitTransition
                ) {
                    AddEditBoardScreen(
                        onShowSnackbar = onShowSnackbar,
                        appViewModel = appViewModel,
                        createMode = true,
                        previousRoute = navController.previousBackStackEntry?.destination?.route,
                        onNavigateBack = { navController.navigateUp() },
                        onNavigateToBoardFlow = { navController.navigateToBoardFlow(it) }
                    )
                }

                composable(
                    route = Screen.Label.route,
                    arguments = Screen.Label.arguments,
                    enterTransition = peerScreenEnterTransition,
                    exitTransition = {
                        when {
                            Screen.Label.childRoutes.contains(targetState.destination.route) -> {
                                childScreenExitTransition()
                            }
                            else -> peerScreenExitTransition()
                        }
                    },
                    popEnterTransition = {
                        when {
                            Screen.Label.childRoutes.contains(initialState.destination.route) -> {
                                childScreenPopEnterTransition()
                            }
                            else -> peerScreenEnterTransition()
                        }
                    }
                ) {
                    LabelScreen(
                        appViewModel = appViewModel,
                        drawerState = drawerState,
                        onShowSnackbar = onShowSnackbar,
                        onNavigateToHome = { navController.navigateToHome() },
                        onNavigateToCreateBoard = { navController.navigateToCreateBoard() },
                        onNavigateToBoardFlow = { navController.navigateToBoardFlow(it) }
                    )
                }

                dialog(Screen.CreateLabel.route) {
                    CreateLabelContent(
                        onNavigateBack = { navController.navigateUp() },
                        onShowSnackbar = onShowSnackbar
                    )
                }

                composable(
                    route = Screen.CreateTask.route,
                    arguments = Screen.CreateTask.arguments,
                    enterTransition = childScreenEnterTransition,
                    exitTransition = childScreenExitTransition
                ) {
                    AddEditTaskScreen(
                        createMode = true,
                        appViewModel = appViewModel,
                        previousRoute = navController.previousBackStackEntry?.destination?.route ?: "",
                        onNavigateBack = { navController.navigateUp() },
                        onNavigateToBoard = { boardId, boardState ->
                            navController.navigateToBoard(boardId, boardState)
                        }
                    )
                }

                composable(
                    route = Screen.EditTask.route,
                    arguments = Screen.EditTask.arguments,
                    deepLinks = Screen.EditTask.deepLinks,
                    enterTransition = childScreenEnterTransition,
                    exitTransition = childScreenExitTransition
                ) {
                    AddEditTaskScreen(
                        createMode = false,
                        appViewModel = appViewModel,
                        previousRoute = navController.previousBackStackEntry?.destination?.route ?: "",
                        onNavigateBack = { navController.navigateUp() },
                        onNavigateToBoard = { boardId, boardState ->
                            navController.navigateToBoard(boardId, boardState)
                        },
                        onShowSnackbar = onShowSnackbar
                    )
                }

                composable(
                    route = Screen.EditSubtask.route,
                    arguments = Screen.EditSubtask.arguments,
                    enterTransition = childScreenEnterTransition,
                    exitTransition = childScreenExitTransition
                ) {
                    EditSubtaskScreen(
                        appViewModel = appViewModel,
                        previousRoute = navController.previousBackStackEntry?.destination?.route ?: "",
                        onNavigateBack = { navController.navigateUp() },
                        onNavigateToBoard = { boardId, boardState ->
                            navController.navigateToBoard(boardId, boardState)
                        },
                        onShowSnackbar = onShowSnackbar
                    )
                }
            }
        }
    }
}