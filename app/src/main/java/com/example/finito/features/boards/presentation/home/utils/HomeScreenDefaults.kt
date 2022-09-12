package com.example.finito.features.boards.presentation.home.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
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
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.util.BottomBarState
import com.example.finito.core.presentation.util.TopBarState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object HomeScreenDefaults {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun rememberTopBarState(
        appViewModel: AppViewModel,
        drawerState: DrawerState,
        scope: CoroutineScope = rememberCoroutineScope(),
    ): TopBarState {
        val homeTitle = stringResource(id = R.string.home)

        return remember(appViewModel.showSearchbar, scope, drawerState, appViewModel) {
            if (appViewModel.showSearchbar) {
                TopBarState(
                    navigationIcon = Icons.Outlined.ArrowBack,
                    navigationIconDescription = R.string.go_back,
                    title = "",
                    onNavigationIconClick = {
                        appViewModel.onEvent(
                            screen = Screen.Home,
                            event = AppEvent.Home.ShowSearchBar(show = false)
                        )
                    },
                    actions = {
                        IconButton(onClick = {
                            appViewModel.onEvent(
                                screen = Screen.Home,
                                event = AppEvent.Home.ShowSearchBar(show = true)
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Search,
                                contentDescription = stringResource(id = R.string.search_boards)
                            )
                        }
                        IconButton(onClick = {
                            appViewModel.onEvent(
                                screen = Screen.Home,
                                event = AppEvent.Home.ToggleLayout
                            )
                        }) {
                            if (appViewModel.gridLayout) {
                                Icon(
                                    imageVector = Icons.Outlined.ViewStream,
                                    contentDescription = stringResource(id = R.string.list_view)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Outlined.GridView,
                                    contentDescription = stringResource(id = R.string.grid_view)
                                )
                            }
                        }
                    }
                )
            } else {
                TopBarState(
                    navigationIcon = Icons.Outlined.Menu,
                    navigationIconDescription = R.string.open_menu,
                    title = homeTitle,
                    onNavigationIconClick = {
                        scope.launch { drawerState.open() }
                    }
                )
            }
        }
    }

    @Composable
    fun rememberBottomBarState(
        appViewModel: AppViewModel,
        navController: NavController,
    ): BottomBarState {
        return remember(appViewModel, navController) {
            BottomBarState(
                showFab = true,
                fabDescription = R.string.add_board,
                onFabClick = {
                    navController.navigate(route = Screen.CreateBoard.route)
                },
                actions = {
                    IconButton(onClick = {
                        appViewModel.onEvent(
                            screen = Screen.Home,
                            event = AppEvent.Home.ShowSearchBar(show = true)
                        )
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Search,
                            contentDescription = stringResource(id = R.string.search_boards)
                        )
                    }
                    IconButton(onClick = {
                        appViewModel.onEvent(
                            screen = Screen.Home,
                            event = AppEvent.Home.ToggleLayout
                        )
                    }) {
                        if (appViewModel.gridLayout) {
                            Icon(
                                imageVector = Icons.Outlined.ViewStream,
                                contentDescription = stringResource(id = R.string.list_view)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.GridView,
                                contentDescription = stringResource(id = R.string.grid_view)
                            )
                        }
                    }
                }
            )
        }
    }
}