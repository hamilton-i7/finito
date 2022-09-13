package com.example.finito.core.presentation

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.finito.core.presentation.components.Drawer
import com.example.finito.core.presentation.util.rememberSnackbarState
import com.example.finito.features.boards.presentation.SharedBoardViewModel
import kotlinx.coroutines.launch

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(finishActivity: () -> Unit) {
    val context = LocalContext.current
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val drawerViewModel = hiltViewModel<DrawerViewModel>()
    val sharedBoardViewModel = hiltViewModel<SharedBoardViewModel>()
    val snackbarState = rememberSnackbarState()
    val (snackbarHostState, scope, navController) = snackbarState

    // Dynamically change Snackbar bottom padding
    var currentRoute by remember { mutableStateOf(navController.currentDestination?.route) }
    val snackbarModifier = if (currentRoute == Screen.Home.route
        || currentRoute == Screen.Archive.route) {
        // Include bottom bar height on screens that have it
        Modifier
            .navigationBarsPadding()
            .padding(bottom = 80.dp)
    } else Modifier.navigationBarsPadding()

    navController.addOnDestinationChangedListener(
        listener = { _, destination, arguments ->
            currentRoute = destination.route

            if (!drawerRoutes.contains(destination.route)) {
                return@addOnDestinationChangedListener
            }

            val route: String = if (staticDrawerRoutes.contains(destination.route)) {
                destination.route!!
            } else if (destination.route == Screen.Board.route) {
                "${Screen.Board.prefix}/${arguments?.getInt(Screen.BOARD_ROUTE_ARGUMENT)}"
            } else {
                "${Screen.Label.prefix}/${arguments?.getInt(Screen.LABEL_ROUTE_ARGUMENT)}"
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
        onItemSelected = onItemSelected@{ route ->
            if (drawerViewModel.currentRoute == route) {
                scope.launch { drawerState.close() }
                return@onItemSelected
            }
            scope.launch {
                drawerState.close()
                navController.navigate(route)
            }
        }
    ) {
        Scaffold(
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = snackbarModifier
                )
            },
        ) {
            Surface {
                FinitoNavHost(
                    navHostController = navController,
                    drawerState = drawerState,
                    sharedBoardViewModel = sharedBoardViewModel,
                    finishActivity = finishActivity,
                    showSnackbar = { message, onActionClick ->
                        snackbarState.showSnackbar(
                            context = context,
                            message = message,
                            onActionClick = onActionClick
                        )
                    }
                )
            }
        }
    }
}