package com.example.finito.core.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.finito.R
import com.example.finito.core.presentation.AppEvent
import com.example.finito.core.presentation.AppViewModel
import com.example.finito.core.presentation.Screen
import com.example.finito.core.presentation.components.bars.BottomBar
import com.example.finito.core.presentation.components.bars.CenterTopBar
import com.example.finito.core.presentation.components.bars.TopBar
import com.example.finito.core.presentation.util.noRippleClickable
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
                Screen.Home.route -> CenterTopBar(
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

