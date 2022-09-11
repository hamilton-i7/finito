package com.example.finito.core.presentation.components

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.R
import com.example.finito.core.domain.util.menu.TrashScreenMenuOption
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.MENU_MIN_WIDTH
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.SearchTopBar
import com.example.finito.core.presentation.util.DialogType
import com.example.finito.core.presentation.util.TopBarState
import com.example.finito.core.presentation.util.noRippleClickable
import com.example.finito.features.boards.presentation.home.components.HomeTopBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Layout(
    appViewModel: AppViewModel = hiltViewModel(),
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    currentRoute: String?,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val enterOnScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pinnedScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.navigationBarsPadding()
            )
        },
        topBar = {
            when (currentRoute) {
                Screen.Home.route -> HomeScreenTopBar(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    showSearchBar = appViewModel.showSearchbar,
                    onSearchBarNavigationIconClick = {
                        appViewModel.onEvent(AppEvent.ShowSearchBar(show = false))
                    },
                    searchQuery = appViewModel.searchQuery,
                    onSearchQueryChange = {
                        appViewModel.onEvent(AppEvent.SearchBoards(it))
                    },
                    scrollBehavior = enterOnScrollBehavior,
                    searchBarScrollBehavior = pinnedScrollBehavior
                )
                Screen.Archive.route, Screen.Trash.route -> TopBar(
                    appViewModel = appViewModel,
                    drawerState = drawerState,
                    currentRoute = currentRoute,
                    scrollBehavior = enterOnScrollBehavior,
                    searchBarScrollBehavior = pinnedScrollBehavior,
                )
            }
        },
        bottomBar = bottomBar@{
            BottomBar(
                showBottomBar = appViewModel.showBottomBar,
                fabDescription = R.string.add_board,
                searchDescription = R.string.search_boards,
                onChangeLayoutClick = {
                    appViewModel.onEvent(AppEvent.ToggleLayout)
                },
                gridLayout = appViewModel.gridLayout,
                onSearchClick = {
                    appViewModel.onEvent(AppEvent.ShowSearchBar(show = true))
                },
                onFabClick = {
                    navController.navigate(route = Screen.CreateBoard.route)
                }
            )
        },
        modifier = Modifier
            .nestedScroll(
                if (appViewModel.showSearchbar)
                    pinnedScrollBehavior.nestedScrollConnection
                else
                    enterOnScrollBehavior.nestedScrollConnection
            )
            .noRippleClickable { focusManager.clearFocus() },
    ) { innerPadding ->
        Surface(modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)) { content() }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenTopBar(
    onMenuClick: () -> Unit,
    showSearchBar: Boolean,
    onSearchBarNavigationIconClick: () -> Unit,
    searchQuery: TextFieldValue,
    onSearchQueryChange: (TextFieldValue) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    searchBarScrollBehavior: TopAppBarScrollBehavior,
) {
    val focusRequester = remember { FocusRequester() }

    AnimatedContent(
        targetState = showSearchBar,
        transitionSpec = {
            (slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Start
            ) with slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.End)
            ).using(SizeTransform(clip = false))
        }
    ) { showingSearchBar ->
        if (showingSearchBar) {
            SearchTopBar(
                query = searchQuery,
                onBackClick = onSearchBarNavigationIconClick,
                onQueryChange = onSearchQueryChange,
                scrollBehavior = searchBarScrollBehavior,
                focusRequester = focusRequester
            )
        } else {
            HomeTopBar(
                onMenuClick = onMenuClick,
                scrollBehavior = scrollBehavior
            )
        }
    }
}

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    appViewModel: AppViewModel,
    drawerState: DrawerState,
    currentRoute: String?,
    scrollBehavior: TopAppBarScrollBehavior,
    searchBarScrollBehavior: TopAppBarScrollBehavior,
) {
    val scope = rememberCoroutineScope()

    val topBarState: TopBarState = when (currentRoute) {
        Screen.Archive.route -> atArchiveScreen(scope, drawerState)
        Screen.Trash.route -> atTrashScreen(appViewModel = appViewModel, scope, drawerState)
        else -> TopBarState(
            navigationIcon = Icons.Outlined.Menu,
            navigationIconDescription = R.string.open_menu,
            title = R.string.home,
            onNavigationIconClick = {}
        )
    }

    AnimatedContent(
        targetState = appViewModel.showSearchbar,
        transitionSpec = {
            (slideIntoContainer(
                towards = AnimatedContentScope.SlideDirection.Start
            ) with slideOutOfContainer(
                towards = AnimatedContentScope.SlideDirection.End)
                    ).using(SizeTransform(clip = false))
        }
    ) { showingSearchBar ->
        if (showingSearchBar) {
            SearchTopBar(
                query = appViewModel.searchQuery,
                onQueryChange = {
                    appViewModel.onEvent(AppEvent.SearchBoards(it))
                },
                onBackClick = {
                    appViewModel.onEvent(AppEvent.ShowSearchBar(show = false))
                },
                scrollBehavior = searchBarScrollBehavior,
            )
        } else {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = topBarState.onNavigationIconClick) {
                        Icon(
                            imageVector = topBarState.navigationIcon,
                            contentDescription = stringResource(
                                id = topBarState.navigationIconDescription
                            )
                        )
                    }
                },
                title = { Text(text = stringResource(id = topBarState.title)) },
                actions = {
                    topBarState.actions()
                },
                scrollBehavior = scrollBehavior,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun atArchiveScreen(scope: CoroutineScope, drawerState: DrawerState): TopBarState {
    return TopBarState(
        navigationIcon = Icons.Outlined.Menu,
        navigationIconDescription = R.string.open_menu,
        title = R.string.archive,
        onNavigationIconClick = {
            scope.launch { drawerState.open() }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun atTrashScreen(
    appViewModel: AppViewModel,
    scope: CoroutineScope,
    drawerState: DrawerState,
): TopBarState {
    val menuOptions = listOf<TrashScreenMenuOption>(TrashScreenMenuOption.EmptyTrash)

    return TopBarState(
        navigationIcon = Icons.Outlined.Menu,
        navigationIconDescription = R.string.open_menu,
        title = R.string.trash,
        onNavigationIconClick = {
            scope.launch { drawerState.open() }
        },
        actions = {
            Box {
                IconButton(onClick = {
                    appViewModel.onEvent(AppEvent.ShowTopBarMenu(show = true))
                }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = stringResource(id = R.string.more_options)
                    )
                }
                DropdownMenu(
                    expanded = appViewModel.showTopBarMenu,
                    onDismissRequest = {
                        appViewModel.onEvent(AppEvent.ShowTopBarMenu(show = false))
                    },
                    modifier = Modifier.widthIn(min = MENU_MIN_WIDTH)
                ) {
                    menuOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(id = option.label)) },
                            onClick = {
                                appViewModel.onEvent(AppEvent.ShowTopBarMenu(show = false))
                                when (option) {
                                    TrashScreenMenuOption.EmptyTrash -> {
                                        appViewModel.onEvent(AppEvent.ShowDialog(
                                            type = DialogType.EmptyTrash
                                        ))
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    )
}