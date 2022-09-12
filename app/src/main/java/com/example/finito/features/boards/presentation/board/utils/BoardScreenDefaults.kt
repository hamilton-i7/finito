package com.example.finito.features.boards.presentation.board.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.example.finito.R
import com.example.finito.core.domain.util.menu.ActiveBoardScreenOption
import com.example.finito.core.domain.util.menu.ArchivedBoardScreenMenuOption
import com.example.finito.core.domain.util.menu.DeletedBoardScreenMenuOption
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.menu.FinitoMenu
import com.example.finito.core.presentation.util.TopBarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object BoardScreenDefaults {
    private val activeBoardOptions = listOf(
        ActiveBoardScreenOption.EditBoard,
        ActiveBoardScreenOption.ArchiveBoard,
        ActiveBoardScreenOption.DeleteBoard,
        ActiveBoardScreenOption.DeleteCompletedTasks,
    )

    private val archivedBoardOptions = listOf(
        ArchivedBoardScreenMenuOption.EditBoard,
        ArchivedBoardScreenMenuOption.UnarchiveBoard,
        ArchivedBoardScreenMenuOption.DeleteBoard,
        ArchivedBoardScreenMenuOption.DeleteCompletedTasks,
    )

    private val deletedBoardOptions = listOf(
        DeletedBoardScreenMenuOption.EditBoard,
        DeletedBoardScreenMenuOption.RestoreBoard,
        DeletedBoardScreenMenuOption.DeleteCompletedTasks,
    )

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun rememberTopBarState(
        appViewModel: AppViewModel,
        drawerState: DrawerState,
        navController: NavController,
        scope: CoroutineScope = rememberCoroutineScope(),
    ): TopBarState {
        val previousDestination = navController.previousBackStackEntry?.destination?.route

        return remember(appViewModel, drawerState, navController, scope) {
            TopBarState(
                navigationIcon = Icons.Outlined.ArrowBack,
                navigationIconDescription = R.string.go_back,
                title = appViewModel.dynamicTopBarTitle,
                onNavigationIconClick = onNavigationIconClick@{
                    if (previousDestination == Screen.Archive.route
                        || previousDestination != Screen.Trash.route) {
                        navController.navigateUp()
                        return@onNavigationIconClick
                    }
                    scope.launch { drawerState.open() }
                },
                actions = {
                    Box {
                        IconButton(onClick = {
                            appViewModel.onEvent(
                                screen = Screen.Board,
                                event = AppEvent.Board.ShowTopBarMenu(show = true)
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.MoreVert,
                                contentDescription = stringResource(id = R.string.more_options)
                            )
                        }
                        FinitoMenu(
                            show = appViewModel.showTopBarMenu,
                            onDismiss = {
                                appViewModel.onEvent(
                                    screen = Screen.Board,
                                    event = AppEvent.Board.ShowTopBarMenu(show = false)
                                )
                            },
                            options = when (previousDestination) {
                                Screen.Archive.route -> archivedBoardOptions
                                Screen.Trash.route -> deletedBoardOptions
                                else -> activeBoardOptions
                            },
                            onOptionClick = {
                                appViewModel.onEvent(
                                    screen = Screen.Board,
                                    event = AppEvent.Board.ShowTopBarMenu(show = false)
                                )
                                when (previousDestination) {
                                    Screen.Archive.route -> TODO()
                                    Screen.Trash.route -> TODO()
                                    else -> handleActiveBoardOptions(
                                        appViewModel = appViewModel,
                                        menuOption = it as ActiveBoardScreenOption
                                    )
                                }
                            }
                        )
                    }
                }
            )
        }
    }

    private fun handleActiveBoardOptions(
        appViewModel: AppViewModel,
        menuOption: ActiveBoardScreenOption,
    ) {
        when (menuOption) {
            ActiveBoardScreenOption.EditBoard -> TODO(reason = "Navigate to edit board screen")
            ActiveBoardScreenOption.ArchiveBoard -> {
                appViewModel.onEvent(
                    screen = Screen.Board,
                    event = AppEvent.Board.ArchiveBoard
                )
            }
            ActiveBoardScreenOption.DeleteBoard -> {
                appViewModel.onEvent(
                    screen = Screen.Board,
                    event = AppEvent.Board.DeleteBoard
                )
            }
            ActiveBoardScreenOption.DeleteCompletedTasks -> {
                appViewModel.onEvent(
                    screen = Screen.Board,
                    event = AppEvent.Board.DeleteCompletedTasks
                )
            }
        }
    }
}